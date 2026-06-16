# Security Assessment - Phase 2, Sprint 2

This document records the security assessment activities performed at the end of Phase 2. Per the rubric (Sprint 2, *Operate* criterion), it covers the full attack surface (UC1-UC7). Methodology and tooling come from [Sprint 1 Testing and Validation](../../Sprint1/TestingAndValidation/testingAndValidation.md); this report focuses on findings and per-use-case verdicts. Container and deployment-chain controls are documented in [Security Configuration and Installation](../SecurityConfigurationAndInstallation/securityConfigurationAndInstallation.md).

## Table of Contents

1. [Assessment Scope](#1-assessment-scope)
2. [Methodology](#2-methodology)
3. [Per-Use-Case Probes](#3-per-use-case-probes)
4. [Vulnerability Management](#4-vulnerability-management)
5. [Risk Evaluation](#5-risk-evaluation)
6. [Monitoring Considerations](#6-monitoring-considerations)
7. [Assessment Outcome](#7-assessment-outcome)

---

## 1. Assessment Scope

| Asset                                                    | Use case(s) | Sprint introduced | Re-assessed in Sprint 2 |
|----------------------------------------------------------|-------------|-------------------|-------------------------|
| Authentication and session management                    | UC1         | Sprint 1          | Yes                     |
| Password management and recovery                         | UC2         | Sprint 1          | Yes                     |
| Supplier application submission                          | UC3         | Sprint 2          | Yes                     |
| Supplier approval workflow                               | UC4         | Sprint 2          | Yes                     |
| Meal planning management                                 | UC5         | Sprint 2          | Yes                     |
| Product ordering                                         | UC6         | Sprint 2          | Yes                     |
| Supplier management                                      | UC7         | Sprint 2          | Yes                     |
| Deployment chain (GitHub Actions → GHCR → VM → Docker)   | n/a         | Sprint 2          | Yes (new)               |

Out of scope: external mail provider configuration (managed externally) and VM hardening below the Docker layer.

---

## 2. Methodology

The assessment combined five complementary techniques:

1. **SAST (CodeQL + Semgrep).** Run on every PR and push to `main`/`dev`; CodeQL results published to the GitHub Security tab; Semgrep covers OWASP Top 10 and secrets rulesets.
2. **SCA (OWASP Dependency-Check + Trivy FS).** OWASP Dependency-Check runs on every PR (threshold CVSS ≥ 11, informational only) and on every push to `main`/`dev` (threshold CVSS ≥ 7, build-breaking). Trivy filesystem scan runs only on push to `main`/`dev`, failing on HIGH/CRITICAL findings.
3. **Container Scanning (Trivy image).** Docker image scanned on every PR and on every push to `main`/`dev`; results uploaded to the GitHub Security tab as SARIF (non-blocking).
4. **DAST (OWASP ZAP Baseline Scan).** Unauthenticated baseline scan on push to `main`/`dev`, against an ephemeral CI environment (Spring Boot + MySQL service container).
5. **Targeted security scenarios.** Hand-written integration test cases per use case that ZAP cannot reach on its own (role enforcement, input validation, ownership checks).

Each technique remains automated in the security pipeline ([Pipeline Automation](../PipelineAutomation/pipelineAutomation.md)).

---

## 3. Per-Use-Case Probes

### UC1 – Authenticate in the system

Authentication was tested at both controller and service level using unit tests with mocks (`AuthenticationControllerTest`, `AuthenticationServiceTest`).

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Login with valid credentials | Verify authentication success and JWT issuance | Pass |
| Login with invalid credentials | Prevent unauthorized access | Pass |
| Non-existent user login | Prevent user enumeration and invalid access | Pass |
| Password mismatch validation | Ensure credential validation logic is enforced | Pass |
| JWT generation on successful login | Ensure session token creation | Pass |
| Authorization header returned correctly | Ensure token propagation to client | Pass |

#### Observations:
- Invalid credentials correctly trigger `InvalidCredentialsException`
- No authentication bypass paths were identified in the service logic
- JWT token is only generated after successful password validation
- Passwords are never exposed in responses

### UC2 – Password management and recovery

Password management was tested at both service and controller level using unit tests with mocks (`PasswordServiceTest`, `PasswordControllerTest`).

The tests validate password strength rules, password history enforcement, reset flows, token validation, and account activation logic.

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Validate strong password | Ensure password complexity requirements are enforced | Pass |
| Reject null/empty password | Prevent invalid password inputs | Pass |
| Reject common passwords | Prevent weak credential usage | Pass |
| Reject weak passwords | Enforce minimum security strength rules | Pass |
| Prevent reuse of last passwords | Enforce password history policy (last 5 passwords) | Pass |
| Change password with correct current password | Ensure authenticated password change flow | Pass |
| Reject incorrect current password | Prevent unauthorized password changes | Pass |
| Apply new encoded password | Ensure secure password storage (encoding) | Pass |
| Password expiry check (null date) | Enforce mandatory password update policy | Pass |
| Password expiry check (>6 months) | Ensure periodic password updates | Pass |
| Password expiry check (recent) | Allow valid sessions within policy | Pass |
| Send password reset email | Validate secure reset token generation and email delivery | Pass |
| Reject reset for unknown email | Prevent user enumeration via reset endpoint | Pass |
| Generate supplier activation token | Ensure secure onboarding flow for suppliers | Pass |
| Reset password with valid token | Validate secure token-based password reset | Pass |
| Reject already used token | Prevent replay attacks | Pass |
| Reject expired token | Enforce token expiration policy | Pass |
| Reject invalid token | Prevent unauthorized resets | Pass |
| Controller: missing fields validation | Ensure input validation at API level | Pass |
| Controller: user not found handling | Prevent invalid account operations | Pass |
| Account activation via token | Validate secure onboarding password setup | Pass |
| Check password expiry endpoint | Expose safe password expiry status | Pass |

---

#### Observations:

- Passwords must satisfy complexity rules (minimum length, uppercase, numeric, special character).
- Common and weak passwords are explicitly rejected.
- The system enforces password history (last 5 passwords cannot be reused).
- Password reset tokens are time-bound and single-use.
- Expired or reused tokens are correctly rejected.
- Passwords are always stored in encoded form using a `PasswordEncoder`.
- No plaintext password exposure was detected in any flow.
- User enumeration risks are reduced by generic responses in reset flows.
- Account activation requires secure token-based password setup.

### UC3 – Supplier application submission

Supplier application submission was tested at controller and service level using unit tests with mocks (`SupplierControllerTest`, `SupplierServiceTest`).

The tests validate file upload security, input validation, email validation, and virus scanning mechanisms.

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Apply to supplier position | Ensure valid supplier application submission | Pass |
| Missing certificate | Prevent incomplete application submission | Pass |
| Invalid certificate type (non-PDF) | Enforce file type restrictions | Pass |
| Certificate exceeding 5MB | Prevent oversized file upload attacks | Pass |
| Valid PDF upload | Ensure correct file processing | Pass |
| Email domain validation | Prevent invalid email input | Pass |
| Virus scan execution | Ensure malware scanning before storage | Pass |

---

#### Observations:

- Only PDF files are accepted for BIO certificates.
- File size is restricted to 5MB to prevent abuse.
- Empty or missing certificates are rejected.
- Uploaded files are scanned using an external virus scanning service.
- Email validation is enforced before persistence.
- Supplier application data is mapped securely between DTO and domain model.

---

### UC5 – Manage meal planning

Meal planning functionality was tested at controller and service level using unit tests with mocks (`MenuControllerTest`, `MenuServiceTest`).

The tests validate menu generation, publication rules, dish selection logic, stock validation, and access control for dietician operations.

---

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Get all menus | Ensure controlled access to menu data | Pass |
| Create menu | Validate menu creation workflow | Pass |
| Generate menu | Ensure automated menu generation logic integrity | Pass |
| Filter menus by week | Ensure correct date-based filtering | Pass |
| Publish menu | Ensure only authorized publication by dietician | Pass |
| Close menu | Ensure correct lifecycle state transition | Pass |
| Get planning statistics | Validate safe aggregation of planning data | Pass |
| Validate dietician existence | Prevent unauthorized menu creation | Pass |
| Reject non-dietician user | Enforce role-based access control | Pass |
| Generate menu with valid stock | Ensure dish selection respects product availability | Pass |
| Reject menu generation without stock | Prevent invalid menu creation | Pass |
| Publish menu without valid dietician | Prevent unauthorized publishing | Pass |
| Close menu transitions | Ensure correct state management | Pass |

---

#### Observations:

- Menu creation is restricted to valid dieticians only.
- Role validation is enforced before allowing menu creation or publication.
- Menu generation logic ensures that only dishes with available stock are selected.
- Dish availability depends on seasonal product constraints.
- Menu lifecycle is strictly controlled (GENERATED → PUBLISHED → CLOSED).
- Planning statistics are derived from repository data without exposing internal entities.
- Date-based filtering ensures users only access relevant weekly menus.

---

### UC6 – Order products from suppliers

Product management functionality was tested at controller and service level using unit tests with mocks (`ProductControllerTest`, `ProductServiceTest`).

The tests validate product retrieval, filtering of seasonal products, counting logic, and business-rule calculations.

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Get all products | Ensure controlled retrieval of product catalog | Pass |
| Get seasonal products | Ensure correct business filtering logic | Pass |
| Get product count | Validate safe aggregation of product data | Pass |
| Product mapping (entity → DTO) | Ensure safe data transformation layer | Pass |
| Calculate organic product percentage | Validate business rule logic integrity | Pass |

---

### UC4-UC7 – Supplier management and administration

Supplier management functionality was tested at service level using unit tests with mocks (`SupplierServiceTest`).

The tests cover supplier approval, rejection, data retrieval, quarantine mechanisms, and filtering operations.

#### Security probes executed:

| Test | Security objective | Result |
|---|---|---|
| Approve supplier application | Ensure only valid applications are approved | Pass |
| Reject supplier application | Ensure proper rejection workflow with reason | Pass |
| Application not found | Prevent invalid approval operations | Pass |
| Bio certificate retrieval | Secure access to stored sensitive file | Pass |
| Supplier statistics | Ensure safe aggregation of system data | Pass |
| Find all suppliers | Validate controlled data exposure | Pass |
| Find suppliers by product | Ensure correct filtering logic | Pass |
| Find all applications | Ensure safe access to application data | Pass |
| Quarantine supplier | Enforce safety isolation mechanism | Pass |
| Unquarantine supplier | Restore supplier availability securely | Pass |
| Filter suppliers by name | Validate search functionality | Pass |
| Filter suppliers by village | Validate geographic filtering | Pass |
| Filter suppliers by municipality | Validate regional filtering logic | Pass |

---

#### Observations:

- Supplier approval is strictly dependent on interview status.
- Rejection triggers email notification with a reason.
- Supplier quarantine propagates to associated product batches.
- Only valid supplier applications can be approved.
- BIO certificates are retrieved in controlled binary format (PDF).
- Supplier filtering does not expose unauthorized data.
- All operations are service-layer controlled (no direct repository exposure in controller).

---

## 4. Vulnerability Management

The Sprint 2 dependency tree was scanned against NVD/CISA feeds (OWASP Dependency-Check). Since Sprint 1, the Spring Boot version was upgraded from 3.3.4 to 3.5.15, resolving all previously reported CVEs across Tomcat, Spring Security, Spring Framework, and transitive log4j artifacts.

| | Sprint 1 | Sprint 2 |
|---|---|---|
| Reported CVEs (CVSS ≥ 7) | 39 | 0 |
| Critical (CVSS ≥ 9) | 14 | 0 |
| High (CVSS 7–8.9) | 25 | 0 |
| Build-breaking (CVSS ≥ 7) | Yes | No |

Key dependency versions in Sprint 2:

| Dependency | Version | Notes |
|---|---|---|
| Spring Boot | 3.5.15 | All Spring Boot CVEs patched |
| Tomcat Embed Core | 10.1.55 | All Tomcat CVEs patched |
| Spring Security | 6.5.11 | All Spring Security CVEs patched |
| Spring Framework | 6.2.19 | All Spring Framework CVEs patched |
| log4j-to-slf4j / log4j-api | Patched (via BOM) | Spring Boot 3.5.15 BOM manages transitive log4j artifacts to patched versions; no CVEs reported |

---

## 5. Risk Evaluation

| Residual risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| GitHub Secret leakage by misconfigured workflow | Low | High | Secrets consumed only in workflow steps, never echoed. Tokens are masked. PR review catches accidental dumps. |
| New CVEs published against current dependency versions | Low | Medium | OWASP DC and Trivy re-scan on every push; pipeline fails on CVSS ≥ 7. |
| Email credential exposure via SMTP configuration | Low | Medium | `MAIL_USERNAME` and `MAIL_PASSWORD` stored as GitHub Secrets; never committed to source. |
| JWT secret compromise | Low | High | `JWT_TOKEN` stored as GitHub Secret; never embedded in image. Rotation requires re-deploy. |
| ZAP false positives masking real findings | Low | Low | Rule 10049 (Non-Storable Content) suppressed in `.zap/rules.tsv`; all other rules remain active. |

---

## 6. Monitoring Considerations

The Sprint 2 deployment is API-only; monitoring stays lean:

- **Application logs.** Spring Boot logs to stdout; captured by the Docker logging driver in production.
- **Health endpoint.** `/actuator/health` is the only exposed Actuator endpoint; used by the container healthcheck and the release pipeline readiness check.
- **Pipeline alerting.** CodeQL HIGH/CRITICAL alerts fail the build; ZAP warnings fail the build unless suppressed in `.zap/rules.tsv`; OWASP DC fails on CVSS ≥ 7.

Items intentionally out of scope (acknowledged in the rubric for Sprint 2): centralised log aggregation, SIEM integration, automated incident response.

---

## 7. Assessment Outcome

| | |
|---|---|
| Sprint | Sprint 2 |
| Use cases assessed | UC1-UC7 (all) |
| Overall result | Pass |
| Critical / High findings (CVSS ≥ 7) | 0 |
| Medium findings | 0 |
| Low findings | 1 (Non-Storable Content on unauthenticated endpoints, suppressed in `.zap/rules.tsv`) |
| Accepted CVEs | 0 (all resolved by upgrading to Spring Boot 3.5.15) |
| Build-breaking issues | 0 |

The pipeline policy (`fail_action: true` on ZAP, CVSS ≥ 7 build break on OWASP DC, fail on CodeQL HIGH) remained blocking and stayed green throughout Sprint 2.
