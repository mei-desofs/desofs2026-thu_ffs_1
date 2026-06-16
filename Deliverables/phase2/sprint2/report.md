# Phase 2: Sprint 2

---

### Table of Contents

- [Introduction](#introduction)
- [Test Planning](#test-planning)
- [Conclusion](#conclusion)

---

## Introduction

This document covers the security engineering practices adopted during the development of the **Cantinas de Cinfães** backend — a Spring Boot REST API for managing canteen operations, including user authentication, meal scheduling, and email notifications.

The project follows a **shift-left security** approach, integrating security checks throughout the development lifecycle via three automated GitHub Actions pipelines: one for commits, one for Pull Requests, and one for Releases. Practices include SAST, SCA, DAST, secret scanning, artifact scanning, SBOM generation, and automated testing, all traceable to security requirements defined using the OWASP ASVS.

---

## Test Planning

This document maps each implemented or in-progress ASVS 5.0 control to its corresponding test plan, organized by functional area. Controls with status **Compliant** have a passing test procedure. Controls marked **Not Applicable** are accompanied by a justification based on the application's architecture (confirmed via `pom.xml` and source code review). Controls marked **In Progress** have a defined test plan pending full implementation.

---

## 2.1. Authentication

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V1.1.1** | L2 | Input is decoded to canonical form only once, before processing or validation. | **Test:** Inject double-encoded payloads (e.g., `%2527` for `%27`) into the login fields (`email`, `password`). Verify the application decodes input only once and does not re-decode after validation, rejecting the malicious input at a single point. |
| **V1.1.2** | L2 | Output encoding and escaping are performed as the final step before the interpreter consumes the data. | **Test:** Submit values containing `"`, `\`, and `\n` in `email`/`deviceId` fields during login. Inspect the JSON response (`LoginResponse`) via `curl -i` and confirm Jackson serialization escapes all special characters, producing a valid, well-formed JSON document. |
| **V2.1.1** | L1 | Documentation defines input validation rules for expected data structures (e.g., email, NIF, phone). | **Test:** Review the `LoginDTO`/`RegisterDTO` Bean Validation annotations (`@Email`, `@NotBlank`, `@Pattern`). Submit malformed values (invalid email format, non-numeric NIF) and confirm the API returns `400 Bad Request` with field-level validation errors. |
| **V2.1.3** | L2 | Business logic limits and validations are documented (per-user and global). | **Test:** Review documentation for limits such as the 3-failed-login lockout threshold (5-minute lockout) and the 20-minute session inactivity timeout. Confirm these values are enforced consistently and match the documented thresholds. |
| **V2.2.1** | L1 | Input is validated against an allowlist/expected structure for business and security decisions. | **Test:** Submit a login request with `role` or `tokenVersion` fields injected into the request body (fields not part of `LoginDTO`). Verify the API ignores/rejects unexpected fields and does not allow privilege escalation via extra body parameters. |
| **V2.2.2** | L1 | Input validation is enforced at the trusted server-side layer; client-side validation is not relied upon. | **Test:** Bypass any client and send a direct `POST /api/auth/login` request via Postman with a password that violates the policy (e.g., `"123"`). Verify the server independently rejects it with `400 Bad Request`, confirming validation does not depend on a frontend. |
| **V1.2.2** | L1 | Output encoding for dynamically built URLs uses safe encoding; only safe protocols (`https`/`http`) are permitted. | **Test (Authentication):** Trigger the password reset / supplier-approval email flow. Verify the `resetLink`/activation URL is built from a hardcoded base URL + server-generated token (never from user input), is properly URL-encoded, and uses `https`/`http` only. |
| **V1.2.3** | L1 | Output encoding/escaping is used when dynamically building JSON to avoid JSON injection. | **Test (Menu Tampering):** Submit a value containing `"`, `\`, `\n` in any text field returned by a `@RestController`/`ResponseEntity` endpoint. Confirm Jackson serializes the response as valid JSON, with no structural injection possible. |
| **V1.2.4** | L1 | Database queries use parameterized queries/ORM, protected from SQL Injection. | **Test (Auth Bypass via SQL Injection):** Submit SQL injection strings (e.g., `' OR '1'='1`) to the login endpoint. Validate that the API rejects the request with `401 Unauthorized` and does not expose database errors. |
| **V1.3.11** | L2 | User input is sanitized before being passed to mail systems (SMTP/IMAP injection protection). | **Test (SMTP/IMAP Injection):** Submit a `deviceId`/email value containing `\r\n` and forged headers (e.g., `\r\nBcc: attacker@evil.com`). Send via the configured MailTrap sandbox and confirm the resulting email has no injected headers/recipients. |
| **V6.1.1** | L1 | Anti-automation / account lockout after repeated failed login attempts. | **Test (Brute Force / Credential Stuffing):** Trigger multiple rapid, sequential login requests with an incorrect password. Validate that the API blocks the account with `429 Too Many Requests` after the 3rd failed attempt, and that `sendLockNotification` is triggered. |
| **V6.1.2** | L2 | Deny-list of context-specific words (organization name, etc.) enforced in passwords. | **Test (Weak Passwords – Context-Specific Words):** Attempt to create or change a password to a term included in the context-specific deny-list (e.g., `BioCantinas123`). The system must reject the submission with a policy error. |
| **V6.1.3** | L2 | Anti-automation controls apply consistently across authentication endpoints. | **Test:** Repeat the V6.1.1 lockout test against the password-reset and supplier-registration endpoints. Confirm rate limiting/lockout behaviour is consistent across all authentication-related endpoints. |
| **V6.2.1** | L1 | Minimum password length/complexity policy enforced. | **Test (Insecure Password Creation):** Attempt to submit a weak/short password (e.g., `12345`, `qwerty`). The system must block creation and mandate the policy (10-char minimum, uppercase, number, special character). |
| **V6.2.2 / V6.2.3 / V6.2.4 / V6.2.6 / V6.2.7** | L1 | Password length limits, Unicode normalization, no truncation, and re-authentication for sensitive changes. | **Test:** Submit passwords at the documented minimum/maximum boundary lengths (10 and 128 chars), including Unicode characters (e.g., `ã`, `ç`). Confirm correct acceptance/rejection without silent truncation. Confirm password-change requires re-entry of the current password. |
| **V6.2.8** | L1 | Password comparison performed using a constant-time, secure mechanism. | **Test:** Review `AuthenticationService.login()` — confirms use of `passwordEncoder.matches()` (BCrypt). Verify no custom `String.equals()` comparison of password hashes exists anywhere in the codebase. |
| **V6.2.11 / V6.2.12** | L2 | Secure password storage (hashing algorithm, salt, work factor). | **Test:** Inspect a stored `User.password` value in the database — confirm it is a BCrypt hash (`$2a$...`/`$2b$...`), never plaintext, with an adequate work factor (≥10). |
| **V6.3.1 / V6.3.3 / V6.3.4 / V6.3.5 / V6.3.6 / V6.3.7** | L1–L3 | Secure credential recovery flow (token-based, time-limited, single-use). | **Test:** Trigger the password-reset flow. Verify the reset email contains a server-generated token valid for 20 minutes (REQ2.3), that the token is single-use, and that the link does not leak the user's identity to third parties. |
| **V6.4.1** | L1 | Knowledge-based authentication (security questions/hints) eliminated. | **Test (Knowledge-Based Auth Bypass):** Architecturally validate the complete absence of "security question"/"password hint" fields across registration, login, and recovery flows. Confirm the email-based secure token reset flow is the only recovery mechanism. |
| **V6.4.3** | L2 | Account recovery does not disclose whether an account exists (anti-enumeration). | **Test:** Submit a password-reset request for a non-existent email and for a valid email. Confirm both return an identical generic response (`"If this email exists, a reset link was sent"`), preventing email enumeration. |
| **V6.4.6** | L3 | Administrators can only trigger reset workflows, never set passwords directly. | **Test (Admin-Controlled Password Reset):** Access the admin panel/API and confirm there is no endpoint allowing an Admin to directly set another user's password — only `sendEmail`-based reset-link triggers exist. |
| **V6.7.1** | L3 | Session inactivity timeout implemented and documented. | **Test (Session Inactivity Timeout):** Leave a JWT idle beyond 20 minutes (`EXPIRATION_TIME` in `JwtService`). Submit a new API request using that token and validate it returns `401 Unauthorized`. |
| **V7.1.1 / V7.1.2** | L2 | Session inactivity timeout and absolute session lifetime are documented with justification. | **Test:** Review `JwtService.EXPIRATION_TIME` (20 minutes) and `User.refreshTokenExpiry` (7 days). Confirm these values are documented as the inactivity timeout and absolute session lifetime respectively, aligned with REQ1.6. |
| **V7.2.1** | L1 | Session token verification is performed by a trusted backend service. | **Test:** Send a tampered/expired JWT directly to a protected endpoint, bypassing any client. Verify the backend (OAuth2 Resource Server / `JwtDecoder`) independently validates the token and rejects it with `401 Unauthorized`. |
| **V7.2.2** | L1 | Dynamically generated, self-contained tokens (not static API keys). | **Test:** Inspect the token issued after login — confirm it is a signed JWT (`io.jsonwebtoken`) with `exp`/`iat` claims, and that a new token is issued on every login. |
| **V7.2.3** | L1 | Tokens are unique with ≥128 bits of entropy. | **Test:** Capture multiple JWTs issued for the same account across separate logins. Confirm the signature/payload differ (due to `iat`), and that `generateRefreshToken()` (`UUID.randomUUID()`) produces 128-bit-entropy values. |
| **V7.2.4** | L1 | A new session token is generated on (re-)authentication; the old token is invalidated. | **Test:** Capture a JWT, log in again, capture the new JWT. Confirm `tokenVersion` increments on password change (`JwtService` claim), invalidating previously issued tokens. |
| **V7.3.1 / V7.3.2** | L2 | Inactivity timeout enforces re-authentication per documented risk analysis. | **Test:** After the 20-minute inactivity timeout, attempt a sensitive operation (e.g., password change). Confirm the expired JWT is rejected with `401 Unauthorized`, requiring re-authentication. |
| **V7.4.1** | L1 | Session immediately invalidated server-side on logout/expiration. | **Test (Session Hijacking):** Verify absolute session timeout (20 min, `EXPIRATION_TIME`) is enforced and that an expired/old token is rejected with `401 Unauthorized` on any subsequent request. |
| **V7.4.2** | L1 | All sessions are invalidated after a password change (REQ1.5). | **Test:** Change the account password. Verify `tokenVersion` is incremented on `User` and that a previously issued JWT (with the old `tokenVersion` claim) is rejected with `401 Unauthorized`. |
| **V7.4.3** | L2 | Users can terminate other active sessions after changing an authentication factor. | **Test:** Log in from two sessions. Change the password from one session. Verify the `tokenVersion` bump invalidates the JWT used by the second session on its next request. |
| **V7.5.2** | L2 | Users can view/terminate active sessions; new-device logins are detected. | **Test:** Log in from a new `deviceId`. Verify `checkAndAlertNewDevice()` triggers `sendNewDeviceAlert()` and that `User.lastDeviceId` is updated, providing visibility of the most recent session/device. |
| **V7.6.2** | L2 | Session creation requires explicit user action (no silent session creation). | **Test:** Confirm no endpoint creates a JWT/session without an explicit `POST /api/auth/login` with valid credentials — there is no SSO/silent-session code path in `AuthenticationService`. |
| **V8.1.1** | L1 | RBAC restricts function-level and data-specific access by role. | **Test (Privilege Escalation / IDOR / Unauthorized Deactivation):** (1) Authenticate as `Supplier`, call `/api/admin/ApproveSupplier` → expect `403 Forbidden`. (2) Authenticate as Supplier A, target Supplier B's ID → expect `403`. (3) Send a deletion request with a regular-user token → expect `403`. |
| **V8.2.1** | L1 | Function-level access restricted to consumers with explicit permissions. | **Test:** Enumerate all API endpoints and call each with tokens of lower-privileged roles (e.g., `Supplier` calling Admin routes). Verify all are rejected with `403 Forbidden` (Spring Security `@PreAuthorize`/role-based filters). |
| **V8.2.2** | L1 | Data-specific access restricted to authorized consumers (IDOR/BOLA prevention). | **Test (Unauthorized Account Deactivation):** Send a deletion request authenticated with a regular user token to `/api/supplier/delete`. Validate `403 Forbidden`. |
| **V8.3.1** | L1 | Authorization decisions made server-side, based on the validated token. | **Test:** Confirm (via code review of `JwtService`/`SecurityConfig`) that all authorization checks are based on the `role` claim validated server-side from the JWT signature — never on client-supplied headers/parameters. |
| **V9.1.1** | L1 | JWT digital signature is always validated before accepting claims. | **Test (JWT Payload Tampering):** Alter the `role` claim inside a JWT (e.g., `DIETITIAN` → `ADMIN`) without re-signing with the correct `jwt.secret`. Send the modified token. The server must detect the invalid signature and return `401 Unauthorized`. |
| **V9.1.2 / V9.1.3** | L1 | Token signing key strength and algorithm are appropriate. | **Test:** Review `JwtService.init()` — confirms `jwt.secret` is a base64-encoded 256-bit key, signed with `Keys.hmacShaKeyFor()` (HS256). Confirm `@PostConstruct` fails startup if `jwt.secret` is missing/invalid. |
| **V9.2.1** | L1 | Tokens are rejected outside their validity span (`exp`/`nbf`). | **Test:** Capture a valid JWT and manually advance the clock past `exp` (20 min). Send the expired token — server must reject it with `401 Unauthorized`. |
| **V9.2.2** | L2 | Receiving service validates token type before trusting claims. | **Test:** Present the opaque `refreshToken` (UUID, from `generateRefreshToken()`) directly to a protected endpoint expecting a JWT access token. Verify the OAuth2 Resource Server rejects it with `401 Unauthorized` (fails JWT structural validation). |
| **V10.3.2** | L2 | The application uses the role/claims from the validated token to enforce authorization. | **Test:** Confirm Spring Security extracts the `role` claim from the validated JWT and uses it directly to authorize route access (no separate role lookup that could diverge from the token). |
| **V10.3.3** | L2 | The application identifies the user from the token subject, not from client-supplied identifiers. | **Test:** Confirm the `sub` claim (email) from the JWT is used to look up the `User` entity in `IUserRepo` — never a user ID passed as a request parameter. |
| **V10.3.5** | L3 | Tokens have a defined, enforced expiration. | **Test:** Confirm `JwtService.EXPIRATION_TIME = 20 minutes` is enforced by the OAuth2 Resource Server's `JwtDecoder` (rejects tokens past `exp`). |
| **V12.2.1** | L1 | TLS used for all client-to-backend communication, no plaintext fallback. | **Test (Credential Sniffing):** Attempt an unencrypted HTTP request to the API. Verify HSTS (`Strict-Transport-Security`) header is present and the server enforces TLS 1.2/1.3, rejecting older protocols/cipher suites. |
| **V13.3.1** | L2 | Secrets are not hardcoded in source code or build artifacts. | **Test:** Review `JwtService` — confirm `jwt.secret` is injected via `@Value("${jwt.secret:}")` from `application.properties`/environment variables, and that `@PostConstruct` throws `IllegalStateException` at startup if absent, preventing accidental use of a hardcoded/default key. Confirm `jwt.secret` and MySQL credentials are not committed to the repository (managed via GitHub Secrets). |
| **V16.2.1** | L2 | Each log entry includes full metadata (when, where, who, what). | **Test (Action Repudiation – Auth):** Perform a supplier rejection action and validate the audit log records the Admin's ID, timestamp, IP address, and the exact action performed. |
| **V16.2.2** | L2 | Log timestamps are synchronized and use UTC/offset. | **Test:** Generate log entries from multiple components (API, mail service) simultaneously. Confirm timestamps are consistent (≤1s skew) and formatted in UTC. |
| **V16.2.3** | L2 | Logs are only sent to documented destinations. | **Test:** Compare the log inventory documentation against active log destinations. Confirm a test log event appears only in documented sinks. |
| **V16.2.4** | L2 | Logs use a common, correlatable format. | **Test:** Generate sample log events and confirm they parse correctly when ingested by a log processor, with correlation possible via a session/request ID. |
| **V16.2.5** | L2 | Sensitive data in logs is masked/omitted. | **Test (Sensitive Data Leakage):** Force a processing error involving confidential fields (bio-certificate, passwords). Confirm the log entry masks sensitive values as `***` with no plaintext credentials/personal data. |
| **V16.4.1** | L2 | Logging components encode data to prevent log injection. | **Test:** Submit log-injection payloads (`\n`, ANSI escape sequences, forged log prefixes) via `deviceId`/email fields. Inspect the Slf4j/Logback output and confirm injected control characters are escaped/stripped, with no forged log lines. |

### Not Applicable

| ASVS ID | Level | Requirement | Justification |
| :--- | :---: | :--- | :--- |
| **V6.4.5** | L3 | Advance notifications before expiring authentication factors, with self-service renewal. | No requirement (REQ2.x) defines time-limited credentials/certificates that expire on a schedule requiring advance notice — only the 20-minute reset token (REQ2.3) and 6-month password rotation (REQ2.5), already covered elsewhere. |
| **V8.4.2** | L3 | Administrative interfaces require continuous identity verification / contextual risk analysis (MFA, device posture). | Out of scope for an academic L2 project — no MFA/device-posture infrastructure exists or is required by the documented NFRs. |
| **V9.2.3** | L2 | Tokens are validated against an `aud` (audience) allowlist. | The application issues and consumes JWTs for a single audience (its own API) only — there is no multi-service/multi-audience token exchange in the current architecture. |
 
---

## 2.2. Supplier Approval

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V5.1.1** | L2 | Documentation defines permitted file types, extensions, max sizes, and safe-handling behaviour for uploads. | **Test:** Review the file-upload documentation for the bio-certificate upload point. Verify it specifies `.pdf` only, 5 MB max, and the expected rejection behaviour for invalid/oversized files. |
| **V5.2.1** | L1 | Only files of a processable size are accepted (no DoS via huge files). | **Test:** Upload a file exactly at 5 MB and verify acceptance. Upload a 6 MB file and verify server-side rejection (not only client-side). |
| **V5.2.2** | L1 | Files validated by extension, magic bytes, and content structure. | **Test (Malicious File Upload):** Upload an executable renamed to `.pdf` and a genuine PDF exceeding 5 MB. Both must be rejected. Verify the validation checks magic bytes (`%PDF-`), enforces the 5 MB limit, and restricts uploads to `.pdf`. |
| **V5.3.1** | L1 | Uploaded files do not allow code execution on the server. | **Test:** Confirm uploaded bio-certificates are stored as `byte[]` directly in the database (not written to the filesystem/executed), eliminating any path for an uploaded file to be "executed" by the server. |
| **V5.3.2** | L1 | File paths use internally generated identifiers, not user-submitted filenames. | **Test (PDF Path Traversal):** Upload a file with path-traversal characters in its filename (e.g., `../../etc/passwd.pdf`). Verify the server ignores the original filename, storing/retrieving the file as `"bio_certificate_" + id + ".pdf"`. |
| **V5.4.1 / V5.4.2** | L2 | Uploaded files undergo content/format validation appropriate to their type. | **Test:** Confirm the bio-certificate upload pipeline validates PDF structure (magic bytes + parseability) before persisting, rejecting corrupted or non-PDF content disguised with a `.pdf` extension. |
| **V5.4.3** | L2 | Uploaded files are scanned for malware before being trusted. | **Test:** Upload a file containing the EICAR test signature. Confirm `TotalVirusService` (VirusTotal integration) flags the file and the upload is rejected before persistence. |
| **V12.3.1** | L2 | Encrypted protocol (TLS) used for all connections to the notification service. | **Test (Decision Email Tampering):** Attempt to reuse a credential-setup link sent via email after it has been used once, or after 24 hours have elapsed. The server must reject the token as expired/already consumed. |
| **V1.3.7** | L2 | Application avoids building templates from untrusted input (template injection protection). | **Test (Email Templates):** Review `EmailService` — confirm all email bodies (`sendSupplierWelcomeEmail`, `sendSupplierRejectionEmail`, `sendNewDeviceAlert`) are built via Java string concatenation with hardcoded templates (no Thymeleaf/FreeMarker/Velocity engine). Submit a `reason`/`deviceInfo` value containing template syntax (e.g., `${7*7}`, `#{...}`) and confirm it is rendered literally in the email body, never evaluated. |
| **V2.1.2** | L2 | Documentation defines validation of logical/contextual consistency between combined data fields. | **Test (Residency Consistency – REQ3.4):** Submit a supplier application where the address/postal code does not correspond to the Cinfães municipality. Verify the application is rejected at validation, confirming the documented rule that residency data must be logically consistent with the "resident of Cinfães" requirement. |

### Not Applicable

| ASVS ID | Level | Requirement | Justification |
| :--- | :---: | :--- | :--- |
| **V2.4.1** | L2 | Anti-automation controls against excessive calls (data exfiltration, quota exhaustion, DoS). | No rate-limiting/anti-automation layer (e.g., Bucket4j, API gateway) is currently implemented in the project. **Recommendation for a future iteration:** revisit this status — V6.1.1 already implements account-lockout-based anti-automation for login; a similar mechanism could be extended to the public registration endpoint (`/api/suppliers/apply`) and the supplier-listing endpoint. |
 
---

## 2.3. Supplier Management

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V8.2.2** | L1 | Data-specific access restricted to authorized consumers (IDOR/BOLA prevention). | *(See §2.1 – Compliant – V8.2.2 above.)* |
| **V16.2.5** | L2 | Sensitive data in logs is masked or omitted per the data protection level. | *(See §2.1 – Compliant – V16.2.5 above.)* |
| **V15.4.2** | L3 | State checks and dependent actions performed atomically (TOCTOU prevention). | **Test (Race Condition – Supplier Edit):** Send two concurrent `PUT` requests for the same supplier record with conflicting data. The system must process exactly one and reject the other via optimistic locking. **Test (Supplier Ranking Manipulation):** Send the supplier-listing request with tampered client-side sort parameters. Verify the server ignores them and computes ranking strictly server-side. |
| **V4.1.1** | L1 | Every HTTP response with a body declares a correct `Content-Type` (incl. `charset`). | **Test:** `curl -i` against `GET /api/canteens` and other `@RestController` endpoints. Verify the response header is `application/json;charset=UTF-8` (Spring Boot/Jackson default), including for endpoints returning entities with Portuguese accented characters (`ã`, `ç`, `õ`). |
| **V2.2.3** | L2 | Combinations of related data items are validated as reasonable per pre-defined rules. | **Test:** Submit a supplier-data update (REQ7.1/7.3) where `productiveCapacity` is inconsistent with the declared `address`/region (e.g., a capacity value of 0 combined with an "active supplier" status). Verify the API enforces the documented combination rule and rejects inconsistent updates. |

### Not Applicable

| ASVS ID | Level | Requirement | Justification |
| :--- | :---: | :--- | :--- |
| **V4.1.4** | L3 | Only explicitly supported HTTP methods are allowed; unused methods (e.g., `TRACE`, `CONNECT`) are blocked. | Covered by default Tomcat 10.1.x behaviour (embedded via Spring Boot 3.5.15) — `TRACE`/`CONNECT` are disabled by default (`allowTrace=false`), and Spring's `@RequestMapping` annotations implicitly restrict each route to its declared HTTP method(s), returning `405 Method Not Allowed` for others. No custom configuration was required. |
 
---

## 2.4. Meal Planning Management

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V14.1.1** | L2 | All sensitive data is identified, classified, and documented. | **Test (Specific Diet Data Leak):** Authenticate with a profile lacking clinical privileges and attempt to retrieve meal-planning data via the API. Verify allergy/health notes are absent or redacted from the response, and that allergy data is classified as sensitive and encrypted at rest. |
| **V14.1.2** | L2 | Sensitive-data protection levels have documented requirements (encryption, retention, access control, privacy). | **Test:** Review the data classification documentation and verify each sensitivity level (PII, health data, credentials) has documented protection requirements covering encryption at rest/in transit, retention period, access control, and privacy measures. |
| **V14.2.3** | L2 | Sensitive data is removed/anonymized when no longer required. | **Test:** Review the data-retention policy for supplier applications rejected under REQ4.4. Confirm rejected applicants' bio-certificate `byte[]` and personal data are either deleted or anonymized after the documented retention period. |
 
---

## 2.5. Order Product

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V2.3.1** | L1 | Business logic flows are processed only in the expected sequential step order; steps cannot be skipped. | **Test:** Attempt to skip a required step in the order-placement flow (e.g., submit an order confirmation without completing the stock-validation/calculation step). Verify the system rejects out-of-sequence requests and enforces the correct flow order. |
| **V2.3.2** | L2 | Business logic limits implemented per documentation, preventing exploitable logic flaws. | **Test (Order Calculator Manipulation):** Submit invalid/irrational numeric parameters (e.g., `-500`, `0`, or alphabetic characters) into the order calculator. The system must trigger a validation error before processing — all calculator inputs must be server-side validated against documented business rules. |
| **V2.3.4** | L2 | Locking mechanisms prevent double-booking of limited-quantity resources. | **Test:** Submit two concurrent confirmation requests for the same encomenda/reserva. Verify locking (optimistic or pessimistic) ensures exactly one succeeds and the resource is not double-booked. |
| **V2.3.5** | L3 | High-value business logic flows require multi-user approval. | **Test:** Identify the high-value flow(s) in scope (e.g., approval of large-volume supplier contracts, REQ4.2). Confirm the documented approval workflow requires sign-off from a second authorized Admin before the action (e.g., `SupplierStatus → Approved`) is finalized, and that this cannot be bypassed via a direct API call from a single Admin account. *(Note: confirm and document where this dual-approval step is implemented — see observation below.)* |
| **V2.2.2** | L1 | Input validation enforced server-side, not relied upon client-side. | **Test:** Intercept a request that passed any client-side validation and modify field values to invalid data (e.g., negative quantities, disallowed characters). Submit it directly to the server. Verify the server independently validates and rejects the tampered request. |
| **V15.4.1** | L3 | Shared objects accessed safely (synchronization) to prevent race conditions. | **Test (Double Order Race Condition):** Fire two identical order-finalization requests in parallel/simultaneously. The system must process the first and drop the second via the idempotency token — no duplicate invoices/orders generated. |
 
---

## 2.6. CI/CD and Pipeline Security

The following controls are implemented as automated steps within the **GitHub Actions** CI/CD pipeline.

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V16.2.1** | L2 | Security-relevant pipeline events are logged with full metadata (who, what, when, where). | *(See §2.1 – Compliant – V16.2.1 above. Pipeline-specific context: every workflow run records the triggering actor, commit SHA, branch, and timestamp in the GitHub Actions audit log.)* |
| **V12.2.1** | L2 | TLS is enforced for all connectivity; no plaintext fallback. | *(See §2.1 – Compliant – V12.2.1 above. Pipeline-specific context: all pipeline steps communicating with external services — NVD feeds, Docker registries, GitHub API — use HTTPS exclusively.)* |
| **V8.1.1** | L1 | Authorization documentation restricts access; release publication is access-controlled. | *(See §2.1 – Compliant – V8.1.1. Pipeline-specific context: release-publication jobs use `permissions: contents: write` scoped only to the release job; branch protection rules restrict who can trigger it.)* |
| **V1.2.4** | L1 | Parameterized queries / safe output encoding prevent injection in dynamic constructs. | *(See §2.1 – Compliant – V1.2.4. Pipeline-specific context: a SAST tool integrated into the pipeline detects string-concatenated SQL patterns and fails the build.)* |
| **V5.3.2** | L1 | File paths use internally generated names; user-supplied filenames are not used in storage. | *(See §2.2 – Compliant – V5.3.2.)* |
| **V2.2.2** | L1 | Input validation enforced server-side. | *(See §2.5 – Compliant – V2.2.2. Pipeline-specific context: a failing security-related unit test, e.g. verifying JWT validation rejects an unsigned token, blocks artifact generation and merge approval.)* |
| **V15.4.1** | L3 | Idempotency/locking mechanisms prevent duplicate transactions. | *(See §2.5 – Compliant – V15.4.1. Pipeline-specific context: release artifacts are produced and uploaded exclusively by the GitHub Actions pipeline, gated on all mandatory security stages passing.)* |

### Not Applicable

| ASVS ID | Level | Requirement | Justification |
| :--- | :---: | :--- | :--- |
| **V14.3.1** | L1 | Automated SCA on dependencies, monitored against known CVEs. | No SCA tool (e.g., OWASP Dependency-Check, Dependabot, Snyk) is currently configured in the GitHub Actions workflows for this repository. **Recommendation:** revisit before final delivery — adding a Dependency-Check step is low-effort and directly supports the "Secure Build" section of the project documentation. |
| **V14.3.2** | L2 | Automated secret scanning on every commit, blocking exposed credentials. | No secret-scanning step (e.g., GitHub Advanced Security, truffleHog, gitleaks) is currently configured. **Recommendation:** same as above — easy to add and directly relevant given `jwt.secret`/MySQL credentials are managed as GitHub Secrets. |
| **V14.2.4** | L2 | SARIF security reports uploaded to a centralized dashboard. | No SAST/SCA tool currently produces SARIF output for this project; without V14.3.1, this is also not applicable. |
| **V13.1.2** | L3 | Documentation defines max concurrent connections per service, with fallback behaviour. | L3 requirement; no formal documentation of connection-pool limits/fallback behaviour exists for the MySQL connection pool or mail service. *(The DoS-via-complex-payload test concept discussed for this requirement is instead covered functionally under V2.3.2 — Order Calculator input validation.)* |
 
---

## 2.7. Cross-Cutting Architecture, Communications & Configuration (V1, V4, V12, V13)

This section covers controls that are not specific to a single functional flow but apply across the entire application — primarily related to HTTP protocol handling, TLS, and information-leakage prevention. All items in this section were confirmed via review of `pom.xml` (Spring Boot 3.5.15, embedded Tomcat 10.1.x) and source code.

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V1.3.3** | L2 | Data passed to a dangerous context is sanitized — allowlist of safe characters + length limits. | **Test:** Submit boundary-violating values for key fields (e.g., NIF with non-numeric characters or wrong length, address fields with control characters, `reason`/`deviceInfo` exceeding a reasonable max length). Verify each is rejected via Bean Validation (`@Pattern`, `@Size`) before reaching business logic, email, or logging layers. |
| **V4.2.1** | L2 | All components determine HTTP message boundaries correctly (request smuggling prevention). | **Test:** Send a request with both `Transfer-Encoding: chunked` and a conflicting `Content-Length` header to `https://<host>/api/canteens`. Confirm Tomcat 10.1.x rejects or correctly resolves the ambiguity per RFC 7230 (ignoring `Content-Length` when `Transfer-Encoding` is present). *No load balancer/firewall/reverse proxy is present in the current architecture — Tomcat is the sole component determining message framing.* |
| **V4.2.2** | L3 | `Content-Length` does not conflict with actual content length (smuggling prevention). | **Test:** Inspect response headers via `curl -i` for several endpoints of varying response sizes. Confirm `Content-Length` always matches the actual body length — managed automatically by Tomcat, with no manual header manipulation in any controller. |
| **V4.2.3** | L3 | No connection-specific headers (`Transfer-Encoding`, `Connection`, `Upgrade`) sent/accepted over HTTP/2 or HTTP/3. | **Test:** `curl --http2 -H "Transfer-Encoding: chunked" -H "Connection: keep-alive" -i https://<host>/api/canteens`. Confirm the request is rejected or the headers are ignored, per RFC 9113 (enforced by Tomcat 10.1.x). |
| **V4.2.4** | L3 | HTTP/2 and HTTP/3 header fields cannot contain CR/LF sequences (header injection prevention). | **Test:** Confirm via code review that HPACK/QPACK (used by Tomcat for HTTP/2) is a binary, length-prefixed format where CR/LF injection in header values is structurally impossible — no test payload can produce a malformed header. |
| **V4.2.5** | L3 | Outgoing URIs/headers (e.g., `Authorization`, `Cookie`) are validated/limited in length to avoid DoS. | **Test:** Review `JwtService.generateToken()` — confirm claims are limited to `role`, `tokenVersion`, `sub` (email), `iat`, `exp` (no arrays/lists). Measure the resulting JWT length (~150–250 bytes) and confirm it remains well below Tomcat's `maxHttpHeaderSize` (default 8KB). |
| **V12.2.2** | L1 | External-facing services use publicly trusted TLS certificates. | **Test:** Connect to all external-facing endpoints and inspect TLS certificates. Verify they are issued by a publicly trusted CA, are not expired, and the hostname matches — no self-signed certificates in production. |
| **V13.4.1** | L1 | No source-control metadata (`.git`/`.svn`) accessible. | **Test:** `curl https://<host>/.git/config` and `/.svn/entries` — expect `404`. Confirmed by design: deploy artifact is a repackaged Spring Boot JAR (`spring-boot-maven-plugin:repackage`), which does not include `.git`/`.svn` directories. |
| **V13.4.2** | L2 | Debug modes disabled in production. | **Test:** Confirm `application-prod.properties` (or equivalent profile) does not set `debug=true`, `spring.jpa.show-sql=true`, or `logging.level.root=DEBUG`. Run `GET /actuator/env` against a production-like deployment and confirm it is not exposed (see V13.4.5). |
| **V13.4.3** | L2 | Directory listings not exposed. | **Test:** Request a directory path without an index (e.g., `GET /api/`) — expect `404`/`403`, never a file listing. Confirmed by design: Tomcat embedded via Spring Boot does not serve static directory listings, and the application has no `src/main/resources/static` content. |
| **V13.4.4** | L2 | HTTP `TRACE` method not supported in production. | **Test:** `curl -X TRACE https://<host>/api/canteens` — expect `405 Method Not Allowed`. Confirmed by Tomcat 10.1.x default configuration (`allowTrace=false`). |
| **V13.4.5** | L2 | Documentation/monitoring endpoints not exposed unless intended. | **Test:** `curl https://<host>/actuator/env`, `/actuator/beans`, `/actuator/mappings`. Confirm these return `404`/`401` and only `/actuator/health` (and optionally `/actuator/info`) are reachable — Spring Boot 3.x default for `management.endpoints.web.exposure.include`. |
| **V13.4.6** | L3 | No detailed backend version information exposed. | **Test:** `curl -I https://<host>/api/canteens` and inspect the `Server` header. Confirm it does not disclose a specific Tomcat/Spring Boot version (suppressed/genericized). Confirm `/actuator/info` (if exposed) does not return build/version metadata to anonymous users. |


## Traceability Matrix — Secure Development Requirements ↔ ASVS Controls ↔ Tests

This matrix closes the loop between the **Secure Development Requirements** (high-level SDLC practices, documented in the project's security requirements) and the **concrete ASVS-level test procedures** defined in the Test Planning document (§2.1–§2.7). For each practice, it identifies which specific, executable tests provide evidence that the practice is actually being verified — and flags cases where a documented practice currently lacks a corresponding implemented/passing test (**Gap**).

A traceability status is assigned per practice:

- **Traced** — the practice is backed by at least one concrete, passing test in the Test Planning document.
- **Partially Traced** — some aspects of the practice are verified by tests, but others rely only on process/manual review without an automated check.
- **Gap** — the practice is documented (and possibly described as "implemented" in the Secure Development Requirements section), but no corresponding test exists, or the related ASVS control is marked *Not Applicable*/*Not Started* in the tracker — indicating a discrepancy between documentation and verified reality.

---

### 1. Secure Coding Guidelines

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| OWASP Secure Coding Practices applied to Menu, Reservations, Stock, and Supplier flows — input validation, RBAC, least privilege. | V1.2.4, V1.3.3, V1.3.11, V2.2.1, V2.2.2, V2.2.3, V8.1.1, V8.2.1, V8.2.2 | §2.1 V1.2.4 (SQLi via login fields), §2.1 V2.2.2 (server-side validation bypass), §2.7 V1.3.3 (allowlist/length-limit boundary tests), §2.1 V8.1.1/V8.2.1/V8.2.2 (RBAC/IDOR tests against Admin, Supplier, Dietitian roles) | **Traced** |

> **Note:** the practice statement references "Menu, Reservations, Stock Operations" explicitly — the current test plan covers Authentication, Supplier, and Order flows in depth, but Meal Planning/Stock-specific input-validation tests (e.g., REQ5.3 dish creation, REQ5.4 five dish types) are not yet itemised. Recommend adding a dedicated test entry under §2.4 for input validation on menu/dish creation endpoints to fully close this practice.

---

### 2. Dependency Management

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| Third-party libraries monitored via OWASP Dependency Check in CI; vulnerable `pom.xml` packages trigger build failure; SCA report reviewed at each release. | V14.3.1 | §2.6 V14.3.1 — currently classified **Not Applicable** ("No SCA tool ... is currently configured in the GitHub Actions workflows") | **Gap** |

> **Finding:** this is a direct contradiction. The Secure Development Requirements section states OWASP Dependency Check is integrated into CI and gates the build — but the ASVS tracker (§2.6) marks V14.3.1 as *Not Applicable* due to the absence of any SCA step in the GitHub Actions workflows. **Either** (a) the Dependency-Check step exists and §2.6 must be updated to *Compliant* with the test plan already drafted for V14.3.1 ("add a dependency with a known CVE to `pom.xml`, trigger CI, validate the SARIF/HTML report flags it and fails the build on HIGH/CRITICAL"), **or** (b) it does not exist yet and the Secure Development Requirements section is describing an *intended* practice rather than an *implemented* one — in which case it should be reworded (e.g., "planned" / "to be implemented") to avoid an inconsistency the reviewer will flag.

---

### 3. Secure Code Review

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| Security-focused manual review required for changes to JWT logic, RBAC, and MySQL data access. SonarQube assists but does not replace manual review. | V9.1.1, V9.1.2/V9.1.3, V8.1.1, V8.3.1, V1.2.4 | §2.1 V9.1.1 (JWT signature tampering test), §2.1 V9.1.2/V9.1.3 (review of `JwtService.init()` — HS256, 256-bit key, fail-fast on missing secret), §2.1 V8.1.1/V8.3.1 (RBAC/authorization tests), §2.1 V1.2.4 (parameterized queries) | **Partially Traced** |

> **Note:** the *automated* portions (JWT signature validation, parameterized queries, RBAC enforcement) are covered by concrete tests. The "PR approval gate requires at least one security-aware reviewer" portion is a **process control**, not something testable via an automated test procedure — this is expected and acceptable, but should be documented as a process artifact (e.g., a CODEOWNERS file or branch-protection rule requiring review from a specific team/group) rather than left only as a textual claim.

---

### 4. SAST (Static Application Security Testing)

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| SonarQube/CodeQL integrated into GitHub Actions, running on every PR and push to protected branches. PRs with HIGH/CRITICAL findings are blocked. | V1.2.4, V14.2.4 | §2.6 V1.2.4 ("SAST job detects string-concatenated SQL pattern and fails the build" — Compliant), §2.6 V14.2.4 — currently **Not Applicable** ("No SAST/SCA tool currently produces SARIF output") | **Partially Traced / Gap** |

> **Finding:** the V1.2.4 pipeline test plan (committing a deliberate SQLi pattern and confirming the SAST job fails the build) presumes a SAST tool is active — yet V14.2.4 (SARIF upload to the Security dashboard) is marked *Not Applicable* on the basis that **no SAST/SCA tool currently produces SARIF output**. These two statuses cannot both be accurate simultaneously: if SonarQube/CodeQL is genuinely integrated (as the practice claims and as V1.2.4 assumes), V14.2.4 should be re-evaluated as *Compliant* or *In Progress* with a concrete SARIF-upload test, rather than *Not Applicable*. **Action required:** confirm whether a SAST tool is actually wired into the CI workflow; update either V1.2.4 (if it is not) or V14.2.4 (if it is) so the tracker is internally consistent.

---

### 5. Secret Management

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| All secrets (`jwt.secret`, MySQL connection strings, External Portal API certificates) stored exclusively in GitHub Secrets. Secret scanning on every commit blocks hardcoded credentials and fails the pipeline. | V13.3.1, V14.3.2 | §2.1 V13.3.1 (code review of `JwtService` — `@Value` injection, `@PostConstruct` fail-fast if `jwt.secret` absent — **Compliant**), §2.6 V14.3.2 — currently **Not Applicable** ("No secret-scanning step ... is currently configured") | **Partially Traced / Gap** |

> **Finding:** the *runtime* half of this practice (the application itself never hardcodes `jwt.secret` and fails to start without it) is verified and **Compliant** (V13.3.1). The *pipeline* half (automated secret scanning blocking commits with exposed credentials) is claimed in the Secure Development Requirements section but is **Not Applicable/not implemented** per V14.3.2. **Action required:** same pattern as Dependency Management — either add a secret-scanning step (e.g., gitleaks, GitHub Advanced Security secret scanning, low implementation cost) and run the V14.3.2 test ("commit a fake AWS key/JWT secret pattern to a test branch, verify the workflow blocks the merge"), or adjust the wording in the Secure Development Requirements section to reflect this as a planned rather than implemented control.

---

### 6. Secure Logging

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| Security events (authentication, unauthorized access, External Portal errors) logged without sensitive data; format validated per V16.2.x. Log-injection tests run as part of the integration suite; V16.2.5 masking validated. | V16.2.1, V16.2.2, V16.2.3, V16.2.4, V16.2.5, V16.4.1 | §2.1 V16.2.1 (audit log metadata for supplier rejection/approval/menu publication), V16.2.2 (UTC/synchronized timestamps), V16.2.3 (documented log destinations only), V16.2.4 (correlatable log format), V16.2.5 (sensitive-data masking on processing errors), V16.4.1 (log-injection via `deviceId`/email) | **Traced** |

> **Note:** this is the most thoroughly traced practice — every sub-claim in the documented practice (event coverage, masking, format, injection protection) maps directly to a §2.1 test with a concrete payload/expected result. No action required, beyond ensuring the §2.1 V16.4.1 test specifically exercises the `sendNewDeviceAlert`/`deviceInfo` path identified earlier as the highest-risk log-injection vector (since `deviceInfo` can originate from a client-controlled `User-Agent`-like value).

---

### 7. Automated Security Tests

| Documented Practice | Related ASVS Controls | Corresponding Test(s) | Traceability Status |
| :--- | :--- | :--- | :---: |
| Unit tests cover JWT generation/validation. Integration tests verify secure interactions between backend, MySQL, and External Portal. E2E tests simulate real-world attack scenarios. Security test suite runs every CI build; failures block merge/publication. | V9.1.1, V9.2.1, V9.2.2, V2.3.x, V15.4.1, V15.4.2, V8.1.1 | §2.1 V9.1.1/V9.2.1/V9.2.2 (unit-level JWT tests — signature, expiry, token-type validation), §2.5 V2.3.1/V2.3.2/V2.3.4/V15.4.1 (integration tests — order flow sequencing, calculator limits, idempotency), §2.3 V15.4.2 (concurrent-edit/race-condition integration test), §2.1 V8.1.1 (E2E privilege-escalation scenarios across roles) | **Traced** |

> **Note:** "interactions ... with the External Portal" is referenced in the documented practice, but no test in §2.1–§2.7 currently exercises an External-Portal integration — consistent with the earlier finding that no External-Portal integration code/configuration was identified in the codebase reviewed. If the integration does not yet exist in the implementation, this clause of the practice should be marked as **planned/future work** rather than an active, tested control — otherwise this is a second minor documentation-vs-reality gap, lower severity than items 2 and 5 above.

---

## Summary — Traceability Status Overview

| # | Practice | Status | Action Required Before Submission |
| :---: | :--- | :---: | :--- |
| 1 | Secure Coding Guidelines | Traced | Optional: add a Meal Planning/Stock-specific input-validation test entry (§2.4) for full coverage of the practice's stated scope. |
| 2 | Dependency Management | **Gap** | Reconcile V14.3.1 status with the practice's claim of an active OWASP Dependency Check step — implement it, or reword the practice as planned. |
| 3 | Secure Code Review | Partially Traced | Document the PR-review process control (e.g., CODEOWNERS/branch protection) as an artifact, separate from automated tests. |
| 4 | SAST | Partially Traced / **Gap** | Reconcile V1.2.4 (assumes SAST active) vs. V14.2.4 (Not Applicable, no SAST/SARIF) — internal inconsistency must be resolved. |
| 5 | Secret Management | Partially Traced / **Gap** | Reconcile V14.3.2 (Not Applicable, no secret scanning) with the practice's claim — implement gitleaks/Advanced Security, or reword as planned. |
| 6 | Secure Logging | Traced | None — fully covered. |
| 7 | Automated Security Tests | Traced | Clarify whether External Portal integration tests exist or are future work; adjust wording if not yet implemented. |

**Overall:** 3 of 7 practices (1, 6, 7) are fully traced to concrete, executable tests with no outstanding issues. Practices 3 (Secure Code Review) is partially traced — the automated component is verified, while the process component is a documentation artifact rather than a test. Practices 2, 4, and 5 reveal the same recurring pattern: **the Secure Development Requirements section describes CI/CD security automation (SCA, SAST/SARIF, secret scanning) as implemented, while the ASVS tracker marks the corresponding controls (V14.3.1, V14.2.4, V14.3.2) as *Not Applicable* due to their absence from the GitHub Actions workflows.** This is the single most important finding of this traceability exercise and should be resolved — either by implementing these (relatively low-effort) pipeline steps, or by aligning the documentation's wording with the project's actual current state — before final submission.