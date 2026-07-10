# Project Instructions for Codex

## Project Type

This is an enterprise-grade Java Spring Boot eCommerce / multi-vendor marketplace project.

## Technology Stack

* Java 17+
* Spring Boot 3.x
* Spring MVC
* Spring Data JPA / Hibernate
* Thymeleaf
* Bootstrap 5.3
* MySQL/PostgreSQL
* Maven
* Linux / Tomcat deployment

## Coding Rules

* Follow clean code, OOP, SOLID principles, enterprise Java standards,International Standard, .
* Do not create unnecessary complexity.
* Keep code readable, maintainable, and production-ready.
* Use meaningful class, method, variable, and package names.
* Do not break existing functionality.
* Before changing code, understand current project structure.
* Prefer fixing the root cause instead of temporary patches.

## Spring Boot Rules

* Use proper layer separation:

  * Controller
  * Service
  * Repository
  * Entity
  * DTO / Form object where needed
* Keep business logic inside service classes, not controllers.
* Use constructor injection where possible.
* Use `@Transactional` only in service layer when database write operations are involved.
* Validate input using Bean Validation annotations.
* Use proper error handling and clear validation messages.

## Entity and Database Rules

* PostgreSQL is the target database.
* Avoid reserved SQL keywords for column names.
* Use explicit `@Column` names where needed.
* Use `BigDecimal` for money.
* Use `LocalDate`, `LocalDateTime`, or `Instant` for dates.
* Avoid lazy loading problems in Thymeleaf pages.
* Do not rename database columns without explaining migration impact.
* For production changes, mention required SQL migration if needed.

## Thymeleaf Rules

* Use Bootstrap 5 compatible markup.
* Avoid deprecated Thymeleaf syntax.
* Keep templates clean and readable.
* Use safe null checks where needed.
* Do not put business logic inside templates.

## Security Rules

* Follow Spring Security best practices.
* Do not expose sensitive data in logs or pages.
* Do not disable CSRF unless there is a clear reason.
* Protect admin, vendor, and customer areas separately.

## Logging and Debugging

* When fixing an error, identify:

  1. Exact root cause
  2. File/class causing the issue
  3. Correct fix
  4. Any database or configuration change required
* Add useful logs only when necessary.
* Do not add excessive debug logs to production code.

## UI Rules

* Use Bootstrap 5.
* UI should be clean, responsive, enterprise-grade, and admin-friendly.
* Do not change existing design style unless requested.
* Use consistent spacing, alignment, badges, buttons, and tables.

## Output Style

When responding:

* Explain the problem clearly.
* Provide corrected code.
* Mention exactly where to place the code.
* If multiple files are affected, list them.
* If there is risk, mention it.
* Keep the answer direct and practical.


## Scalability Rules

Design and modify the project for long-term scalability.

### Application Scalability

* Keep modules loosely coupled.
* Avoid tightly connecting controller, service, repository, and entity logic.
* Use clear module boundaries such as:

  * User Management
  * Product Management
  * Vendor Management
  * Order Management
  * Payment
  * Shipping
  * Promotion
  * Inventory
  * Reporting
* New features should be easy to extend without changing many existing files.

### Database Scalability

* Design database changes carefully for high data volume.
* Add indexes for frequently searched, filtered, joined, or sorted columns.
* Avoid unnecessary eager loading.
* Prevent N+1 query problems.
* Use pagination for large lists.
* Avoid loading all records into memory.
* Use proper transaction boundaries.
* Use database constraints for important business rules.

### Code Scalability

* Prefer reusable services, helpers, validators, and components.
* Avoid duplicate logic.
* Keep methods small and focused.
* Use interfaces where future multiple implementations may be needed.
* Do not over-engineer simple features.
* Make code easy to test and maintain.

### Enterprise Architecture

* Design features so they can later support:

  * Multi-vendor marketplace
  * Multiple warehouses
  * Multiple payment gateways
  * Multiple shipping carriers
  * Multiple countries
  * Multiple currencies
  * High traffic
  * API/mobile app support
