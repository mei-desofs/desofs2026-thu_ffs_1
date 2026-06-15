# Phase 2: Sprint 2

---

### Table of Contents

- [Introduction](#introduction)
- [Development](#development)
- [Build and Test](#build-and-test)
- [Pipeline Automation](#pipeline-automation)
- [Test Planning](#test-planning)
- [Conclusion](#conclusion)

---

## Introduction

This document covers the security engineering practices adopted during the development of the **Cantinas de Cinfães** backend — a Spring Boot REST API for managing canteen operations, including user authentication, meal scheduling, and email notifications.

The project follows a **shift-left security** approach, integrating security checks throughout the development lifecycle via three automated GitHub Actions pipelines: one for commits, one for Pull Requests, and one for Releases. Practices include SAST, SCA, DAST, secret scanning, artifact scanning, SBOM generation, and automated testing, all traceable to security requirements defined using the OWASP ASVS.

---

## Test Planning

This document maps each implemented or in-progress ASVS 5.0 control to its corresponding test plan, organized by functional area. Controls with status **Compliant** have a passing test procedure. Controls marked **In Progress** have a defined test plan pending full implementation.
 
---

## 2.1. Authentication

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V1.1.1** | L2 | Input is decoded to canonical form only once, before processing or validation. | **Test:** Inject double-encoded payloads (e.g., `%2527` for `%27`) into all input fields. Verify the application decodes input only once and does not re-decode after validation, rejecting the malicious input at a single point. |
| **V1.1.2** | L2 | Output encoding and escaping are performed as the final step before the interpreter consumes the data. | **Test:** Submit HTML metacharacters (`<`, `>`, `"`) through login fields and profile forms. Inspect the HTTP response and rendered output to confirm all characters are properly escaped and no raw HTML is reflected. |
| **V6.2.1** | L1 | User-set passwords meet the defined minimum length policy. | **Test:** Attempt to submit a weak or short password (e.g., `12345`, `qwerty`) on the registration and profile update forms. The system must block creation and display a policy error mandating the minimum criteria (10-character minimum, uppercase, numbers, and special characters). |
| **V7.2.1** | L1 | All session token verification is performed by a trusted backend service. | **Test:** Attempt to bypass session verification by sending tampered tokens directly to backend endpoints, skipping frontend validation. Verify the backend independently validates every token and rejects tampered ones with `401 Unauthorized`. |
| **V7.2.2** | L1 | The application uses dynamically generated self-contained or reference tokens, not static API keys. | **Test:** Inspect the token issued after login. Verify it is dynamically generated (e.g., a signed JWT with expiry claims) and that a static API secret alone cannot authenticate a session. Confirm a new token is issued on every login. |
| **V7.2.3** | L1 | Reference tokens are unique and generated with at least 128 bits of cryptographically secure entropy. | **Test:** Capture multiple session tokens issued under the same account in separate sessions. Verify they are unique, non-sequential, and unpredictable. Confirm entropy meets the minimum 128-bit threshold via token length and character set analysis. |
| **V7.2.4** | L1 | A new session token is generated on authentication and re-authentication; the old token is terminated. | **Test:** Record the session token before login, log in, and capture the new token. Attempt to use the pre-login token in a subsequent API request. The server must reject the old token with `401 Unauthorized`. |
| **V8.1.1** | L1 | Authorization documentation defines rules restricting function-level and data-specific access by role. | **Test (Privilege Escalation):** Authenticate as a `Supplier` and send a direct HTTP request to `/api/admin/ApproveSupplier`. Server must return `403 Forbidden`. **Test (IDOR):** Authenticate as Supplier A and submit a data change targeting Supplier B's ID. Server must return `403 Forbidden`. **Test (Unauthorized Deletion):** Send a deletion request authenticated as a regular user token. Server must return `403 Forbidden`. |
| **V8.2.1** | L1 | Function-level access is restricted to consumers with explicit permissions. | **Test:** Enumerate all API endpoints and attempt to call each one with tokens of lower-privileged roles (e.g., `Supplier` calling `Admin` routes). Verify that every unauthorized call is rejected with `403 Forbidden`. |
| **V8.2.2** | L1 | Data-specific access is restricted to authorized consumers, preventing IDOR/BOLA. | **Test:** Authenticate as Supplier A and issue a deletion request to `/api/supplier/delete` with Supplier B's ID. Validate that the server returns `403 Forbidden` and no data is modified. |
| **V9.2.1** | L1 | Tokens are rejected if they fall outside their validity time span (`nbf`/`exp` claims are enforced). | **Test:** Capture a valid JWT and manually advance its `exp` claim. Send the expired token in an API request. The server must reject it with `401 Unauthorized` and must not accept tokens before their `nbf` time. |
| **V12.1.1** | L1 | Only the latest recommended TLS protocol versions are enabled. | **Test (Credential Sniffing):** Attempt to establish a connection over unencrypted HTTP (port 80). The server must reject the connection or force a redirect to HTTPS. Verify via TLS scanner that only TLS 1.2/1.3 are accepted and that older protocols (SSL 3.0, TLS 1.0, TLS 1.1) are refused. |
| **V12.2.1** | L1 | TLS is used for all client-to-backend communications with no fallback to plaintext. | **Test (Supplier Data Interception):** Inspect server response headers and verify HSTS (`Strict-Transport-Security`) is present and enforced, preventing protocol downgrades. Attempt a TLS downgrade attack; the server must reject the degraded connection. |
| **V12.2.2** | L1 | External-facing services use publicly trusted TLS certificates. | **Test:** Connect to all external-facing endpoints and inspect their TLS certificates. Verify they are issued by a publicly trusted CA, are not expired, and the hostname matches. Confirm no self-signed certificates are used in production. |
| **V16.2.1** | L2 | Each log entry includes full metadata: when, where, who, and what. | **Test (Action Repudiation – Auth):** Perform a supplier rejection action and validate in the audit log that the Admin's ID, timestamp, IP address, and the exact action performed are all recorded. **Test (Approval):** Perform a supplier approval action and verify the log contains the previous state, new state, and the responsible Admin's ID. **Test (Meal Planning):** Publish a menu and inspect the audit log entry, verifying the explicit presence of the responsible Dietitian's ID. |
| **V16.2.2** | L2 | Time sources for all logging components are synchronized; timestamps use UTC or include a time zone offset. | **Test:** Generate log entries from multiple application components (API server, database, mail service) simultaneously. Compare timestamps across log sources and verify they are synchronized (within ≤1 second) and formatted consistently in UTC. |
| **V16.2.3** | L2 | The application only stores or broadcasts logs to destinations defined in the log inventory. | **Test:** Review the log inventory documentation and compare it against active log destinations (files, external services). Introduce a test log event and verify it appears only in documented destinations and not in any undocumented sinks. |
| **V16.2.4** | L2 | Logs can be read and correlated by the log processor in use, using a common logging format. | **Test:** Generate a sample of log events from different components and ingest them into the configured log processor (e.g., ELK, Splunk). Verify that all entries parse correctly, fields are correctly mapped, and cross-component correlation (e.g., by session ID or request ID) is functional. |
| **V16.2.5** | L2 | Sensitive data in logs is masked or omitted according to the data protection level. | **Test (Sensitive Data Leakage):** Force a processing error involving confidential fields (e.g., bio-certificate data, passwords). Review the generated log entry and confirm that sensitive values are masked as `***` and no plaintext credentials, tokens, or personal data appear in any log output. |

### In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V6.4.1** | L2 | Knowledge-based authentication mechanisms (security questions, password hints) are eliminated and replaced with secure recovery flows. | **Test:** Architecturally validate the complete absence of any "security question" or "password hint" fields across registration, login, and recovery flows. Test the email-based secure token reset flow end-to-end and confirm no question-based fallback path exists. |
| **V6.4.5** | L2 | Users receive advance notifications before authentication factors expire; self-service renewal is available. | **Test:** Advance the system clock or simulate credential expiry. Verify that the application automatically sends expiration warning notifications (multiple reminders) and that the self-service renewal flow allows early rotation without requiring support intervention. |
| **V6.4.6** | L2 | Administrators can only trigger password reset workflows (send reset link/token); they cannot set a user's password directly. | **Test:** Access the admin panel and verify that the admin interface provides only a "Send reset link" action, with no input field allowing direct password entry for another user's account. Confirm all admin reset actions are logged. |
| **V6.7.1** | L2 | Session inactivity timeouts are implemented and documented with appropriate values per risk level. | **Test (Session Inactivity Timeout):** Leave a session token inactive beyond the documented inactivity timeout limit. Submit a new API request using that idle token and validate the server returns `401 Unauthorized`. Verify timeout values are documented with justification aligned to NIST SP 800-63B. |
| **V7.1.1** | L2 | Session inactivity timeout and absolute maximum session lifetime are documented with justification. | **Test:** Review the session management documentation and verify it explicitly states the inactivity timeout value, the absolute maximum session lifetime, and a risk-based justification aligned with NIST SP 800-63B re-authentication requirements. |
| **V7.3.1** | L2 | An inactivity timeout enforces re-authentication according to risk analysis and documented security decisions. | **Test:** After the documented inactivity period, attempt to perform a sensitive operation (e.g., change email, submit an order) without re-authenticating. The application must prompt for re-authentication and reject the operation without valid credentials. |
| **V7.4.1** | L1 | On session termination (logout or expiration), the session is immediately invalidated server-side and no further use is permitted. | **Test (Session Hijacking):** Click "Logout" and capture the invalidated session token. Submit a new API request using the old token. The server must return `401 Unauthorized`. Additionally, verify that absolute session timeouts (20 min) are enforced and tokens are cleared from client storage upon logout. |
| **V7.4.3** | L2 | Users can terminate all other active sessions after a successful change or removal of any authentication factor. | **Test:** Log in from two separate devices or browsers to create two active sessions. Change the account password from one session. Navigate to the session management panel and verify an option exists to terminate all other active sessions. Confirm that the second session is immediately invalidated after the action. |
| **V7.5.2** | L2 | Users can view and (after re-authentication) terminate any or all active sessions. | **Test:** Log in from multiple devices. Navigate to the active sessions view and verify it lists all current sessions with identifying metadata (IP, device, timestamp). Re-authenticate and terminate a specific session. Confirm the terminated session's token is immediately rejected on the corresponding device. |
| **V7.6.2** | L2 | Session creation requires explicit user consent or action, preventing silent session creation. | **Test:** Attempt to trigger a new application session silently via a crafted link or redirect (e.g., simulating a third-party SSO flow) without any user interaction or consent step. Verify the application does not create a session without an explicit user action. |
| **V8.4.2** | L3 | Administrative interfaces require multiple security layers including continuous identity verification and contextual risk analysis. | **Test:** Access the admin interface from an unrecognized device or network location and verify that additional verification steps are triggered (e.g., MFA challenge, device posture check). Confirm that network location alone does not grant administrative access. |
| **V9.1.1** | L1 | JWT digital signatures are always validated before accepting token contents. | **Test (JWT Payload Tampering):** Alter the `role` claim inside a JWT (e.g., change `Dietitian` to `Admin`) without re-signing with the correct secret. Send the modified token in a request. The server must detect the invalid signature and return `401 Unauthorized`, never trusting the tampered payload. |
| **V9.2.2** | L2 | The receiving service validates that the token type is correct and intended for the current purpose before accepting its contents. | **Test:** Present a refresh token or ID token to an endpoint that expects an access token. Verify the server rejects the token with `401 Unauthorized`, confirming it validates token type before trusting claims. |
| **V9.2.3** | L2 | The service only accepts tokens whose audience (`aud`) claim matches the intended service. | **Test:** Generate a valid token with an `aud` claim targeting a different service (e.g., `aud: "external-portal"`). Present it to the BioCantinas API. The server must reject it with `401 Unauthorized`. Verify the audience allowlist is enforced server-side. |
| **V16.4.1** | L2 | All logging components encode data appropriately to prevent log injection attacks. | **Test:** Submit log injection payloads (e.g., newline characters `\n`, ANSI escape sequences, forged log prefixes) through user-controlled input fields (e.g., username, email). Inspect the generated log entries and verify the injected characters are encoded or sanitized, with no forged log lines appearing. |
 
---

## 2.2. Supplier Approval

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V5.1.1** | L2 | Documentation defines permitted file types, expected extensions, maximum sizes, and safe handling behaviour for all upload features. | **Test:** Review the file upload documentation for each upload point (supplier registration PDF, bio-certificate). Verify it specifies allowed types (`.pdf`), maximum size (5 MB), and the expected system behaviour when a malicious or oversized file is detected (rejection with error message). |
| **V5.2.1** | L1 | The application only accepts files of a size it can process without causing performance degradation or denial of service. | **Test:** Upload a file exactly at the maximum allowed size limit (5 MB) and verify it is accepted. Upload a file exceeding the limit (e.g., 6 MB) and verify it is rejected with an appropriate error. Confirm the size check occurs server-side and not only on the client. |
| **V12.3.1** | L2 | An encrypted protocol (TLS) is used for all inbound and outbound connections to the notification service. | **Test (Decision Email Tampering):** Attempt to reuse a credential setup link sent via email after it has been used once or after 24 hours have elapsed. The server must reject the token as expired or already consumed, preventing link-reuse attacks even if the email was intercepted. |

### In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V2.4.1** | L2 | Anti-automation controls protect against excessive calls that could cause data exfiltration, garbage-data creation, or denial of service. | **Test (Registration Flooding):** Simulate a registration flood by firing hundreds of concurrent POST requests to the public registration endpoint per second. Validate that the rate-limiting mechanism activates and blocks excess traffic with `429 Too Many Requests`. **Test (Mass Exfiltration):** Run an automated script issuing rapid sequential read requests to the supplier list endpoint. Verify the system throttles requests and restricts per-page data volume. |
| **V5.2.2** | L1 | Files are validated by checking extension, magic bytes, and content structure; invalid files are rejected. | **Test (Malicious File Upload):** Upload an executable file renamed to `.pdf` (e.g., `malware.exe` → `document.pdf`). Upload a genuine PDF exceeding 5 MB. The system must reject both cases with an error. Verify the validation checks magic bytes (`%PDF-`), enforces the 5 MB size limit, and restricts uploads to `.pdf` extension only. |
| **V5.3.2** | L1 | File paths are constructed using internally generated identifiers (e.g., GUIDs), not user-submitted filenames. | **Test (PDF Path Traversal):** Upload a file with path traversal characters in its filename (e.g., `../../../../etc/passwd.pdf`). Verify on the server that the file is stored using a randomly generated UUID as its name, and that the original user-submitted filename is neither used in storage nor exposed in the API response. |
 
---

## 2.3. Supplier Management

###  Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V4.1.4** | L3 | Only explicitly supported HTTP methods can be used; unused methods are blocked. | **Test (CSRF on Management Actions):** Replicate an HTTP state-changing request (e.g., DELETE to remove a supplier) while omitting the anti-CSRF token or required custom application header. The API must reject the request. Additionally, send requests using unsupported HTTP methods (e.g., `TRACE`, `CONNECT`) to all management endpoints and verify they return `405 Method Not Allowed`. |
| **V8.2.2** | L1 | Data-specific access is restricted to authorized consumers (IDOR/BOLA prevention). | *(See §2.1 – Compliant – V8.2.2 above.)* |
| **V16.2.5** | L2 | Sensitive data in logs is masked or omitted per the data protection level. | *(See §2.1 – Compliant – V16.2.5 above.)* |

###  In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V15.4.2** | L3 | State checks and the actions that depend on them are performed atomically to prevent TOCTOU race conditions. | **Test (Race Condition – Supplier Edit):** Send two concurrent PUT requests for the exact same supplier record with conflicting data using parallel HTTP clients. The system must process exactly one and reject the other, returning a conflict or lock error. **Test (Supplier Ranking Manipulation):** Send a request to the sorted supplier list endpoint with tampered client-side sorting parameters. Verify the server ignores the client-supplied sort directive and calculates the ranking algorithm strictly server-side. |
 
---

## 2.4. Meal Planning Management

###  Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V14.1.1** | L2 | All sensitive data is identified, classified into protection levels, and documented. | **Test (Specific Diet Data Leak):** Authenticate with a profile lacking clinical privileges and attempt to retrieve meal planning data via the API. Verify that allergy notes and sensitive medical fields are either absent from the response or returned as blank/redacted. Confirm that allergy data is classified as sensitive and encrypted at rest in the database. |
| **V14.1.2** | L2 | All sensitive data protection levels have a documented set of protection requirements covering encryption, retention, access controls, and privacy requirements. | **Test:** Review the data classification documentation and verify that each sensitivity level (e.g., PII, health data, credentials) has an explicit, documented protection requirement covering encryption at rest, encryption in transit, retention period, access control rules, and privacy-enhancing measures. |

###  In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V13.1.2** | L3 | Documentation defines maximum concurrent connection limits for each service dependency, with defined fallback behaviour to prevent denial of service. | **Test (DoS via Complex Meal Plans):** Submit a deliberately oversized or computationally complex payload to the nutritional calculation endpoint (e.g., a plan with thousands of variables). Validate that the API enforces a processing timeout and terminates the request with a controlled error, preventing resource exhaustion. Verify the timeout value and fallback behaviour match the documented service limits. |
 
---

## 2.5. Order Product

###  In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V2.3.1** | L1 | Business logic flows are processed only in the expected sequential step order; steps cannot be skipped. | **Test:** Attempt to skip a required step in the order placement flow (e.g., directly submit an order confirmation without completing the stock validation step). Verify the system rejects out-of-sequence requests and enforces the correct business flow order. |
| **V2.3.2** | L2 | Business logic limits are implemented per the application's documentation to prevent logic flaws being exploited. | **Test (Order Calculator Manipulation):** Submit invalid or irrational numeric parameters (e.g., negative quantities like `-500`, zero values, or alphabetic characters) into the order calculator form. The system must trigger a validation error before processing any calculation. Verify that all calculator inputs are server-side validated against the documented business rules. |
| **V2.3.5** | L3 | High-value business logic flows require multi-user approval to prevent unauthorized or accidental actions. | **Test:** Attempt to confirm a high-value order (above the defined threshold) using a single approver. Verify the system requires a second authorized party to approve before the order is finalized. Confirm the approval workflow cannot be bypassed by sending a direct API request. |
| **V2.2.2** | L1 | Input validation is enforced at the trusted server-side layer; client-side validation is not relied upon as a security control. | **Test:** Intercept an HTTP request sent after passing client-side validation and modify field values to invalid data (e.g., negative prices, disallowed characters). Submit the modified request directly to the server. Verify the server independently validates the input and rejects the tampered request. |
| **V15.4.1** | L3 | Shared objects in multi-threaded code are accessed safely using synchronization mechanisms to prevent race conditions. | **Test (Double Order Race Condition):** Fire two identical order-finalization requests in parallel using concurrent HTTP clients or tools (e.g., `ab`, `wrk`). The system must process exactly one transaction and drop the duplicate via an idempotency token. Verify no duplicate invoices are generated in the database. |
 
---

## 2.6. CI/CD and Pipeline Security

The following controls are implemented as automated steps within the **GitHub Actions** CI/CD pipeline. Each pipeline job enforces a specific security requirement and blocks the build or release if the check fails.

### Compliant

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V16.2.1** | L2 | Security-relevant pipeline events are logged with full metadata (who, what, when, where). | *(See §2.1 – Compliant – V16.2.1 above. Pipeline-specific context: every workflow run records the triggering actor, commit SHA, branch, and timestamp in the GitHub Actions audit log.)* |
| **V12.2.1** | L2 | TLS is enforced for all connectivity; no plaintext fallback. | *(See §2.1 – Compliant – V12.2.1 above. Pipeline-specific context: all pipeline steps that communicate with external services — NVD feeds, Docker registries, GitHub API — use HTTPS exclusively.)* |

###  In Progress

| ASVS ID | Level | Requirement | Test Plan |
| :--- | :---: | :--- | :--- |
| **V14.3.1** | L2 | Automated Software Composition Analysis (SCA) is performed on all dependencies and monitored against known CVEs. | **Test (Malicious Dependency Introduction):** Add a dependency version with a known CVE to `pom.xml`. Trigger the CI pipeline and validate that the OWASP Dependency Check job detects and reports the vulnerability in the generated HTML/SARIF report. Verify the job is configured to fail the build on HIGH or CRITICAL findings and uses an up-to-date NVD feed. |
| **V14.3.2** | L2 | Automated secret scanning is executed on every commit; exposed credentials are blocked from being merged. | **Test (Secrets Exposure):** Commit a fake but pattern-valid AWS key or JWT secret into a feature branch. Verify that the secret scanning workflow (e.g., GitHub Advanced Security, truffleHog) identifies the exposed credential and prevents the branch from being merged into a protected branch. |
| **V14.2.4** | L2 | SARIF security reports are uploaded to a centralized security dashboard for traceability and review. | **Test (Unvalidated Security Findings):** Trigger a Trivy container image scan that produces a finding. Verify the SARIF output file is automatically uploaded to the GitHub Security tab (Code Scanning Alerts). Confirm the finding is visible to the development team without requiring manual upload. |
| **V13.1.2** | L3 | Configuration files and infrastructure definitions are validated automatically before pipeline execution continues. | **Test (Pipeline Misconfiguration):** Introduce invalid YAML syntax or a semantic error (e.g., a missing required service) into the `docker-compose.yml` file. Verify that a `config-validation` CI job (e.g., `docker compose config`) detects the error and fails immediately, blocking subsequent pipeline stages. |
| **V2.2.2** | L1 | Input validation is enforced at the server layer; client-only validation is not relied upon. | **Test (Compromised Build Integrity — Unit Tests):** Introduce a failing security-related unit test (e.g., a test that verifies the JWT validation function rejects an unsigned token). Verify that the CI pipeline's test stage detects the failure and blocks artifact generation and merge approval. |
| **V2.4.1** | L2 | Anti-automation controls protect against denial-of-service via the pipeline and registration endpoints. | **Test (DAST — Runtime Validation):** Execute the OWASP ZAP baseline scan against the running production-like environment in the CI pipeline. Validate that vulnerable endpoints, missing HTTP security headers, or exposed debug paths are identified in the generated ZAP report. Verify the job is a required stage before release publication. |
| **V15.4.1** | L3 | Idempotency and locking mechanisms prevent duplicate transactions introduced by concurrent pipeline runs. | **Test (Release Artifact Integrity):** Validate that release artifacts are produced and uploaded exclusively by the GitHub Actions pipeline without any manual intervention step. Verify that no pipeline run can publish a release asset if any mandatory security stage (SAST, SCA, DAST, artifact scanning) has failed or been skipped. |
| **V8.1.1** | L1 | Authorization documentation defines rules restricting access based on permissions; release publication is access-controlled. | **Test (Unauthorized Release Publication):** Attempt to trigger the release publication workflow job using an account without repository release permissions. The GitHub Actions permission model and branch protection rules must block the operation. Verify the workflow uses `permissions: contents: write` scoped only to the release job. |
| **V1.2.4** | L1 | Parameterized queries / safe output encoding prevents injection in all dynamic constructs. | **Test (SAST — Static Code Analysis):** Integrate a SAST tool (e.g., SonarQube, CodeQL) into the pipeline. Commit code containing a deliberate SQL injection pattern (e.g., string concatenation in a query). Verify the SAST job detects the pattern and marks the build as failed, preventing the vulnerable code from reaching a protected branch. |
| **V5.3.2** | L1 | File paths use internally generated names; user-supplied filenames are not used in storage operations. | **Test (Bypassing Secure Development Lifecycle):** Attempt a direct push to the protected `main` branch without going through a Pull Request. Verify that GitHub branch protection rules reject the push. Additionally, attempt to merge a Pull Request with failing required CI security checks and confirm the merge is blocked. |
 
---