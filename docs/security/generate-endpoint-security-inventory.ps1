param(
    [string]$SourceRoot = (Join-Path $PSScriptRoot '..\..\main\java'),
    [string]$SecurityConfigPath = (Join-Path $PSScriptRoot '..\..\main\java\com\ecommerce\app\SecurityConfig.java'),
    [string]$OutputPath = (Join-Path $PSScriptRoot 'application-security-endpoint-inventory.csv'),
    [string]$PermissionCatalogueOutputPath = (Join-Path $PSScriptRoot 'application-security-permission-catalogue.csv')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-AnnotationBlock {
    param(
        [string[]]$Lines,
        [int]$StartIndex
    )

    $parts = [System.Collections.Generic.List[string]]::new()
    $balance = 0
    $seenParenthesis = $false
    $index = $StartIndex

    while ($index -lt $Lines.Count) {
        $line = $Lines[$index].Trim()
        $parts.Add($line)
        $openCount = ([regex]::Matches($line, '\(')).Count
        $closeCount = ([regex]::Matches($line, '\)')).Count
        if ($openCount -gt 0) {
            $seenParenthesis = $true
        }
        $balance += $openCount - $closeCount

        if (-not $seenParenthesis -or $balance -le 0) {
            break
        }
        $index++
    }

    [PSCustomObject]@{
        Text = ($parts -join ' ')
        EndIndex = $index
    }
}

function Get-MappingDetails {
    param([string]$Annotation)

    if ($Annotation -notmatch '^@(?<name>RequestMapping|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)\b') {
        return $null
    }

    $name = $Matches.name
    $httpMethods = switch ($name) {
        'GetMapping' { @('GET') }
        'PostMapping' { @('POST') }
        'PutMapping' { @('PUT') }
        'PatchMapping' { @('PATCH') }
        'DeleteMapping' { @('DELETE') }
        default {
            $requestMethods = @([regex]::Matches($Annotation, 'RequestMethod\.(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS|TRACE)') |
                ForEach-Object { $_.Groups[1].Value } |
                Sort-Object -Unique)
            if ($requestMethods.Count -eq 0) { @('ALL') } else { @($requestMethods) }
        }
    }

    $paths = @([regex]::Matches($Annotation, '"(?<value>[^"\\]*(?:\\.[^"\\]*)*)"') |
        ForEach-Object { $_.Groups['value'].Value } |
        Sort-Object -Unique)
    if ($paths.Count -eq 0) {
        $paths = @('')
    }

    [PSCustomObject]@{
        Name = $name
        HttpMethods = @($httpMethods)
        Paths = @($paths)
    }
}

function Join-RoutePath {
    param(
        [string]$ClassPath,
        [string]$MethodPath
    )

    $left = if ([string]::IsNullOrWhiteSpace($ClassPath)) { '' } else { $ClassPath.Trim() }
    $right = if ($null -eq $MethodPath) { '' } else { $MethodPath.Trim() }

    if ([string]::IsNullOrWhiteSpace($left)) {
        if ([string]::IsNullOrWhiteSpace($right) -or $right -eq '/') {
            return '/'
        }
        return '/' + $right.Trim('/')
    }

    $basePath = '/' + $left.Trim('/')
    if ([string]::IsNullOrWhiteSpace($right)) {
        return $basePath
    }
    if ($right -eq '/') {
        return $basePath + '/'
    }
    return $basePath + '/' + $right.Trim('/')
}

function Test-AntLikePath {
    param(
        [string]$Path,
        [string]$Pattern
    )

    if ($Pattern -eq '/') {
        return $Path -eq '/'
    }
    if ($Pattern.EndsWith('/**')) {
        $prefix = $Pattern.Substring(0, $Pattern.Length - 3)
        return $Path -eq $prefix -or $Path.StartsWith($prefix + '/')
    }
    return $Path -eq $Pattern
}

function Get-JavaStringArrayValues {
    param(
        [string]$JavaSource,
        [string]$VariableName
    )

    $escapedVariableName = [regex]::Escape($VariableName)
    $arrayMatch = [regex]::Match(
        $JavaSource,
        "(?s)\bString\s*\[\]\s+$escapedVariableName\s*=\s*\{(?<body>.*?)\}\s*;"
    )
    if (-not $arrayMatch.Success) {
        throw "Unable to find Java String[] '$VariableName' in the security configuration."
    }

    return @([regex]::Matches($arrayMatch.Groups['body'].Value, '"(?<value>[^"\\]*(?:\\.[^"\\]*)*)"') |
        ForEach-Object { $_.Groups['value'].Value } |
        Sort-Object -Unique)
}

function Get-SecurityUrlPolicy {
    param([string]$Path)

    $resolvedPath = (Resolve-Path -LiteralPath $Path).Path
    $javaSource = Get-Content -Raw -LiteralPath $resolvedPath
    $permitAllPatterns = @(
        Get-JavaStringArrayValues -JavaSource $javaSource -VariableName 'STATIC_WHITELIST'
        Get-JavaStringArrayValues -JavaSource $javaSource -VariableName 'PUBLIC_URLS'
    ) | Sort-Object -Unique

    $authorityPatterns = @([regex]::Matches(
            $javaSource,
            '(?s)\.requestMatchers\s*\((?<args>.*?)\)\s*\.has(?:Any)?(?:Authority|Role)\s*\('
        ) |
        ForEach-Object {
            [regex]::Matches($_.Groups['args'].Value, '"(?<value>[^"\\]*(?:\\.[^"\\]*)*)"') |
                ForEach-Object { $_.Groups['value'].Value }
        } |
        Sort-Object -Unique)

    if ($permitAllPatterns.Count -eq 0) {
        throw 'No permit-all URL patterns were read from SecurityConfig.'
    }
    if ($authorityPatterns.Count -eq 0) {
        throw 'No explicit authority URL patterns were read from SecurityConfig.'
    }

    [PSCustomObject]@{
        SourcePath = $resolvedPath
        PermitAllPatterns = @($permitAllPatterns)
        AuthorityPatterns = @($authorityPatterns)
    }
}

$securityUrlPolicy = Get-SecurityUrlPolicy -Path $SecurityConfigPath

function Get-UrlRule {
    param([string]$Path)

    foreach ($pattern in $securityUrlPolicy.PermitAllPatterns) {
        if (Test-AntLikePath -Path $Path -Pattern $pattern) {
            return 'PERMIT_ALL'
        }
    }

    foreach ($pattern in $securityUrlPolicy.AuthorityPatterns) {
        if (Test-AntLikePath -Path $Path -Pattern $pattern) {
            if ($Path -match '^/(?:admin|api)/fraud(?:/|$)') {
                return 'FRAUD_AUTHORITY_SET'
            }
            return 'EXPLICIT_AUTHORITY_SET'
        }
    }

    return 'AUTHENTICATED_ONLY'
}

function Get-TargetZoneCandidate {
    param(
        [string]$Path,
        [string]$PackageName,
        [string]$Controller
    )

    if ($Path -match '^/api(?:/|$)') { return 'API' }
    if ($Path -match '(?i)(webhook|callback)') { return 'WEBHOOK_CALLBACK' }
    if ($Path -eq '/' -or
        $Path -in @('/robots.txt', '/sitemap.xml', '/llms.txt', '/maintenance', '/register', '/customerregister/register', '/users/login') -or
        $Path -match '^/(public|cart|carts|cart_address|checkout/guest|forgotpassword|customer_registration|district)(?:/|$)' -or
        $Path -match '^/users/(uregistrations|usave|frontRegistrationSave|userforgotpassword)(?:/|$)' -or
        $Path -match '^/order/(create|savebyvendor|savebyvendorupdate|placed)(?:/|$)') {
        return 'PUBLIC_EXPLICIT'
    }
    if (($Controller -eq 'UsersController' -and (
            $Path -in @('/users', '/users/', '/users/index', '/users/userbystatus', '/users/login-history', '/users/registrations', '/users/save') -or
            $Path -match '^/users/(?:login-history|change-password|edit|delete|deletewithexception|generate-referral-code|detailsinfo|view)(?:/|$)')) -or
        $Controller -eq 'UserDetailsController') {
        return 'ADMIN'
    }
    if ($Path -in @('/order', '/order/', '/order/index')) {
        return 'ADMIN'
    }
    if ($Path -match '^/(admin|role|privilege|module)(?:/|$)' -or $PackageName -match '\.admin(?:\.|$)' -or
        $PackageName -match '\.admincustomer(?:\.|$)' -or $PackageName -match '\.adminvendor(?:\.|$)' -or
        $Controller -match '^Admin') {
        return 'ADMIN'
    }
    if ($Path -match '^/(vendor|vendor-|vendor_|vendorprofile|vendorlogo|vendoraddress|productvendor)(?:/|$)' -or
        $PackageName -match '\.vendor(?:\.|$)') {
        return 'VENDOR'
    }
    if ($Path -match '^/(customer|customer-|customer_|customerprofileimage|wishlist)(?:/|$)' -or
        $Path -eq '/users/profile' -or
        $PackageName -match '\.customer(?:\.|$)' -or $Controller -match '^Customer') {
        return 'CUSTOMER'
    }
    if ($PackageName -match '\.publics(?:\.|$)' -or $Controller -match '^Public|^Welcome') {
        return 'PUBLIC_REVIEW_REQUIRED'
    }
    if ($PackageName -match '\.product\.controller(?:\.|$)' -or
        $PackageName -match '\.module\.(ReferralRewards|shipping|settings)\.controller(?:\.|$)') {
        return 'ADMIN_LEGACY_ROUTE_REVIEW'
    }
    return 'SHARED_OR_UNCLASSIFIED'
}

function Get-ModuleCandidate {
    param(
        [string]$Path,
        [string]$PackageName,
        [string]$Controller
    )

    if ($Path -match '^/(role|privilege|module)(?:/|$)') { return 'IAM' }

    $source = ($Path + ' ' + $PackageName + ' ' + $Controller).ToLowerInvariant()
    if ($source -match 'fraud') { return 'FRAUD' }
    if ($source -match 'communication|notification|message') { return 'COMMUNICATION' }
    if ($source -match 'blog') { return 'BLOG' }
    if ($source -match 'cart|checkout|billingaddress') { return 'CART_CHECKOUT' }
    if ($source -match 'wishlist') { return 'WISHLIST' }
    if ($source -match 'review|comment|ratecontroller') { return 'REVIEW_RATING' }
    if ($source -match 'referral|reward|wallet|coupon|giftcard|cashback|cashout|promotion') { return 'PROMOTION_REWARDS' }
    if ($source -match 'commission') { return 'COMMISSION' }
    if ($source -match 'shipping|shipment|carrier|delivery|packaging|pickup') { return 'SHIPPING' }
    if ($source -match 'stock|inventory|warehouse') { return 'INVENTORY' }
    if ($source -match 'product|catalog|manufacturer|warranty|dimension|units') { return 'CATALOG' }
    if ($source -match 'order|sales|payment|emi|refund|return') { return 'ORDER_PAYMENT' }
    if ($source -match 'vendor|payout') { return 'VENDOR_MANAGEMENT' }
    if ($source -match 'customer') { return 'CUSTOMER_ACCOUNT' }
    if ($source -match 'user|role|privilege|module|password|login') { return 'IDENTITY_ACCESS' }
    if ($source -match 'setting') { return 'SETTINGS' }
    if ($source -match 'ads|marketing|socialshare|seo') { return 'MARKETING_SEO' }
    if ($source -match 'system|endpoint|location') { return 'SYSTEM' }
    return 'GENERAL'
}

function Get-ActionCandidate {
    param(
        [string]$HttpMethod,
        [string]$JavaMethod
    )

    if ($JavaMethod -match '(?i)(export|download|pdf|generate|seed|verify|approve|reject|refund|payout|password|role|privilege|delete|remove|revoke|suspend)') {
        return 'SENSITIVE_REVIEW'
    }
    if ($HttpMethod -in @('GET', 'HEAD', 'OPTIONS')) {
        return 'READ'
    }
    if ($HttpMethod -eq 'ALL') {
        return 'HTTP_METHOD_REVIEW'
    }
    return 'MANAGE'
}

function Get-ScopeCandidate {
    param([string]$Zone)

    switch ($Zone) {
        'PUBLIC_EXPLICIT' { 'EXPLICIT_ALLOWLIST_INPUT_LIMITS_AND_SESSION_SCOPE' }
        'PUBLIC_REVIEW_REQUIRED' { 'PUBLIC_JUSTIFICATION_OR_RECLASSIFY' }
        'CUSTOMER' { 'ACTIVE_CUSTOMER_AND_CURRENT_USER_OWNERSHIP' }
        'VENDOR' { 'ACTIVE_VENDOR_MEMBERSHIP_PERMISSION_AND_RESOURCE_SCOPE' }
        'ADMIN' { 'ACTIVE_ADMIN_AND_MODULE_PERMISSION' }
        'ADMIN_LEGACY_ROUTE_REVIEW' { 'ADMIN_MODULE_PERMISSION_AND_ROUTE_PREFIX_REVIEW' }
        'API' { 'CLIENT_OR_USER_SCOPE_AND_RESOURCE_SCOPE' }
        'WEBHOOK_CALLBACK' { 'SIGNATURE_REPLAY_PROTECTION_AND_IDEMPOTENCY' }
        default { 'AUTHENTICATED_OWNERSHIP_OR_MODULE_PERMISSION_REVIEW' }
    }
}

function Get-ReviewStatusCandidate {
    param([string]$Path)

    if ($Path -match '^/(role|privilege|module|adminvendorusers|vendor-users)(?:/|$)' -or
        $Path -match '^/users/login-history(?:/|$)' -or
        $Path -match '^/(adminvendor|admin-vendor-payout-methods|vendor-payout-methods|admin/ads|catalog-attributes|manufacturer|uom)(?:/|$).*delete(?:/|$)' -or
        $Path -match '^/(product/delete|productcategory/(save|delete)|productvendor/(save|delete)|vendorprofile/save|vendor-payout/save|customer/save|public/home-contact-save|forgotpassword/(showemail|reset)|changepassword|vendorverifications/verify-email)(?:/|$)') {
        return 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF'
    }
    return 'PENDING_MANUAL_CONFIRMATION'
}

function Get-PermissionCandidate {
    param(
        [string]$Zone,
        [string]$Module,
        [string]$Action,
        [string]$JavaMethod,
        [string]$Path
    )

    $moduleSegment = $Module.ToLowerInvariant().Replace('_', '.')
    if ($Zone -eq 'CUSTOMER' -and $moduleSegment.StartsWith('customer.')) {
        $moduleSegment = $moduleSegment.Substring('customer.'.Length)
    }
    if ($Zone -eq 'VENDOR' -and $moduleSegment.StartsWith('vendor.')) {
        $moduleSegment = $moduleSegment.Substring('vendor.'.Length)
    }
    if ($Zone -eq 'ADMIN' -and $Path -match '^/users/login-history(?:/|$)') {
        return 'platform.security.audit.read'
    }
    if ($Zone -eq 'ADMIN' -and $Path -match '^/users/change-password(?:/|$)') {
        return 'platform.identity.password.reset'
    }
    if ($Path -match '^/changepassword(?:/|$)') {
        return 'internal.identity.access.disabled'
    }
    if ($Zone -eq 'ADMIN' -and (
            $Path -match '^/users/(?:registrations|edit|save|delete|deletewithexception|generate-referral-code)(?:/|$)')) {
        return 'platform.identity.user.manage'
    }
    if ($Zone -eq 'ADMIN' -and (
            $Path -in @('/users', '/users/', '/users/index', '/users/userbystatus') -or
            $Path -match '^/(?:users/detailsinfo|userdetails)(?:/|$)')) {
        return 'platform.identity.user.read'
    }
    if ($Zone -eq 'ADMIN' -and $Path -match '^/adminvendorusers(?:/|$)') {
        if ($Path -match '^/adminvendorusers/(?:role_delete|privileges_delete)(?:/|$)') {
            return 'platform.vendor.management.delete'
        }
        if ($Path -match '^/adminvendorusers/privileges(?:_|list|/|$)') {
            return 'platform.vendor.management.privilege'
        }
        if ($Path -match '^/adminvendorusers/role_save(?:/|$)') {
            return 'platform.vendor.management.manage'
        }
        return 'platform.vendor.management.read'
    }
    if ($Zone -eq 'VENDOR' -and $Path -match '^/vendor-users/roles(?:/|$)') {
        return 'vendor.role.manage'
    }
    if ($Zone -eq 'VENDOR' -and $Path -match '^/vendor-users(?:/|$)') {
        return 'vendor.staff.manage'
    }
    if ($Zone -eq 'ADMIN' -and $Module -eq 'IAM') {
        if ($Action -eq 'READ') {
            return 'platform.iam.read'
        }
        if ($Path -match '^/(privilege|module)(?:/|$)') {
            return 'platform.iam.protected.manage'
        }
        return 'platform.iam.manage'
    }
    $actionSegment = switch ($Action) {
        'READ' { 'read' }
        'MANAGE' { 'manage' }
        'HTTP_METHOD_REVIEW' { 'method-review' }
        'SENSITIVE_REVIEW' {
            $sensitiveActions = @(
                'refund', 'payout', 'approve', 'reject', 'revoke', 'suspend',
                'delete', 'remove', 'export', 'download', 'pdf', 'generate',
                'seed', 'verify', 'password', 'role', 'privilege'
            )
            $matchedAction = $sensitiveActions |
                Where-Object { $JavaMethod -match [regex]::Escape($_) } |
                Select-Object -First 1
            if ($null -eq $matchedAction) { 'sensitive-review' } else { $matchedAction }
        }
        default { 'review' }
    }

    switch ($Zone) {
        'PUBLIC_EXPLICIT' { "public.$moduleSegment.$actionSegment" }
        'PUBLIC_REVIEW_REQUIRED' { "public.review.$moduleSegment.$actionSegment" }
        'CUSTOMER' { "customer.$moduleSegment.$actionSegment" }
        'VENDOR' { "vendor.$moduleSegment.$actionSegment" }
        'ADMIN' { "platform.$moduleSegment.$actionSegment" }
        'ADMIN_LEGACY_ROUTE_REVIEW' { "platform.$moduleSegment.$actionSegment" }
        'API' { "api.$moduleSegment.$actionSegment" }
        'WEBHOOK_CALLBACK' { "webhook.$moduleSegment.receive" }
        default { "internal.unclassified.$moduleSegment.$actionSegment" }
    }
}

function New-ValidatedCsvExport {
    param(
        [object[]]$Rows,
        [string]$DestinationPath,
        [string[]]$RequiredHeaders,
        [string[]]$RequiredValueHeaders,
        [string]$ArtifactName
    )

    if ($Rows.Count -eq 0) {
        throw "$ArtifactName contains no rows. Existing output was not replaced."
    }

    $fullDestinationPath = [System.IO.Path]::GetFullPath($DestinationPath)
    $destinationDirectory = [System.IO.Path]::GetDirectoryName($fullDestinationPath)
    if (-not (Test-Path -LiteralPath $destinationDirectory)) {
        New-Item -Path $destinationDirectory -ItemType Directory | Out-Null
    }

    $temporaryPath = Join-Path $destinationDirectory (
        '.{0}.{1}.tmp' -f [System.IO.Path]::GetFileName($fullDestinationPath), [guid]::NewGuid().ToString('N')
    )

    try {
        $Rows | Export-Csv -LiteralPath $temporaryPath -NoTypeInformation -Encoding utf8
        $bytes = [System.IO.File]::ReadAllBytes($temporaryPath)
        if ([Array]::IndexOf($bytes, [byte]0) -ge 0) {
            throw "$ArtifactName contains NUL bytes. Existing output was not replaced."
        }

        $parsedRows = @(Import-Csv -LiteralPath $temporaryPath)
        if ($parsedRows.Count -ne $Rows.Count) {
            throw "$ArtifactName row validation failed: expected $($Rows.Count), parsed $($parsedRows.Count)."
        }

        $actualHeaders = @($parsedRows[0].PSObject.Properties.Name)
        if (($actualHeaders -join '|') -ne ($RequiredHeaders -join '|')) {
            throw "$ArtifactName header validation failed. Existing output was not replaced."
        }

        foreach ($requiredValueHeader in $RequiredValueHeaders) {
            $blankCount = @($parsedRows | Where-Object {
                    [string]::IsNullOrWhiteSpace([string]$_.$requiredValueHeader)
                }).Count
            if ($blankCount -gt 0) {
                throw "$ArtifactName contains $blankCount blank '$requiredValueHeader' values."
            }
        }

        return [PSCustomObject]@{
            TemporaryPath = $temporaryPath
            DestinationPath = $fullDestinationPath
        }
    } catch {
        if (Test-Path -LiteralPath $temporaryPath) {
            [System.IO.File]::Delete($temporaryPath)
        }
        throw
    }
}

function Publish-ValidatedCsvExport {
    param([PSCustomObject]$Export)

    if (Test-Path -LiteralPath $Export.DestinationPath) {
        [System.IO.File]::Delete($Export.DestinationPath)
    }
    [System.IO.File]::Move($Export.TemporaryPath, $Export.DestinationPath)
}

$sourcePath = (Resolve-Path -LiteralPath $SourceRoot).Path
$rows = [System.Collections.Generic.List[object]]::new()

foreach ($file in Get-ChildItem -LiteralPath $sourcePath -Filter '*.java' -File -Recurse) {
    $lines = Get-Content -LiteralPath $file.FullName
    $raw = $lines -join "`n"
    if ($raw -notmatch '@(RestController|Controller)\b') {
        continue
    }

    $packageName = if ($raw -match '(?m)^package\s+([^;]+);') { $Matches[1] } else { '' }
    $relativeFile = $file.FullName.Substring($sourcePath.Length + 1).Replace('\', '/')
    $pendingAnnotations = [System.Collections.Generic.List[string]]::new()
    $classPaths = @('')
    $classGuard = ''
    $controllerName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $trimmed = $lines[$i].Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('//') -or $trimmed.StartsWith('*') -or $trimmed.StartsWith('/*')) {
            continue
        }

        if ($trimmed.StartsWith('@')) {
            $block = Get-AnnotationBlock -Lines $lines -StartIndex $i
            $pendingAnnotations.Add($block.Text)
            $i = $block.EndIndex
            continue
        }

        if ($trimmed -match '^(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed)\s+)*(class|record)\s+(?<name>[A-Za-z0-9_]+)') {
            $controllerName = $Matches.name
            $classMapping = $pendingAnnotations |
                ForEach-Object { Get-MappingDetails -Annotation $_ } |
                Where-Object { $null -ne $_ } |
                Select-Object -First 1
            if ($null -ne $classMapping) {
                $classPaths = @($classMapping.Paths)
            }
            $guardAnnotation = $pendingAnnotations | Where-Object { $_ -match '^@PreAuthorize\b' } | Select-Object -First 1
            if ($null -ne $guardAnnotation) {
                $classGuard = ($guardAnnotation -replace '\s+', ' ').Trim()
            }
            $pendingAnnotations.Clear()
            continue
        }

        $methodMappings = @($pendingAnnotations |
            ForEach-Object { Get-MappingDetails -Annotation $_ } |
            Where-Object { $null -ne $_ })
        if ($methodMappings.Count -gt 0) {
            $signatureParts = [System.Collections.Generic.List[string]]::new()
            $signatureIndex = $i
            while ($signatureIndex -lt $lines.Count -and $signatureParts.Count -lt 20) {
                $signatureLine = $lines[$signatureIndex].Trim()
                if (-not [string]::IsNullOrWhiteSpace($signatureLine)) {
                    $signatureParts.Add($signatureLine)
                }
                if ($signatureLine.Contains('{') -or $signatureLine.EndsWith(';')) {
                    break
                }
                $signatureIndex++
            }
            $signature = $signatureParts -join ' '
            $javaMethod = if ($signature -match '(?<name>[A-Za-z_][A-Za-z0-9_]*)\s*\(') { $Matches.name } else { 'UNRESOLVED_METHOD' }
            $guardAnnotation = $pendingAnnotations | Where-Object { $_ -match '^@PreAuthorize\b' } | Select-Object -First 1
            $methodGuard = if ($null -ne $guardAnnotation) {
                ($guardAnnotation -replace '\s+', ' ').Trim()
            } else {
                $classGuard
            }

            foreach ($mapping in $methodMappings) {
                foreach ($classPath in $classPaths) {
                    foreach ($methodPath in $mapping.Paths) {
                        $route = Join-RoutePath -ClassPath $classPath -MethodPath $methodPath
                        foreach ($httpMethod in $mapping.HttpMethods) {
                            $zone = Get-TargetZoneCandidate -Path $route -PackageName $packageName -Controller $controllerName
                            $module = Get-ModuleCandidate -Path $route -PackageName $packageName -Controller $controllerName
                            $action = Get-ActionCandidate -HttpMethod $httpMethod -JavaMethod $javaMethod
                            $urlRule = Get-UrlRule -Path $route
                            $rows.Add([PSCustomObject]@{
                                SourceFile = $relativeFile
                                SourceLine = $i + 1
                                Package = $packageName
                                Controller = $controllerName
                                JavaMethod = $javaMethod
                                HttpMethod = $httpMethod
                                Path = $route
                                CurrentUrlRule = $urlRule
                                CurrentMethodGuard = $methodGuard
                                TargetZoneCandidate = $zone
                                ModuleCandidate = $module
                                ActionCandidate = $action
                                PermissionCandidate = Get-PermissionCandidate -Zone $zone -Module $module -Action $action -JavaMethod $javaMethod -Path $route
                                RequiredScopeCandidate = Get-ScopeCandidate -Zone $zone
                                ReviewStatus = Get-ReviewStatusCandidate -Path $route
                            })
                        }
                    }
                }
            }
            $i = $signatureIndex
        }

        $pendingAnnotations.Clear()
    }
}

