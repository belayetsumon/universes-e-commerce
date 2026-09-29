<#
Builds and validates the durable Phase 1 security-decision layer.
The source inventory and candidate catalogue remain rebuildable facts.
#>
param(
    [string]$InventoryPath = (Join-Path $PSScriptRoot 'application-security-endpoint-inventory.csv'),
    [string]$PermissionCataloguePath = (Join-Path $PSScriptRoot 'application-security-permission-catalogue.csv'),
    [string]$EndpointDecisionOutputPath = (Join-Path $PSScriptRoot 'application-security-endpoint-decision-ledger.csv'),
    [string]$PermissionDecisionOutputPath = (Join-Path $PSScriptRoot 'application-security-permission-decision-ledger.csv'),
    [string]$Reviewer = 'Security architecture review',
    [datetime]$ReviewedAtUtc = [datetime]::UtcNow
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$decisionSchemaVersion = '1.0'
$reviewedAt = $ReviewedAtUtc.ToUniversalTime().ToString('o')
$allowedZones = @('PUBLIC', 'CUSTOMER', 'VENDOR', 'PLATFORM_ADMIN', 'API', 'WEBHOOK', 'INTERNAL_ONLY')
$allowedExactMethods = @('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS')
$allowedDecisionStatuses = @('APPROVED', 'IMPLEMENTED', 'VERIFIED', 'DEFERRED')

function Get-Sha256 {
    param([string]$Value)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value)
        return (($sha256.ComputeHash($bytes) | ForEach-Object { $_.ToString('x2') }) -join '').ToUpperInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Get-RowFingerprint {
    param([object]$Row)
    return Get-Sha256 -Value (@(
            $Row.SourceFile, $Row.SourceLine, $Row.Package, $Row.Controller,
            $Row.JavaMethod, $Row.HttpMethod, $Row.Path, $Row.CurrentUrlRule,
            $Row.CurrentMethodGuard, $Row.TargetZoneCandidate, $Row.ModuleCandidate,
            $Row.ActionCandidate, $Row.PermissionCandidate, $Row.RequiredScopeCandidate,
            $Row.ReviewStatus
        ) -join [char]31)
}

function Assert-Headers {
    param([object[]]$Rows, [string[]]$ExpectedHeaders, [string]$ArtifactName)
    if ($Rows.Count -eq 0) {
        throw "$ArtifactName has no rows."
    }
    $actualHeaders = @($Rows[0].PSObject.Properties.Name)
    if (($actualHeaders -join '|') -ne ($ExpectedHeaders -join '|')) {
        throw "$ArtifactName headers changed. Regenerate or review the source artifact before producing decisions."
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
    $temporaryPath = Join-Path $destinationDirectory ('.{0}.{1}.tmp' -f [System.IO.Path]::GetFileName($fullDestinationPath), [guid]::NewGuid().ToString('N'))

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
        Assert-Headers -Rows $parsedRows -ExpectedHeaders $RequiredHeaders -ArtifactName $ArtifactName
        foreach ($header in $RequiredValueHeaders) {
            $blankCount = @($parsedRows | Where-Object { [string]::IsNullOrWhiteSpace([string]$_.$header) }).Count
            if ($blankCount -gt 0) {
                throw "$ArtifactName contains $blankCount blank '$header' values."
            }
        }
        return [PSCustomObject]@{ TemporaryPath = $temporaryPath; DestinationPath = $fullDestinationPath }
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

function Get-FinalZone {
    param([string]$CandidateZone)
    switch ($CandidateZone) {
        'PUBLIC_EXPLICIT' { return 'PUBLIC' }
        'PUBLIC_REVIEW_REQUIRED' { return 'PUBLIC' }
        'CUSTOMER' { return 'CUSTOMER' }
        'VENDOR' { return 'VENDOR' }
        'ADMIN' { return 'PLATFORM_ADMIN' }
        'ADMIN_LEGACY_ROUTE_REVIEW' { return 'PLATFORM_ADMIN' }
        'API' { return 'API' }
        'WEBHOOK_CALLBACK' { return 'WEBHOOK' }
        'SHARED_OR_UNCLASSIFIED' { return 'INTERNAL_ONLY' }
        default { throw "Unsupported target-zone candidate '$CandidateZone'." }
    }
}

function Get-RequiredAuthentication {
    param([string]$FinalZone, [string]$Action)
    switch ($FinalZone) {
        'PUBLIC' { return 'ANONYMOUS_BROWSER_OR_GUEST_SESSION_WITH_SERVER_SIDE_INPUT_POLICY' }
        'CUSTOMER' { return 'FORM_LOGIN_SESSION_WITH_ACTIVE_CUSTOMER_ACCOUNT' }
        'VENDOR' { return 'FORM_LOGIN_SESSION_WITH_ACTIVE_VENDOR_MEMBERSHIP' }
        'PLATFORM_ADMIN' {
            if ($Action -eq 'SENSITIVE_REVIEW') {
                return 'FORM_LOGIN_SESSION_WITH_ACTIVE_PLATFORM_ACCOUNT_AND_STEP_UP_AUTHENTICATION'
            }
            return 'FORM_LOGIN_SESSION_WITH_ACTIVE_PLATFORM_ACCOUNT'
        }
        'API' { return 'OAUTH2_OR_SIGNED_CLIENT_CREDENTIALS_REQUIRED' }
        'WEBHOOK' { return 'PROVIDER_SIGNATURE_AND_TIMESTAMP_AUTHENTICATION_REQUIRED' }
        'INTERNAL_ONLY' { return 'MUTUALLY_AUTHENTICATED_INTERNAL_SERVICE_IDENTITY_REQUIRED' }
        default { throw "Unsupported final zone '$FinalZone'." }
    }
}

function Get-OwnershipScopeRule {
    param([string]$FinalZone, [string]$SourceZone)
    switch ($FinalZone) {
        'PUBLIC' { return 'EXPLICIT_PUBLIC_INGRESS_INPUT_ALLOWLIST_AND_ANONYMOUS_OR_GUEST_SESSION_SCOPE' }
        'CUSTOMER' { return 'ACTIVE_CUSTOMER_ACCOUNT_AND_AUTHENTICATED_CUSTOMER_OWNERSHIP' }
        'VENDOR' { return 'ACTIVE_VENDOR_MEMBERSHIP_EXACT_CAPABILITY_AND_VENDOR_RESOURCE_OWNERSHIP' }
        'PLATFORM_ADMIN' {
            if ($SourceZone -eq 'ADMIN_LEGACY_ROUTE_REVIEW') {
                return 'ACTIVE_PLATFORM_ACCOUNT_AND_EXACT_CAPABILITY_WITH_LEGACY_ROUTE_MIGRATION_REQUIRED'
            }
            return 'ACTIVE_PLATFORM_ACCOUNT_AND_EXACT_CAPABILITY'
        }
        'API' { return 'CLIENT_OR_USER_SCOPE_AND_RESOURCE_SCOPE' }
        'WEBHOOK' { return 'PROVIDER_ACCOUNT_PLAN_MATCH_AND_SIGNED_EVENT_SCOPE' }
        'INTERNAL_ONLY' { return 'INTERNAL_SERVICE_IDENTITY_AND_EXPLICIT_SERVICE_SCOPE' }
        default { throw "Unsupported final zone '$FinalZone'." }
    }
}

function Get-RepositoryScopeMethod {
    param([string]$FinalZone)
    switch ($FinalZone) {
        'PUBLIC' { return 'DTO_ALLOWLIST_AND_SERVER_SIDE_LOOKUP_ONLY_WITH_NO_PRINCIPAL_BYPASS' }
        'CUSTOMER' { return 'REPOSITORY_QUERY_MUST_INCLUDE_AUTHENTICATED_CUSTOMER_ID_OR_TRUSTED_PARENT' }
        'VENDOR' { return 'REPOSITORY_QUERY_MUST_INCLUDE_ACTIVE_VENDOR_ID_OR_VENDOR_OWNED_PARENT' }
        'PLATFORM_ADMIN' { return 'PLATFORM_SERVICE_GUARD_WITH_TENANT_OR_AGGREGATE_SCOPE_WHERE_APPLICABLE' }
        'API' { return 'REPOSITORY_QUERY_MUST_INCLUDE_CLIENT_OR_USER_SCOPE' }
        'WEBHOOK' { return 'PROVIDER_REFERENCE_AND_PLAN_ID_LOOKUP_WITH_EVENT_DEDUPLICATION' }
        'INTERNAL_ONLY' { return 'REPOSITORY_ACCESS_DENIED_UNTIL_EXPLICIT_INTERNAL_SERVICE_SCOPE_EXISTS' }
        default { throw "Unsupported final zone '$FinalZone'." }
    }
}

function Get-CsrfSignatureReplayRule {
    param([string]$FinalZone, [string]$FinalHttpMethod)
    if ($FinalHttpMethod -eq 'METHOD_SPLIT_REQUIRED') {
        return 'METHOD_SPLIT_REQUIRED_BEFORE_CSRF_SIGNATURE_OR_REPLAY_DECISION'
    }
    switch ($FinalZone) {
        'WEBHOOK' { return 'PROVIDER_SIGNATURE_TIMESTAMP_TOLERANCE_AND_REPLAY_PROTECTION_REQUIRED' }
        'API' { return 'TOKEN_OR_MTLS_REQUIRED_AND_BROWSER_CSRF_NOT_APPLICABLE_TO_NON_BROWSER_API' }
        'INTERNAL_ONLY' { return 'INTERNAL_SERVICE_AUTHENTICATION_AND_REPLAY_POLICY_REQUIRED' }
        default {
            if ($FinalHttpMethod -in @('GET', 'HEAD', 'OPTIONS')) {
                return 'NO_BROWSER_CSRF_REQUIRED_FOR_SAFE_READ'
            }
            return 'SPRING_CSRF_REQUIRED_FOR_BROWSER_MUTATION'
        }
    }
}

function Get-RateLimitPolicy {
    param([string]$FinalZone, [string]$FinalHttpMethod, [string]$Action)
    if ($FinalHttpMethod -eq 'METHOD_SPLIT_REQUIRED') {
        return 'METHOD_SPLIT_REQUIRED_BEFORE_RATE_LIMIT_POLICY'
    }
    switch ($FinalZone) {
        'PUBLIC' {
            if ($FinalHttpMethod -in @('GET', 'HEAD', 'OPTIONS')) {
                return 'PUBLIC_READ_RATE_LIMIT_REQUIRED'
            }
            return 'ANONYMOUS_MUTATION_RATE_LIMIT_REQUIRED'
        }
        'API' { return 'CLIENT_RATE_LIMIT_REQUIRED' }
        'WEBHOOK' { return 'PROVIDER_RATE_LIMIT_AND_REPLAY_WINDOW_REQUIRED' }
        'INTERNAL_ONLY' { return 'INTERNAL_SERVICE_RATE_LIMIT_REQUIRED' }
        default {
            if ($Action -eq 'SENSITIVE_REVIEW' -or $FinalHttpMethod -notin @('GET', 'HEAD', 'OPTIONS')) {
                return 'AUTHENTICATED_MUTATION_OR_SENSITIVE_ACTION_RATE_LIMIT_REQUIRED'
            }
            return 'AUTHENTICATED_READ_RATE_LIMIT_POLICY_REQUIRED'
        }
    }
}

function Get-IdempotencyPolicy {
    param([string]$FinalZone, [string]$FinalHttpMethod)
    if ($FinalHttpMethod -eq 'METHOD_SPLIT_REQUIRED') {
        return 'METHOD_SPLIT_REQUIRED_BEFORE_IDEMPOTENCY_DECISION'
    }
    if ($FinalZone -eq 'WEBHOOK') {
        return 'CALLBACK_EVENT_DEDUPLICATION_AND_IDEMPOTENCY_KEY_REQUIRED'
    }
    if ($FinalHttpMethod -in @('GET', 'HEAD', 'OPTIONS')) {
        return 'NOT_APPLICABLE_TO_SAFE_READ'
    }
    return 'MUTATION_IDEMPOTENCY_OR_DUPLICATE_GUARD_REQUIRED'
}

function Get-AuditEventRequirement {
    param([string]$FinalZone, [string]$Module, [string]$Action)
    $moduleSegment = $Module.ToLowerInvariant().Replace('_', '-')
    $actionSegment = $Action.ToLowerInvariant().Replace('_', '-')
    switch ($FinalZone) {
        'PUBLIC' { return "PUBLIC-INGRESS-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'CUSTOMER' { return "CUSTOMER-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'VENDOR' { return "VENDOR-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'PLATFORM_ADMIN' { return "PLATFORM-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'API' { return "API-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'WEBHOOK' { return "WEBHOOK-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        'INTERNAL_ONLY' { return "INTERNAL-$moduleSegment-$actionSegment-AUDIT-REQUIRED" }
        default { throw "Unsupported final zone '$FinalZone'." }
    }
}

function Test-DestructiveGetContainmentImplemented {
    param([object]$Row)
    return $Row.Path -match '^/(adminvendor|admin-vendor-payout-methods|vendor-payout-methods|admin/ads|catalog-attributes|manufacturer|uom)(?:/|$).*delete(?:/|$)'
}

function Test-ExplicitMutationContainmentImplemented {
    param([object]$Row)
    return $Row.Path -match '^/(product/delete|productcategory/(save|delete)|productvendor/(save|delete)|vendorprofile/save|vendor-payout/save|customer/save|public/home-contact-save)(?:/|$)'
}

function Test-PasswordRecoveryContainmentImplemented {
    param([object]$Row)
    return $Row.Path -match '^/forgotpassword/(showemail|reset)(?:/|$)'
}

function Test-DisabledLegacyPasswordFlowImplemented {
    param([object]$Row)
    return $Row.Path -match '^/changepassword(?:/|$)'
}

function Test-VendorEmailVerificationContainmentImplemented {
    param([object]$Row)
    return $Row.Path -eq '/vendorverifications/verify-email'
}

function Test-ReviewedSensitiveReadOnlyGet {
    param([object]$Row)
    if ($Row.HttpMethod -ne 'GET' -or $Row.ActionCandidate -ne 'SENSITIVE_REVIEW') {
        return $false
    }

    $reviewedReadOnlyPaths = @(
        '/admin/refunds',
        '/admin/return-refund',
        '/admin/return-refunds',
        '/admin/returns',
        '/admin/returns-refunds',
        '/admin-customer/orders/{id}/pdf',
        '/admin-customer/pdf/{id}',
        '/admin/finance/refunds',
        '/admin/finance/return-refund',
        '/admin/finance/return-refunds',
        '/admin/finance/returns',
        '/admin/finance/returns-refunds',
        '/admin/system/seed/permission-catalogue',
        '/admin/blog/export',
        '/customerorder/orders/{id}/pdf',
        '/customerorder/refunds',
        '/forgotpassword',
        '/forgotpassword/',
        '/forgotpassword/index',
        '/forgotpassword/userforgotpassword',
        '/users/change-password',
        '/users/change-password/{id}',
        '/admin/stock/current/pdf',
        '/catalog-variants/generate/{productUuid}',
        '/public/forgot-password',
        '/public/refund-and-returns-policy',
        '/public/refund-returns-policy',
        '/vendor-payout/request',
        '/vendor-order/orders/{id}/pdf',
        '/vendor-order/refunds',
        '/vendorverifications/verify-email',
        '/vendorverifications/verify-mobile'
    )

    return $reviewedReadOnlyPaths -contains $Row.Path
}

function Get-DecisionStatus {
    param([object]$Row, [string]$FinalZone)
    if ($Row.ReviewStatus -eq 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF') {
        return 'IMPLEMENTED'
    }
    if ($Row.HttpMethod -eq 'ALL') {
        return 'DEFERRED'
    }
    if ($Row.TargetZoneCandidate -in @('SHARED_OR_UNCLASSIFIED', 'PUBLIC_REVIEW_REQUIRED')) {
        return 'DEFERRED'
    }
    if ($FinalZone -in @('API', 'WEBHOOK')) {
        return 'DEFERRED'
    }
    if ($FinalZone -eq 'PUBLIC' -and $Row.CurrentUrlRule -ne 'PERMIT_ALL') {
        return 'DEFERRED'
    }
    if ($FinalZone -ne 'PUBLIC' -and $Row.CurrentUrlRule -eq 'PERMIT_ALL') {
        return 'DEFERRED'
    }
    if (Test-ReviewedSensitiveReadOnlyGet -Row $Row) {
        return 'APPROVED'
    }
    if ($Row.HttpMethod -eq 'GET' -and $Row.ActionCandidate -eq 'SENSITIVE_REVIEW') {
        return 'DEFERRED'
    }
    if ($Row.PermissionCandidate -match '(^internal\.unclassified\.|\.method-review$|\.sensitive-review$)') {
        return 'DEFERRED'
    }
    return 'APPROVED'
}

function Get-DecisionRationale {
    param([object]$Row, [string]$FinalZone, [string]$DecisionStatus)
    if ($DecisionStatus -eq 'IMPLEMENTED') {
        if (Test-DestructiveGetContainmentImplemented -Row $Row) {
            return 'IMPLEMENTED: Source evidence confirms the destructive delete endpoint is no longer exposed through GET and its browser POST form is covered by CSRF; migration and deployment-like runtime proof remain outstanding.'
        }
        if (Test-ExplicitMutationContainmentImplemented -Row $Row) {
            return 'IMPLEMENTED: Source evidence confirms the mutation endpoint uses an explicit POST mapping and its browser form is covered by CSRF; migration and deployment-like runtime proof remain outstanding.'
        }
        if (Test-PasswordRecoveryContainmentImplemented -Row $Row) {
            return 'IMPLEMENTED: Source evidence confirms explicit password-recovery GET/POST mappings, neutral account responses, hashed expiring single-use tokens, direct-mail delivery, and CSRF-covered public forms; migration and deployment-like runtime proof remain outstanding.'
        }
        if (Test-DisabledLegacyPasswordFlowImplemented -Row $Row) {
            return 'IMPLEMENTED: Source evidence confirms the legacy password-change endpoint family is explicitly disabled with denyAll while the supported password-change routes remain separate; deployment-like runtime proof remains outstanding.'
        }
        if (Test-VendorEmailVerificationContainmentImplemented -Row $Row) {
            return 'IMPLEMENTED: Source evidence confirms vendor email verification token consumption moved off GET to a CSRF-covered POST confirmation flow with hashed expiring single-use tokens; migration and deployment-like runtime proof remain outstanding.'
        }
        if ($Row.Path -match '^/users/login-history(?:/|$)') {
            return 'IMPLEMENTED: Source evidence confirms exact security-audit controller and service permission guards with raw session identifiers removed from the admin view; migration and deployment-like runtime proof remain outstanding.'
        }
        if ($Row.Controller -eq 'AdminVendorUsersController') {
            return 'IMPLEMENTED: Source evidence confirms exact platform vendor-IAM controller and service permission guards; migration and deployment-like runtime proof remain outstanding.'
        }
        if ($Row.Path -match '^/vendor-users(?:/|$)') {
            return 'IMPLEMENTED: Source evidence confirms exact vendor staff-IAM controller, service, vendor-scope, grant-ceiling, and CSRF controls; invitation lifecycle, revocation/session invalidation, and deployment-like runtime proof remain outstanding.'
        }
        return 'IMPLEMENTED: Source evidence confirms exact platform-IAM controller and service permission guards; migration and deployment-like runtime proof remain outstanding.'
    }
    if ($Row.HttpMethod -eq 'ALL') {
        return 'DEFERRED: Source mapping accepts every HTTP method. Split it into explicit method mappings before a release decision.'
    }
    if ($Row.TargetZoneCandidate -eq 'SHARED_OR_UNCLASSIFIED') {
        return 'DEFERRED: No reliable principal zone was derived. The safe planned zone is internal-only until an owning module approves a final external zone and capability.'
    }
    if ($Row.TargetZoneCandidate -eq 'PUBLIC_REVIEW_REQUIRED') {
        return 'DEFERRED: Public or SEO intent conflicts with current authenticated fallback. Confirm explicit public policy or reclassify the route.'
    }
    if ($FinalZone -eq 'API') {
        return 'DEFERRED: The API currently falls through to form-login authentication. Define client or user authentication and resource scope before release.'
    }
    if ($FinalZone -eq 'WEBHOOK') {
        return 'DEFERRED: The callback lacks documented provider-signature, timestamp, replay, and idempotency controls.'
    }
    if ($FinalZone -eq 'PUBLIC' -and $Row.CurrentUrlRule -ne 'PERMIT_ALL') {
        return 'DEFERRED: Proposed public route is not explicitly permit-all in the current security configuration.'
    }
    if ($FinalZone -ne 'PUBLIC' -and $Row.CurrentUrlRule -eq 'PERMIT_ALL') {
        return 'DEFERRED: Current permit-all rule conflicts with the proposed protected zone.'
    }
    if (Test-ReviewedSensitiveReadOnlyGet -Row $Row) {
        return 'APPROVED: Source review confirms this sensitive GET is a read-only page, redirect, form, policy view, or download/export response. Any state-changing follow-up must remain on an explicit unsafe method with CSRF or token replay controls.'
    }
    if ($Row.HttpMethod -eq 'GET' -and $Row.ActionCandidate -eq 'SENSITIVE_REVIEW') {
        return 'DEFERRED: Sensitive operation is exposed through GET. Confirm read-only intent or migrate the mutation to an unsafe explicit method.'
    }
    if ($Row.PermissionCandidate -match '(^internal\.unclassified\.|\.method-review$|\.sensitive-review$)') {
        return 'DEFERRED: Candidate capability is a review placeholder and cannot be released as an active authorization capability.'
    }
    return "APPROVED: Exact source mapping classified as $FinalZone with the named capability; implementation must satisfy the recorded authentication, scope, and control policies before release."
}
function Get-ImplementationEvidenceStatus {
    param([string]$DecisionStatus)
    switch ($DecisionStatus) {
        'IMPLEMENTED' { return 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF' }
        'APPROVED' { return 'POLICY_APPROVED_IMPLEMENTATION_PENDING' }
        'DEFERRED' { return 'SOURCE_GAP_OR_POLICY_BLOCKER_REQUIRES_REVIEW' }
        default { throw "Unexpected decision status '$DecisionStatus'." }
    }
}

function Get-TestIds {
    param([object]$Row, [string]$DecisionStatus)
    if (Test-DestructiveGetContainmentImplemented -Row $Row) {
        return 'DestructiveGetRouteContainmentContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if (Test-ExplicitMutationContainmentImplemented -Row $Row) {
        return 'ExplicitMutationRouteContainmentContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if (Test-PasswordRecoveryContainmentImplemented -Row $Row) {
        return 'PasswordRecoveryRouteContainmentContractTest;PasswordResetLifecycleContractTest;PasswordResetServiceTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if (Test-DisabledLegacyPasswordFlowImplemented -Row $Row) {
        return 'ChangePasswordControllerSecurityTest;ActionStyleGetContainmentContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if (Test-VendorEmailVerificationContainmentImplemented -Row $Row) {
        return 'ActionStyleGetContainmentContractTest;VendorVerificationsServiceTest;VendorVerificationTokenLoggingContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if (Test-ReviewedSensitiveReadOnlyGet -Row $Row) {
        return 'SensitiveReadOnlyGetDecisionContractTest;SECURITY-LEDGER-VALIDATION;MODULE_AUTHORIZATION_AND_SCOPE_TEST_REQUIRED'
    }
    if ($Row.Path -match '^/users/login-history(?:/|$)') {
        return 'UsersControllerSecurityTest;SessionAdministrationServiceMethodSecurityTest;LoginHistoryTemplateSecurityContractTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if ($Row.Controller -eq 'AdminVendorUsersController') {
        return 'AdminVendorUsersControllerSecurityTest;AdminVendorIamTemplateSecurityContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if ($Row.Path -match '^/vendor-users(?:/|$)') {
        return 'VendorStaffIamControllerSecurityTest;VendorStaffAdministrationServiceMethodSecurityTest;VendorStaffIamTemplateSecurityContractTest;SecurityConfigCsrfCoverageTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if ($Row.ReviewStatus -eq 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF') {
        return 'RoleControllerSecurityTest;IamAdministrationServiceMethodSecurityTest;RuntimeEndpointInventoryExportTest;SECURITY-LEDGER-VALIDATION'
    }
    if ($DecisionStatus -eq 'DEFERRED') {
        return 'SECURITY-LEDGER-VALIDATION;IMPLEMENTATION_TEST_REQUIRED'
    }
    return 'SECURITY-LEDGER-VALIDATION;MODULE_AUTHORIZATION_AND_SCOPE_TEST_REQUIRED'
}
function Get-EvidenceReferences {
    param([object]$Row)
    $references = @(
        'docs/security/application-security-endpoint-inventory.csv',
        'main/java/com/ecommerce/app/SecurityConfig.java',
        ('main/java/' + $Row.SourceFile + ':' + $Row.SourceLine)
    )
    if (Test-ReviewedSensitiveReadOnlyGet -Row $Row) {
        $references += @(
            'main/java/com/ecommerce/app/admin/controller/AdminController.java',
            'main/java/com/ecommerce/app/admincustomer/controller/AdminCustomerController.java',
            'main/java/com/ecommerce/app/adminvendor/controller/AdminFinanceReportController.java',
            'main/java/com/ecommerce/app/module/blog/controller/AdminBlogController.java',
            'main/java/com/ecommerce/app/module/customer/controller/CustomerOrderController.java',
            'main/java/com/ecommerce/app/module/user/controller/ForgotPasswordController.java',
            'main/java/com/ecommerce/app/module/user/controller/UsersController.java',
            'main/java/com/ecommerce/app/product/controller/AdminStockController.java',
            'main/java/com/ecommerce/app/product/controller/CatalogVariantController.java',
            'main/java/com/ecommerce/app/publics/controller/PublicController.java',
            'main/java/com/ecommerce/app/vendor/controller/VendorPayoutController.java',
            'main/java/com/ecommerce/app/vendor/controller/VendorSalesOrderController.java',
            'main/java/com/ecommerce/app/vendor/controller/VendorVerificationsController.java',
            'test/java/com/ecommerce/app/SensitiveReadOnlyGetDecisionContractTest.java'
        )
    }
    if ($Row.ReviewStatus -eq 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF') {
        if (Test-DestructiveGetContainmentImplemented -Row $Row) {
            $references += @(
                'main/resources/templates/admin/vendor/admin_vendor_list.html',
                'main/resources/templates/vendor/payoutmethod/list.html',
                'main/resources/templates/ads/ads_list.html',
                'main/resources/templates/product/attribute/list.html',
                'main/resources/templates/product/attribute/options.html',
                'main/resources/templates/product/attribute/category_mappings.html',
                'main/resources/templates/product/manufacturer/list.html',
                'main/resources/templates/product/unit/list.html',
                'test/java/com/ecommerce/app/DestructiveGetRouteContainmentContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif (Test-ExplicitMutationContainmentImplemented -Row $Row) {
            $references += @(
                'main/resources/templates/product/product_details.html',
                'main/resources/templates/product/productcategory/add.html',
                'main/resources/templates/product/productcategory/productcategory_details.html',
                'main/resources/templates/product/manufacturer/manufacturer_details.html',
                'main/resources/templates/vendor/product/add.html',
                'main/resources/templates/vendor/product/product_details.html',
                'main/resources/templates/vendor/profile/vendor_profile_create.html',
                'main/resources/templates/customer/vendor_profile_create.html',
                'main/resources/templates/vendor/payout/payout_form.html',
                'main/resources/templates/frontview/contactUs.html',
                'test/java/com/ecommerce/app/ExplicitMutationRouteContainmentContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif (Test-PasswordRecoveryContainmentImplemented -Row $Row) {
            $references += @(
                'main/java/com/ecommerce/app/module/user/controller/ForgotPasswordController.java',
                'main/java/com/ecommerce/app/module/user/services/PasswordResetService.java',
                'main/java/com/ecommerce/app/module/user/services/PasswordResetEmailSender.java',
                'main/java/com/ecommerce/app/module/user/model/PasswordResetToken.java',
                'main/java/com/ecommerce/app/module/user/ripository/PasswordResetTokenRepository.java',
                'main/java/com/ecommerce/app/publics/controller/PublicController.java',
                'main/resources/templates/user/forgotpassword.html',
                'main/resources/templates/frontview/forgot-password.html',
                'main/resources/templates/frontview/reset-password.html',
                'main/resources/db/migration/mysql/V202608300001__password_reset_tokens.sql',
                'test/java/com/ecommerce/app/PasswordRecoveryRouteContainmentContractTest.java',
                'test/java/com/ecommerce/app/PasswordResetLifecycleContractTest.java',
                'test/java/com/ecommerce/app/module/user/services/PasswordResetServiceTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif (Test-DisabledLegacyPasswordFlowImplemented -Row $Row) {
            $references += @(
                'main/java/com/ecommerce/app/module/user/controller/ChangePasswordController.java',
                'test/java/com/ecommerce/app/module/user/controller/ChangePasswordControllerSecurityTest.java',
                'test/java/com/ecommerce/app/ActionStyleGetContainmentContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif (Test-VendorEmailVerificationContainmentImplemented -Row $Row) {
            $references += @(
                'main/java/com/ecommerce/app/vendor/controller/VendorVerificationsController.java',
                'main/java/com/ecommerce/app/vendor/services/VendorVerificationsService.java',
                'main/resources/templates/vendor/verifications/confirm_email.html',
                'test/java/com/ecommerce/app/ActionStyleGetContainmentContractTest.java',
                'test/java/com/ecommerce/app/vendor/services/VendorVerificationsServiceTest.java',
                'test/java/com/ecommerce/app/VendorVerificationTokenLoggingContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif ($Row.Path -match '^/users/login-history(?:/|$)') {
            $references += @(
                'main/java/com/ecommerce/app/module/user/services/SessionAdministrationService.java',
                'main/resources/templates/user/login_history.html',
                'test/java/com/ecommerce/app/module/user/controller/UsersControllerSecurityTest.java',
                'test/java/com/ecommerce/app/module/user/services/SessionAdministrationServiceMethodSecurityTest.java',
                'test/java/com/ecommerce/app/LoginHistoryTemplateSecurityContractTest.java'
            )
        } elseif ($Row.Controller -eq 'AdminVendorUsersController') {
            $references += @(
                'main/java/com/ecommerce/app/adminvendor/services/AdminVendorIamService.java',
                'test/java/com/ecommerce/app/adminvendor/controller/AdminVendorUsersControllerSecurityTest.java',
                'test/java/com/ecommerce/app/AdminVendorIamTemplateSecurityContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } elseif ($Row.Path -match '^/vendor-users(?:/|$)') {
            $references += @(
                'main/java/com/ecommerce/app/vendor/user/services/VendorStaffAdministrationService.java',
                'main/java/com/ecommerce/app/vendor/user/repository/UserVendorRoleRepository.java',
                'main/resources/templates/vendor/users/vendor_users_list.html',
                'main/resources/templates/vendor/users/vendor_users_form.html',
                'main/resources/templates/vendor/users/vendor_role_manage_list.html',
                'main/resources/templates/vendor/users/vendor_role_manage_form.html',
                'test/java/com/ecommerce/app/vendor/user/controller/VendorStaffIamControllerSecurityTest.java',
                'test/java/com/ecommerce/app/vendor/user/services/VendorStaffAdministrationServiceMethodSecurityTest.java',
                'test/java/com/ecommerce/app/VendorStaffIamTemplateSecurityContractTest.java',
                'test/java/com/ecommerce/app/SecurityConfigCsrfCoverageTest.java'
            )
        } else {
            $references += @(
                'test/java/com/ecommerce/app/module/user/controller/RoleControllerSecurityTest.java',
                'test/java/com/ecommerce/app/module/user/services/IamAdministrationServiceMethodSecurityTest.java'
            )
        }
    }
    return $references -join ';'
}
function Get-AssignmentPolicy {
    param([string]$Namespace, [string]$PermissionCandidate)
    if ($PermissionCandidate -eq 'platform.iam.protected.manage') {
        return 'PROTECTED_BREAK_GLASS_ROLE_ONLY'
    }
    switch ($Namespace) {
        'platform' { return 'PLATFORM_ROLE_ASSIGNABLE_ONLY_WITH_GRANT_AUTHORITY' }
        'vendor' { return 'VENDOR_ROLE_ASSIGNABLE_ONLY_WITH_MEMBERSHIP_AND_GRANT_CEILING' }
        'customer' { return 'NON_ASSIGNABLE_CUSTOMER_SELF_SERVICE_POLICY' }
        'public' { return 'NON_ASSIGNABLE_PUBLIC_INGRESS_POLICY' }
        'api' { return 'NON_ASSIGNABLE_API_CLIENT_SCOPE_POLICY' }
        'webhook' { return 'NON_ASSIGNABLE_WEBHOOK_INGRESS_POLICY' }
        'internal' { return 'NON_ASSIGNABLE_INTERNAL_SERVICE_POLICY' }
        default { throw "Unsupported permission namespace '$Namespace'." }
    }
}

function Get-PermissionDecisionStatus {
    param([object]$CatalogueRow, [object[]]$LinkedEndpointDecisions)
    $candidate = $CatalogueRow.PermissionCandidate
    if ($candidate -match '(^internal\.unclassified\.|\.method-review$|\.sensitive-review$)') {
        return 'DEFERRED'
    }
    if ($CatalogueRow.Namespace -in @('api', 'webhook') -and @($LinkedEndpointDecisions | Where-Object DecisionStatus -ne 'IMPLEMENTED').Count -gt 0) {
        return 'DEFERRED'
    }
    if ($CatalogueRow.ZoneCandidates -match 'PUBLIC_REVIEW_REQUIRED') {
        return 'DEFERRED'
    }
    if (@($LinkedEndpointDecisions | Where-Object DecisionStatus -eq 'IMPLEMENTED').Count -eq $LinkedEndpointDecisions.Count) {
        return 'IMPLEMENTED'
    }
    return 'APPROVED'
}

function Get-PermissionDecisionRationale {
    param([string]$DecisionStatus, [string]$PermissionCandidate)
    switch ($DecisionStatus) {
        'IMPLEMENTED' {
            if ($PermissionCandidate -eq 'platform.security.audit.read') {
                return 'IMPLEMENTED: Security-audit capability has source controller and service security evidence with raw session identifiers removed from the admin view; migration and runtime proof remain outstanding.'
            }
            if ($PermissionCandidate -match '^platform\.vendor\.management\.') {
                return 'IMPLEMENTED: Platform vendor-IAM capability has source controller and service security evidence; migration and runtime proof remain outstanding.'
            }
            if ($PermissionCandidate -match '^vendor\.(staff|role)\.manage$') {
                return 'IMPLEMENTED: Vendor staff-IAM capability has source controller, service, vendor-scope, grant-ceiling, and CSRF evidence; invitation lifecycle, revocation/session invalidation, and runtime proof remain outstanding.'
            }
            if ($PermissionCandidate -match '\.delete$') {
                return 'IMPLEMENTED: Destructive delete endpoint capability has source route and template evidence confirming POST-only browser submission with CSRF; migration and runtime proof remain outstanding.'
            }
            return 'IMPLEMENTED: Existing platform-IAM capability has source controller and service security evidence; migration and runtime proof remain outstanding.'
        }
        'DEFERRED' { return 'DEFERRED: This catalogue candidate is blocked by an unresolved endpoint method, zone, capability, API, webhook, or public-policy review.' }
        'APPROVED' { return 'APPROVED: This immutable namespaced capability is the Phase 1 policy target; code, migration, and runtime evidence remain separate release gates.' }
        default { throw "Unexpected permission decision status '$DecisionStatus'." }
    }
}
$expectedInventoryHeaders = @(
    'SourceFile', 'SourceLine', 'Package', 'Controller', 'JavaMethod', 'HttpMethod', 'Path',
    'CurrentUrlRule', 'CurrentMethodGuard', 'TargetZoneCandidate', 'ModuleCandidate',
    'ActionCandidate', 'PermissionCandidate', 'RequiredScopeCandidate', 'ReviewStatus'
)
$expectedCatalogueHeaders = @(
    'PermissionCandidate', 'Namespace', 'ModuleCandidates', 'ActionCandidates', 'ZoneCandidates',
    'EndpointCount', 'AssignmentPolicyCandidate', 'StepUpCandidate', 'LifecycleCandidate', 'ReviewStatus'
)
$resolvedInventoryPath = (Resolve-Path -LiteralPath $InventoryPath).Path
$resolvedCataloguePath = (Resolve-Path -LiteralPath $PermissionCataloguePath).Path
$inventoryRows = @(Import-Csv -LiteralPath $resolvedInventoryPath)
$catalogueRows = @(Import-Csv -LiteralPath $resolvedCataloguePath)
Assert-Headers -Rows $inventoryRows -ExpectedHeaders $expectedInventoryHeaders -ArtifactName 'Endpoint security inventory'
Assert-Headers -Rows $catalogueRows -ExpectedHeaders $expectedCatalogueHeaders -ArtifactName 'Permission catalogue'

$inventoryHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $resolvedInventoryPath).Hash
$catalogueHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $resolvedCataloguePath).Hash
$endpointDecisionRows = [System.Collections.Generic.List[object]]::new()
foreach ($row in $inventoryRows) {
    $finalZone = Get-FinalZone -CandidateZone $row.TargetZoneCandidate
    $decisionStatus = Get-DecisionStatus -Row $row -FinalZone $finalZone
    $finalHttpMethod = if ($row.HttpMethod -eq 'ALL') { 'METHOD_SPLIT_REQUIRED' } else { $row.HttpMethod }
    $decisionKey = 'EP-' + (Get-Sha256 -Value (@($row.Package, $row.Controller, $row.JavaMethod, $row.HttpMethod, $row.Path) -join [char]31))
    $endpointDecisionRows.Add([PSCustomObject][ordered]@{
            DecisionSchemaVersion = $decisionSchemaVersion
            DecisionKey = $decisionKey
            SourceCandidateFingerprint = Get-RowFingerprint -Row $row
            SourceInventorySha256 = $inventoryHash
            SourceFile = $row.SourceFile
            SourceLine = $row.SourceLine
            Package = $row.Package
            Controller = $row.Controller
            JavaMethod = $row.JavaMethod
            SourceHttpMethod = $row.HttpMethod
            Path = $row.Path
            CurrentUrlRule = $row.CurrentUrlRule
            CurrentMethodGuard = $row.CurrentMethodGuard
            TargetZoneCandidate = $row.TargetZoneCandidate
            ModuleCandidate = $row.ModuleCandidate
            ActionCandidate = $row.ActionCandidate
            PermissionCandidate = $row.PermissionCandidate
            RequiredScopeCandidate = $row.RequiredScopeCandidate
            FinalZone = $finalZone
            FinalHttpMethod = $finalHttpMethod
            RequiredAuthenticationMechanism = Get-RequiredAuthentication -FinalZone $finalZone -Action $row.ActionCandidate
            FinalCapability = $row.PermissionCandidate
            OwnershipScopeRule = Get-OwnershipScopeRule -FinalZone $finalZone -SourceZone $row.TargetZoneCandidate
            RepositoryScopeMethod = Get-RepositoryScopeMethod -FinalZone $finalZone
            CsrfSignatureReplayRule = Get-CsrfSignatureReplayRule -FinalZone $finalZone -FinalHttpMethod $finalHttpMethod
            RateLimitPolicy = Get-RateLimitPolicy -FinalZone $finalZone -FinalHttpMethod $finalHttpMethod -Action $row.ActionCandidate
            IdempotencyPolicy = Get-IdempotencyPolicy -FinalZone $finalZone -FinalHttpMethod $finalHttpMethod
            AuditEventRequirement = Get-AuditEventRequirement -FinalZone $finalZone -Module $row.ModuleCandidate -Action $row.ActionCandidate
            TestIds = Get-TestIds -Row $row -DecisionStatus $decisionStatus
            DecisionStatus = $decisionStatus
            ImplementationEvidenceStatus = Get-ImplementationEvidenceStatus -DecisionStatus $decisionStatus
            DecisionRationale = Get-DecisionRationale -Row $row -FinalZone $finalZone -DecisionStatus $decisionStatus
            EvidenceReferences = Get-EvidenceReferences -Row $row
            DecisionRevision = '1'
            Reviewer = $Reviewer
            ReviewedAtUtc = $reviewedAt
        })
}
$endpointDecisionRows = @($endpointDecisionRows | Sort-Object Package, Controller, Path, SourceHttpMethod, JavaMethod)

$permissionDecisionRows = [System.Collections.Generic.List[object]]::new()
foreach ($catalogueRow in $catalogueRows) {
    $linkedEndpointDecisions = @($endpointDecisionRows | Where-Object FinalCapability -eq $catalogueRow.PermissionCandidate)
    if ($linkedEndpointDecisions.Count -ne [int]$catalogueRow.EndpointCount) {
        throw "Permission '$($catalogueRow.PermissionCandidate)' links to $($linkedEndpointDecisions.Count) endpoint decisions but catalogue records $($catalogueRow.EndpointCount)."
    }
    $permissionDecisionStatus = Get-PermissionDecisionStatus -CatalogueRow $catalogueRow -LinkedEndpointDecisions $linkedEndpointDecisions
    $candidateFingerprint = Get-Sha256 -Value (@(
            $catalogueRow.PermissionCandidate, $catalogueRow.Namespace, $catalogueRow.ModuleCandidates,
            $catalogueRow.ActionCandidates, $catalogueRow.ZoneCandidates, $catalogueRow.EndpointCount,
            $catalogueRow.AssignmentPolicyCandidate, $catalogueRow.StepUpCandidate,
            $catalogueRow.LifecycleCandidate, $catalogueRow.ReviewStatus
        ) -join [char]31)
    $implementationEvidenceStatus = switch ($permissionDecisionStatus) {
        'IMPLEMENTED' { 'SOURCE_IMPLEMENTED_PENDING_MIGRATION_AND_RUNTIME_PROOF' }
        'APPROVED' { 'POLICY_APPROVED_IMPLEMENTATION_PENDING' }
        'DEFERRED' { 'CATALOGUE_OR_ENDPOINT_BLOCKER_REQUIRES_REVIEW' }
        default { throw "Unexpected permission decision status '$permissionDecisionStatus'." }
    }
    $permissionDecisionRows.Add([PSCustomObject][ordered]@{
            DecisionSchemaVersion = $decisionSchemaVersion
            PermissionCandidate = $catalogueRow.PermissionCandidate
            CandidateFingerprint = $candidateFingerprint
            SourcePermissionCatalogueSha256 = $catalogueHash
            FinalCapability = $catalogueRow.PermissionCandidate
            Namespace = $catalogueRow.Namespace
            ModuleCandidates = $catalogueRow.ModuleCandidates
            ActionCandidates = $catalogueRow.ActionCandidates
            ZoneCandidates = $catalogueRow.ZoneCandidates
            EndpointCount = $catalogueRow.EndpointCount
            FinalAssignmentPolicy = Get-AssignmentPolicy -Namespace $catalogueRow.Namespace -PermissionCandidate $catalogueRow.PermissionCandidate
            StepUpRequirement = if ($catalogueRow.ActionCandidates -match 'SENSITIVE_REVIEW') { 'MFA_AND_STEP_UP_REQUIRED_OR_DOCUMENTED_EXCEPTION' } else { 'STEP_UP_NOT_DEFAULT_REVIEW_IF_HIGH_IMPACT' }
            LifecyclePolicy = 'VERSIONED_IMMUTABLE_CATALOGUE_RECORD'
            DecisionStatus = $permissionDecisionStatus
            ImplementationEvidenceStatus = $implementationEvidenceStatus
            DecisionRationale = Get-PermissionDecisionRationale -DecisionStatus $permissionDecisionStatus -PermissionCandidate $catalogueRow.PermissionCandidate
            EvidenceReferences = 'docs/security/application-security-permission-catalogue.csv;docs/security/application-security-endpoint-decision-ledger.csv;docs/security/application-security-authentication-authorization-workflow.md'
            DecisionRevision = '1'
            Reviewer = $Reviewer
            ReviewedAtUtc = $reviewedAt
        })
}
$permissionDecisionRows = @($permissionDecisionRows | Sort-Object FinalCapability)

if ($endpointDecisionRows.Count -ne $inventoryRows.Count) {
    throw "Endpoint decision count mismatch: $($endpointDecisionRows.Count) decisions for $($inventoryRows.Count) source rows."
}
if ((@($endpointDecisionRows | Group-Object DecisionKey | Where-Object Count -ne 1)).Count -gt 0) {
    throw 'Endpoint decision ledger has duplicate decision keys.'
}
if ((@($endpointDecisionRows | Where-Object { $allowedZones -notcontains $_.FinalZone })).Count -gt 0) {
    throw 'Endpoint decision ledger contains an unsupported final zone.'
}
if ((@($endpointDecisionRows | Where-Object { $allowedDecisionStatuses -notcontains $_.DecisionStatus })).Count -gt 0) {
    throw 'Endpoint decision ledger contains an unsupported decision status.'
}
foreach ($decision in $endpointDecisionRows) {
    $methodIsExact = $allowedExactMethods -contains $decision.FinalHttpMethod
    $methodIsDeferred = $decision.FinalHttpMethod -eq 'METHOD_SPLIT_REQUIRED' -and $decision.DecisionStatus -eq 'DEFERRED'
    if (-not ($methodIsExact -or $methodIsDeferred)) {
        throw "Decision '$($decision.DecisionKey)' has an invalid final HTTP method '$($decision.FinalHttpMethod)'."
    }
    if ($decision.FinalHttpMethod -eq 'ALL') {
        throw "Decision '$($decision.DecisionKey)' must not use ALL as a final HTTP method."
    }
    if ($decision.DecisionStatus -in @('APPROVED', 'IMPLEMENTED', 'VERIFIED') -and -not $methodIsExact) {
        throw "Release-eligible decision '$($decision.DecisionKey)' lacks an exact final HTTP method."
    }
    if ($decision.DecisionStatus -eq 'DEFERRED' -and $decision.DecisionRationale -notmatch '^DEFERRED:') {
        throw "Deferred decision '$($decision.DecisionKey)' lacks a documented blocker."
    }
}

$permissionByCapability = @{}
foreach ($permissionDecision in $permissionDecisionRows) {
    if ($permissionByCapability.ContainsKey($permissionDecision.FinalCapability)) {
        throw "Permission decision ledger has a duplicate capability '$($permissionDecision.FinalCapability)'."
    }
    $permissionByCapability[$permissionDecision.FinalCapability] = $permissionDecision
}
foreach ($decision in $endpointDecisionRows) {
    if (-not $permissionByCapability.ContainsKey($decision.FinalCapability)) {
        throw "Endpoint decision '$($decision.DecisionKey)' references an unknown capability '$($decision.FinalCapability)'."
    }
    $permissionDecision = $permissionByCapability[$decision.FinalCapability]
    if ($decision.DecisionStatus -in @('APPROVED', 'IMPLEMENTED', 'VERIFIED') -and $permissionDecision.DecisionStatus -notin @('APPROVED', 'IMPLEMENTED', 'VERIFIED')) {
        throw "Release-eligible endpoint '$($decision.DecisionKey)' links to non-approved capability '$($decision.FinalCapability)'."
    }
    if ($decision.FinalZone -eq 'PUBLIC' -and ($permissionDecision.Namespace -ne 'public' -or $permissionDecision.FinalAssignmentPolicy -notmatch '^NON_ASSIGNABLE')) {
        throw "Public endpoint '$($decision.DecisionKey)' is not linked to a non-assignable public policy."
    }
    if ($decision.FinalZone -eq 'WEBHOOK' -and ($permissionDecision.Namespace -ne 'webhook' -or $permissionDecision.FinalAssignmentPolicy -notmatch '^NON_ASSIGNABLE')) {
        throw "Webhook endpoint '$($decision.DecisionKey)' is not linked to a non-assignable webhook policy."
    }
    if ($decision.FinalZone -eq 'INTERNAL_ONLY' -and ($permissionDecision.Namespace -ne 'internal' -or $permissionDecision.FinalAssignmentPolicy -notmatch '^NON_ASSIGNABLE')) {
        throw "Internal-only endpoint '$($decision.DecisionKey)' is not linked to a non-assignable internal policy."
    }
}

$endpointDecisionHeaders = @(
    'DecisionSchemaVersion', 'DecisionKey', 'SourceCandidateFingerprint', 'SourceInventorySha256',
    'SourceFile', 'SourceLine', 'Package', 'Controller', 'JavaMethod', 'SourceHttpMethod', 'Path',
    'CurrentUrlRule', 'CurrentMethodGuard', 'TargetZoneCandidate', 'ModuleCandidate', 'ActionCandidate',
    'PermissionCandidate', 'RequiredScopeCandidate', 'FinalZone', 'FinalHttpMethod',
    'RequiredAuthenticationMechanism', 'FinalCapability', 'OwnershipScopeRule', 'RepositoryScopeMethod',
    'CsrfSignatureReplayRule', 'RateLimitPolicy', 'IdempotencyPolicy', 'AuditEventRequirement', 'TestIds',
    'DecisionStatus', 'ImplementationEvidenceStatus', 'DecisionRationale', 'EvidenceReferences',
    'DecisionRevision', 'Reviewer', 'ReviewedAtUtc'
)
$permissionDecisionHeaders = @(
    'DecisionSchemaVersion', 'PermissionCandidate', 'CandidateFingerprint', 'SourcePermissionCatalogueSha256',
    'FinalCapability', 'Namespace', 'ModuleCandidates', 'ActionCandidates', 'ZoneCandidates', 'EndpointCount',
    'FinalAssignmentPolicy', 'StepUpRequirement', 'LifecyclePolicy', 'DecisionStatus',
    'ImplementationEvidenceStatus', 'DecisionRationale', 'EvidenceReferences', 'DecisionRevision',
    'Reviewer', 'ReviewedAtUtc'
)
$endpointRequiredValues = @($endpointDecisionHeaders | Where-Object { $_ -ne 'CurrentMethodGuard' })

$endpointExport = $null
$permissionExport = $null
try {
    $endpointExport = New-ValidatedCsvExport -Rows $endpointDecisionRows -DestinationPath $EndpointDecisionOutputPath -RequiredHeaders $endpointDecisionHeaders -RequiredValueHeaders $endpointRequiredValues -ArtifactName 'Endpoint decision ledger'
    $permissionExport = New-ValidatedCsvExport -Rows $permissionDecisionRows -DestinationPath $PermissionDecisionOutputPath -RequiredHeaders $permissionDecisionHeaders -RequiredValueHeaders $permissionDecisionHeaders -ArtifactName 'Permission decision ledger'
    Publish-ValidatedCsvExport -Export $endpointExport
    Publish-ValidatedCsvExport -Export $permissionExport
} finally {
    foreach ($pendingExport in @($endpointExport, $permissionExport)) {
        if ($null -ne $pendingExport -and (Test-Path -LiteralPath $pendingExport.TemporaryPath)) {
            [System.IO.File]::Delete($pendingExport.TemporaryPath)
        }
    }
}

$endpointDecisionRows | Group-Object DecisionStatus | Sort-Object Name | ForEach-Object { "endpoint_decision_status_$($_.Name.ToLowerInvariant())=$($_.Count)" }
$endpointDecisionRows | Group-Object FinalZone | Sort-Object Name | ForEach-Object { "endpoint_final_zone_$($_.Name.ToLowerInvariant())=$($_.Count)" }
$permissionDecisionRows | Group-Object DecisionStatus | Sort-Object Name | ForEach-Object { "permission_decision_status_$($_.Name.ToLowerInvariant())=$($_.Count)" }
"endpoint_decisions=$($endpointDecisionRows.Count)"
"permission_decisions=$($permissionDecisionRows.Count)"
"source_inventory_sha256=$inventoryHash"
"source_permission_catalogue_sha256=$catalogueHash"
"endpoint_decision_output=$((Resolve-Path -LiteralPath $EndpointDecisionOutputPath).Path)"
"endpoint_decision_sha256=$((Get-FileHash -Algorithm SHA256 -LiteralPath $EndpointDecisionOutputPath).Hash)"
"permission_decision_output=$((Resolve-Path -LiteralPath $PermissionDecisionOutputPath).Path)"
"permission_decision_sha256=$((Get-FileHash -Algorithm SHA256 -LiteralPath $PermissionDecisionOutputPath).Hash)"