* Do not hardcode business rules that may change later.
* Use configuration, enums, database rules, or strategy pattern where appropriate.

### Performance Rules

* Always consider query performance.
* Use pagination in admin and customer listing pages.
* Use caching only when data is read frequently and does not change often.
* Avoid unnecessary database calls inside loops.
* Avoid large session objects.
* Avoid storing heavy entity graphs in session.

### Future Microservice Readiness

* Keep business logic clean enough so modules can later be separated into services.
* Use clear service boundaries.
* Avoid direct dependency between unrelated modules.
* For important operations, consider future support for:

  * Event-driven architecture
  * Outbox pattern
  * Idempotency key
  * Audit log
  * Retry-safe processing
  * Distributed transaction safety

### Production Readiness

* Code must be safe for live server deployment.
* Handle errors gracefully.
* Do not expose stack traces to users.
* Log enough information to debug production issues.
* Mention possible migration, rollback, or deployment impact when changing database structure.


## Robustness Rules

Build and modify the project to be robust, stable, and production-safe.

### Error Handling

* Handle expected errors gracefully.
* Do not allow unhandled exceptions to expose stack traces to users.
* Use clear validation messages for user input errors.
* Use proper exception handling in service and controller layers.
* Return meaningful error responses for API endpoints.
* For web pages, show user-friendly error messages using redirect attributes or model attributes.

### Data Integrity

* Validate all important input before saving.
* Use Bean Validation annotations where applicable.
* Use database constraints for critical rules.
* Prevent duplicate records where uniqueness is required.
* Avoid saving incomplete or inconsistent business data.
* Use transactions for multi-step database write operations.

### Transaction Safety

* Use `@Transactional` in the service layer for write operations.
* Avoid catching exceptions inside a transaction without handling rollback correctly.
* Do not silently ignore failed database operations.
* For payment, order, wallet, inventory, coupon, and commission logic, ensure rollback safety.
* For retryable operations, design idempotent behavior where possible.

### Null Safety

* Check nullable values before use.
* Avoid `NullPointerException`.
* Use `Optional` carefully in service/repository logic.
* Do not assume session, request parameter, uploaded file, entity relation, or database value is always present.
* Add fallback behavior only when it is business-safe.

### File Upload Safety

* Validate uploaded file size, type, and extension.
* Do not trust original filenames.
* Generate safe server-side filenames.
* Prevent overwriting existing files accidentally.
* Handle missing upload directories.
* Log upload failures clearly.
* Keep old files if new upload fails.

### Security Robustness

* Do not trust client-side validation only.
* Validate all server-side inputs.
* Protect admin, vendor, and customer operations separately.
* Do not expose internal IDs unnecessarily where UUID/business ID should be used.
* Prevent SQL injection through proper JPA/repository usage.
* Prevent XSS by escaping user-provided content in views.
* Do not log passwords, tokens, API keys, or sensitive personal data.

### Production Stability

* Code must work safely on live Linux/Tomcat deployment.
* Avoid hardcoded local Windows paths.
* Use configuration properties for paths, URLs, keys, limits, and environment-specific values.
* Handle missing configuration clearly.
* Add useful logs for production debugging.
* Avoid excessive logs in normal successful operations.

### External Service Robustness

* For payment gateway, SMS, email, shipping API, and AI API integrations:

  * Handle timeout
  * Handle failed response
  * Handle duplicate callback
  * Handle retry safely
  * Log request/reference IDs
  * Never assume external service always succeeds

### Concurrency Safety

* Protect stock, wallet balance, reward points, coupon redemption, and payment confirmation from race conditions.
* Use database locking, optimistic locking, or unique constraints where needed.
* Prevent double order placement, double payment processing, and double coupon use.

### Recovery and Debugging

* When fixing a bug, identify the root cause before changing code.
* Explain what caused the error.
* Explain why the fix is safe.
* Mention any database migration or configuration change required.
* Do not apply temporary fixes that hide the real problem.