$orderedRows = @($rows | Sort-Object Package, Controller, Path, HttpMethod, JavaMethod)

$catalogueRows = @($orderedRows |
    Group-Object PermissionCandidate |
    ForEach-Object {
        $permissionSlug = $_.Name
        $namespace = ($permissionSlug -split '\.')[0]
        $assignmentPolicy = if ($permissionSlug -eq 'platform.iam.protected.manage') {
            'PROTECTED_BREAK_GLASS_ROLE_ONLY'
        } else { switch ($namespace) {
            'platform' { 'PLATFORM_ROLE_ASSIGNABLE_WITH_GRANT_AUTHORITY' }
            'vendor' { 'VENDOR_ROLE_ASSIGNABLE_WITH_GRANT_CEILING' }
            'customer' { 'PROTECTED_CUSTOMER_SELF_SERVICE_POLICY' }
            'api' { 'CLIENT_OR_USER_SCOPE_REVIEW' }
            'webhook' { 'PROTECTED_NON_ASSIGNABLE_INGRESS_POLICY' }
            'internal' { 'PROTECTED_NON_ASSIGNABLE_SYSTEM_POLICY' }
            'public' { 'PROTECTED_NON_ASSIGNABLE_PUBLIC_POLICY' }
            default { 'NON_ASSIGNABLE_UNTIL_REVIEWED' }
        } }
        $stepUpCandidate = if (
            @($_.Group | Where-Object ActionCandidate -eq 'SENSITIVE_REVIEW').Count -gt 0
        ) {
            'REQUIRED_OR_DOCUMENT_EXCEPTION'
        } else {
            'NOT_DEFAULT_REVIEW_IF_HIGH_IMPACT'
        }

        [PSCustomObject]@{
            PermissionCandidate = $permissionSlug
            Namespace = $namespace
            ModuleCandidates = (($_.Group.ModuleCandidate | Sort-Object -Unique) -join '|')
            ActionCandidates = (($_.Group.ActionCandidate | Sort-Object -Unique) -join '|')
            ZoneCandidates = (($_.Group.TargetZoneCandidate | Sort-Object -Unique) -join '|')
            EndpointCount = $_.Count
            AssignmentPolicyCandidate = $assignmentPolicy
            StepUpCandidate = $stepUpCandidate
            LifecycleCandidate = 'PROTECTED_VERSIONED_PROPOSED'
            ReviewStatus = if (
                @($_.Group | Where-Object ReviewStatus -ne 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF').Count -eq 0
            ) {
                'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF'
            } else {
                'PENDING_MANUAL_CONFIRMATION'
            }
        }
    } |
    Sort-Object PermissionCandidate)

$endpointHeaders = @(
    'SourceFile', 'SourceLine', 'Package', 'Controller', 'JavaMethod', 'HttpMethod', 'Path',
    'CurrentUrlRule', 'CurrentMethodGuard', 'TargetZoneCandidate', 'ModuleCandidate',
    'ActionCandidate', 'PermissionCandidate', 'RequiredScopeCandidate', 'ReviewStatus'
)
$endpointRequiredValues = @(
    'SourceFile', 'SourceLine', 'Package', 'Controller', 'JavaMethod', 'HttpMethod', 'Path',
    'CurrentUrlRule', 'TargetZoneCandidate', 'ModuleCandidate', 'ActionCandidate',
    'PermissionCandidate', 'RequiredScopeCandidate', 'ReviewStatus'
)
$catalogueHeaders = @(
    'PermissionCandidate', 'Namespace', 'ModuleCandidates', 'ActionCandidates', 'ZoneCandidates',
    'EndpointCount', 'AssignmentPolicyCandidate', 'StepUpCandidate', 'LifecycleCandidate', 'ReviewStatus'
)

$endpointExport = $null
$catalogueExport = $null
try {
    $endpointExport = New-ValidatedCsvExport -Rows $orderedRows -DestinationPath $OutputPath `
        -RequiredHeaders $endpointHeaders -RequiredValueHeaders $endpointRequiredValues `
        -ArtifactName 'Endpoint security inventory'
    $catalogueExport = New-ValidatedCsvExport -Rows $catalogueRows `
        -DestinationPath $PermissionCatalogueOutputPath -RequiredHeaders $catalogueHeaders `
        -RequiredValueHeaders $catalogueHeaders -ArtifactName 'Permission catalogue'

    Publish-ValidatedCsvExport -Export $endpointExport
    Publish-ValidatedCsvExport -Export $catalogueExport
} finally {
    foreach ($pendingExport in @($endpointExport, $catalogueExport)) {
        if ($null -ne $pendingExport -and (Test-Path -LiteralPath $pendingExport.TemporaryPath)) {
            [System.IO.File]::Delete($pendingExport.TemporaryPath)
        }
    }
}

$summary = $orderedRows | Group-Object TargetZoneCandidate | Sort-Object Name | ForEach-Object {
    [PSCustomObject]@{ Zone = $_.Name; Endpoints = $_.Count }
}
$summary | Format-Table -AutoSize
"inventory_rows=$($orderedRows.Count)"
"security_config=$($securityUrlPolicy.SourcePath)"
"output=$((Resolve-Path -LiteralPath $OutputPath).Path)"
"output_sha256=$((Get-FileHash -Algorithm SHA256 -LiteralPath $OutputPath).Hash)"
"catalogue_rows=$($catalogueRows.Count)"
"catalogue_output=$((Resolve-Path -LiteralPath $PermissionCatalogueOutputPath).Path)"
"catalogue_sha256=$((Get-FileHash -Algorithm SHA256 -LiteralPath $PermissionCatalogueOutputPath).Hash)"
