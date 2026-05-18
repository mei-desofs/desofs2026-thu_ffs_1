# Phase 2: Sprint 1

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

## Development

All the functionalities mentioned in Phase 1 are implemented (some with not all the requirements), constituting the basic operations of create, read, update and delete.

For this project, the following set of development good practices were adopted.

### Audits

In every controller of the BioCantinas App, there are various and relevant audits like the ones exemplified below.

```java
log.info("Authentication attempt for email: {}", dto.getEmail());

LoginResponse loginResponse = authenticationService.login(dto);

if (loginResponse == null) {
    log.warn("Authentication failed for email: {}", dto.getEmail());
    return ResponseEntity.status(401).build();
}

HttpHeaders headers = new HttpHeaders();
headers.add(
    HttpHeaders.AUTHORIZATION,
    loginResponse.getTokenType() + " " + loginResponse.getToken()
);

UserDTO user = loginResponse.getUser();

if (user != null) {
    log.info(
        "User authenticated successfully. User id: {}, email: {}, role: {}",
        user.getId(),
        user.getEmail(),
        user.getRole()
    );
}
```

### Code Reviews

In the repository, it is mandatory that every Pull Request has at least two approvals to be merged, at least a label associated and that all the checks are successful. When the PR is merged, it happens by squash and merge, meaning all the commits in the PR are merged to the other branch as one single commit.

![img.png](images/pr.png)
![img_1.png](images/pr1.png)

### Repository and Team Rules

- There are 3 branches, `dev`, `e2e` and `main`:
    - `dev` is used for active development where new functionalities are merged.
    - `e2e` receives code from `dev` and is used for end-to-end validation in a more complete environment.
    - `main` represents the code that is fully validated and ready for production.
- Direct commits to `dev`, `e2e` and `main` are not allowed — all changes go through Pull Requests.
- Before merge to dev, branches must follow the naming convention `/feature` for new features or `/bugfix` for bug corrections, and must reference the associated issue. Example: `feature/10-implement-release-workflows`.
- Every commit must have an associated issue, otherwise the push will be rejected.

---

## Build and Test

This section summarises the quality and security practices adopted throughout the development lifecycle, organised by type of practice and indicating when each one runs. Implementation details and pipeline configuration are covered in the [Pipeline Automation](#pipeline-automation) section.

### Inventory of Components

A **Software Bill of Materials (SBOM)** is generated automatically using the CycloneDX Maven Plugin, producing a complete inventory of all Maven dependencies with their versions and licenses. It is generated on every Pull Request and on every Release, and uploaded as an artifact (`sbom` and `final-sbom-<version>` respectively).

### Execution of Test Plans

| Test Type | Plugin | Trigger | Report Artifact |
|---|---|---|---|
| Unit Tests | Maven Surefire | Every commit | `unit-test-reports` |
| Integration Tests | Maven Failsafe | Every Pull Request | `integration-test-reports` |

### Dynamic Analysis

Dynamic Application Security Testing (DAST) is performed on every Release using **OWASP ZAP**. The application is started against a real MySQL 8 database spun up as a service container, and ZAP runs a baseline scan against the live endpoints, testing for OWASP Top 10 vulnerabilities such as XSS, SQL Injection, and insecure headers. Results are uploaded as artifacts and automatically created as a GitHub Issue.

### Configuration Validation

On every commit, the CI pipeline verifies that the required `application.properties` (or `application.yml`) is present, and validates the Docker Compose file syntax using `docker compose config`, catching misconfigurations before they reach further stages.

### Artifact Scanning

The final Docker image is scanned for HIGH and CRITICAL CVEs using **Trivy**, covering both the application layer and the OS base image. This runs on every Pull Request and every Release, with results uploaded to the GitHub Security tab in SARIF format.

### Other Relevant Practices

| Practice | Tool | Trigger |
|---|---|---|
| SAST (lightweight) | Semgrep (`p/java`, `p/owasp-top-ten`, `p/secrets`) | Every commit |
| SAST (deep) | GitHub CodeQL | Every PR and Release |
| SCA | OWASP Dependency Check | Every PR and Release |
| Secret Scanning | Dedicated workflow (`secretDetector.yml`) | Every commit |
| Lint | Checkstyle (Google Java Style Guide) | Every commit |

**Note on IAST**

IAST (Interactive Application Security Testing) combines elements of SAST and DAST by instrumenting the application at runtime during test execution, detecting vulnerabilities with greater precision and fewer false positives than either approach alone. The three most common tools are:

| Tool | Vendor | Notes |
|---|---|---|
| **Contrast Security** | Contrast Security | Agent-based, deep runtime analysis, integrates with CI/CD |
| **Seeker** | Synopsys | Strong compliance reporting, integrates with existing test suites |
| **Hdiv Detection** | Hdiv Security | Lightweight agent, focused on OWASP Top 10 |

All three are commercial products with no free tier suitable for open-source or academic projects. For this reason, IAST was not implemented in this project. SAST (CodeQL + Semgrep) and DAST (OWASP ZAP) were adopted as open-source alternatives that together cover a similar range of vulnerability detection.


---

## Pipeline Automation

Three distinct pipelines were defined applying the concept of shift-left security, with checks appropriate for each stage of the development lifecycle.

### CI Pipeline (commit)

Runs on every push. Designed to be fast and provide immediate feedback.

```yaml
on:
  push:
  workflow_dispatch:
```

#### Job 1: Build

Compiles the project and packages it, skipping tests to keep the pipeline fast. All subsequent jobs depend on this one.

```yaml
build:
  name: Build
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: 21
        distribution: temurin
    - name: Cache Maven packages
      uses: actions/cache@v4
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    - name: Build
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
```

- `actions/cache@v4`: Caches the Maven local repository to avoid re-downloading dependencies on every run.
- `-DskipTests`: Tests are handled in a dedicated job.

#### Job 2: Lint (Checkstyle)

Enforces the Google Java Style Guide using Checkstyle.

```yaml
lint:
  name: Lint (Checkstyle)
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Run Checkstyle
      working-directory: cantinas-cinfaes-backend
      run: mvn checkstyle:check -Dcheckstyle.config.location=google_checks.xml
```

#### Job 3: Unit Tests

Runs unit tests using Maven Surefire and uploads the results as an artifact.

```yaml
unit-tests:
  name: Unit Tests
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Run Unit Tests
      working-directory: cantinas-cinfaes-backend
      run: mvn test
      env:
        JWT_TOKEN: ${{ secrets.JWT_TOKEN }}
    - name: Upload Unit Test Reports
      uses: actions/upload-artifact@v4
      with:
        name: unit-test-reports
        path: cantinas-cinfaes-backend/target/surefire-reports
```

#### Job 4: Basic SAST (Semgrep)

Lightweight static analysis covering Java vulnerabilities, OWASP Top 10, and secret detection.

```yaml
sast-basic:
  name: SAST Basic (Semgrep)
  runs-on: ubuntu-latest
  needs: build
  steps:
    - uses: actions/checkout@v4
    - uses: returntocorp/semgrep-action@v1
      with:
        config: >-
          p/java
          p/owasp-top-ten
          p/secrets
```

#### Job 5: Configuration Validation

Verifies that required configuration files exist and that Docker Compose is syntactically valid.

```yaml
config-validation:
  name: Configuration Validation
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Validate configuration files
      run: |
        test -f cantinas-cinfaes-backend/src/main/resources/application.properties \
          || test -f cantinas-cinfaes-backend/src/main/resources/application.yml
        echo "Config files present"
    - name: Validate Docker Compose
      run: |
        if [ -f docker-compose.yml ]; then
          docker compose config --quiet && echo "Docker Compose valid"
        fi
```

---

### Security Pipeline (pull request)

Acts as a security gate before code is merged, running deeper checks that would be too slow for every commit.

```yaml
on:
  pull_request:
  workflow_dispatch:
```

#### Job 1: SAST (CodeQL)

Deep bytecode-level static analysis. Results are uploaded to the GitHub Security tab.

```yaml
sast:
  name: SAST (CodeQL)
  runs-on: ubuntu-latest
  permissions:
    actions: read
    contents: read
    security-events: write
  steps:
    - name: Initialize CodeQL
      uses: github/codeql-action/init@v3
      with:
        languages: java
    - name: Build with Maven (for CodeQL)
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v3
      with:
        category: "/language:java"
```

- CodeQL requires a full build to analyse the compiled bytecode, unlike Semgrep which analyses source code directly.

#### Job 2: SCA (OWASP Dependency Check)

Scans all Maven dependencies against the NVD for known CVEs.

```yaml
sca:
  name: SCA (OWASP Dependency Check)
  runs-on: ubuntu-latest
  continue-on-error: true
  steps:
    - name: Run OWASP Dependency Check
      working-directory: cantinas-cinfaes-backend
      env:
        NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
      run: >
        mvn org.owasp:dependency-check-maven:check
        -Dnvd.api.key=$NVD_API_KEY
        -Dnvd.api.delay=6000
        -Dnvd.api.maxRetryCount=5
        -DfailBuildOnCVSS=11
    - name: Upload Dependency Check Report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: dependency-check-report
        path: cantinas-cinfaes-backend/target/dependency-check-report.html
```

- `continue-on-error: true`: The pipeline continues even if vulnerabilities are found, ensuring the full report is always generated.
- `-DfailBuildOnCVSS=11`: Set above the maximum CVSS score so the build never hard-fails — findings are reviewed manually from the uploaded report.

#### Job 3: SBOM (CycloneDX)

Generates a Software Bill of Materials for the PR, providing a component inventory.

```yaml
sbom:
  name: SBOM (CycloneDX)
  runs-on: ubuntu-latest
  needs: sca
  steps:
    - name: Generate SBOM
      working-directory: cantinas-cinfaes-backend
      run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
    - name: Upload SBOM
      uses: actions/upload-artifact@v4
      with:
        name: sbom
        path: cantinas-cinfaes-backend/target/bom.json
```

#### Job 4: Integration Tests

Runs integration tests using Maven Failsafe after SAST completes.

```yaml
integration-tests:
  name: Integration Tests
  runs-on: ubuntu-latest
  needs: sast
  steps:
    - name: Run Integration Tests
      working-directory: cantinas-cinfaes-backend
      run: mvn verify
      env:
        JWT_TOKEN: ${{ secrets.JWT_TOKEN }}
    - name: Upload Integration Test Reports
      uses: actions/upload-artifact@v4
      with:
        name: integration-test-reports
        path: cantinas-cinfaes-backend/target/failsafe-reports
```

#### Job 5: Container Scanning (Trivy)

Builds the Docker image and scans it for HIGH and CRITICAL vulnerabilities.

```yaml
container-scanning:
  name: Container Scanning (Trivy)
  runs-on: ubuntu-latest
  permissions:
    security-events: write
  steps:
    - name: Build JAR first
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Build Docker image
      run: docker build -t myapp:${{ github.sha }} cantinas-cinfaes-backend/
    - name: Trivy scan
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: myapp:${{ github.sha }}
        format: sarif
        output: trivy-results.sarif
        severity: HIGH,CRITICAL
    - uses: github/codeql-action/upload-sarif@v3
      with:
        sarif_file: trivy-results.sarif
```

- Results are uploaded in SARIF format, integrating natively with the GitHub Security tab as code scanning alerts.

---

### DAST Pipeline (release)

The most comprehensive pipeline, triggered when a GitHub Release is published. Runs a full suite of security scans against the released code and artifact.

```yaml
on:
  release:
    types: [published]
```

#### Job 1: Full Security Scan (CodeQL + Semgrep)

Combines CodeQL and Semgrep for maximum SAST coverage at release time.

```yaml
full-security-scan:
  name: Full Security Scan (CodeQL + Semgrep)
  runs-on: ubuntu-latest
  permissions:
    actions: read
    contents: read
    security-events: write
  steps:
    - name: Initialize CodeQL
      uses: github/codeql-action/init@v3
      with:
        languages: java
    - name: Build with Maven
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v3
    - name: Semgrep Full Scan
      uses: returntocorp/semgrep-action@v1
      with:
        config: >-
          p/java
          p/owasp-top-ten
          p/secrets
```

#### Job 2: Full CVE Scan (OWASP DC + Trivy FS)

Full SCA with OWASP Dependency Check plus a Trivy filesystem scan covering the OS layer and configuration files.

```yaml
full-cve-scan:
  name: Full CVE Scan (OWASP DC + Trivy FS)
  runs-on: ubuntu-latest
  needs: full-security-scan
  steps:
    - name: OWASP Dependency Check
      working-directory: cantinas-cinfaes-backend
      env:
        NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
      run: >
        mvn org.owasp:dependency-check-maven:check
        -Dnvd.api.key=${{ secrets.NVD_API_KEY }}
        -Dnvd.api.delay=2000
        -Dnvd.api.maxRetryCount=10
    - name: Trivy Filesystem Scan
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: fs
        scan-ref: .
        severity: HIGH,CRITICAL
    - name: Upload CVE Report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: full-cve-report
        path: cantinas-cinfaes-backend/target/dependency-check-report.html
```

- `scan-type: fs`: Scans the entire filesystem, going beyond declared dependencies to catch vulnerabilities in the OS layer and scripts.

#### Job 3: Artifact Scanning (Trivy)

Scans the final Docker image — the exact artifact deployed to production — for HIGH and CRITICAL CVEs.

```yaml
artifact-scanning:
  name: Artifact Scanning (Trivy)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  permissions:
    security-events: write
  steps:
    - name: Build JAR
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Build Docker image
      run: docker build -t myapp:${{ github.sha }} cantinas-cinfaes-backend/
    - name: Trivy image scan
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: myapp:${{ github.sha }}
        format: sarif
        output: trivy-results.sarif
        severity: HIGH,CRITICAL
    - uses: github/codeql-action/upload-sarif@v3
      with:
        sarif_file: trivy-results.sarif
```

#### Job 4: Dynamic Analysis (OWASP ZAP)

Starts the application against a real MySQL database and runs a ZAP baseline scan against the live endpoints.

```yaml
dast:
  name: Dynamic Analysis (OWASP ZAP)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  services:
    mysql:
      image: mysql:8
      env:
        MYSQL_ROOT_PASSWORD: testpassword
        MYSQL_DATABASE: cantinasDB
      ports:
        - 3306:3306
      options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=5
  steps:
    - name: Start Spring Boot application
      working-directory: cantinas-cinfaes-backend
      run: java -jar target/*.jar &
      env:
        SPRING_PROFILES_ACTIVE: test
        SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/cantinasDB
        SPRING_DATASOURCE_USERNAME: root
        SPRING_DATASOURCE_PASSWORD: testpassword
    - name: Wait for application to be ready
      run: |
        for i in {1..24}; do
          if curl -sf http://localhost:8080/actuator/health; then
            echo "Application is up!"
            exit 0
          fi
          sleep 5
        done
        exit 1
    - name: OWASP ZAP Baseline Scan
      uses: zaproxy/action-baseline@v0.14.0
      with:
        target: 'http://localhost:8080'
        fail_action: false
        issue_title: 'ZAP Baseline Scan Report'
```

- The pipeline waits up to 120 seconds for the application to become healthy via `/actuator/health` before ZAP begins.
- `fail_action: false`: Findings are reported without failing the pipeline, and are automatically created as a GitHub Issue.

#### Job 5: Final SBOM (CycloneDX)

Generates the official component inventory for the release, tagged with the version number.

```yaml
final-sbom:
  name: Final SBOM (CycloneDX)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  steps:
    - name: Generate Final SBOM
      working-directory: cantinas-cinfaes-backend
      run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
    - name: Upload Final SBOM
      uses: actions/upload-artifact@v4
      with:
        name: final-sbom-${{ github.ref_name }}
        path: cantinas-cinfaes-backend/target/bom.json
```

- Tagged with `github.ref_name` (e.g. `final-sbom-v1.0.0`), making it easy to correlate with a specific release.

#### Job 6: Release Publication

Creates the GitHub Release and publishes the JAR as a release asset. This job only runs after all security scans (`artifact-scanning`, `dast`, and `final-sbom`) have completed successfully, ensuring the release is only published once the artifact has passed all checks.

```yaml
release:
  name: Release Publication
  runs-on: ubuntu-latest
  needs: [artifact-scanning, dast, final-sbom]
  permissions:
    contents: write
  steps:
    - name: Build project
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Create GitHub Release
      uses: softprops/action-gh-release@v2
      with:
        files: cantinas-cinfaes-backend/target/*.jar
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

- `needs: [artifact-scanning, dast, final-sbom]`: The release is only published after all three parallel scanning jobs finish, making it the final gate of the pipeline.
- `softprops/action-gh-release@v2`: Creates the GitHub Release and attaches the built JAR as a downloadable release asset.

### Release Flow

The DAST pipeline is triggered by the `release: published` GitHub event, meaning it does not run on a tag push alone. The full flow to publish a release is:

**1. Create and push a tag locally:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

**2. Create the GitHub Release manually:**

Go to **Releases → Draft a new release**, select the tag `v1.0.0`, and publish it.

**3. The pipeline triggers automatically:**

The `release: published` event fires and the `dast.yml` pipeline starts, running all security scans in sequence.

**4. The JAR is attached to the release:**

Once all scans complete, the final `release` job runs and attaches the built JAR to the GitHub Release as a downloadable asset.

```
git tag v1.0.0 → git push origin v1.0.0
  └─► GitHub: create Release manually with that tag
        └─► dast.yml triggers (release: published)
              └─► full-security-scan
                    └─► full-cve-scan
                          ├─► artifact-scanning
                          ├─► dast (ZAP)
                          └─► final-sbom
                                └─► release (attaches JAR to GitHub Release)
```

This approach ensures the JAR is only attached to the release **after all security scans have passed**, preventing the publication of an artifact that has not been fully validated.

---

## Test Planning

# Security Testing Methodology and Threat Modelling - BioCantinas

This document establishes the security testing methodology, threat modelling review process, and abuse cases associated with the BioCantinas system flows. The approach is built upon the **OWASP ASVS 5.0 (Application Security Verification Standard)** and the **STRIDE** model, ensuring full traceability between identified threats, mitigations, and their respective validation criteria (conceptual tests).

---

## 1. Security Testing Methodology and Review Process

To validate the proposed mitigations without the immediate need for exhaustive code-specific test scripts (aligning with a security architecture and design perspective), a framework based on three pillars is adopted:

1. **Threat Modelling Review Process:** Threat modelling is a continuous process. Whenever a data flow or system component within BioCantinas is modified, the STRIDE matrix is reviewed to ensure that new attack vectors are anticipated and mitigated before implementation.
2. **Abuse Cases Definition:** The testing plan focuses on the system's behavior under attack (the unhappy path), describing the actions of a malicious actor attempting to break the business logic.
3. **Security Traceability:** Ensures a direct link between the Threat (STRIDE), the Mitigation (ASVS), and the Conceptual Test that validates whether the mitigation is working effectively.

---

## 2. Security Testing Plan by Flow

### 2.1. Authentication

| Threat | Targeted Element | STRIDE Category | Description | Mitigation (ASVS 5.0) | Abuse Case | Conceptual Verification / Test |
| :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| **Auth Bypass via SQL Injection** | Database Server | Tampering | Malicious input in login fields used to bypass the password check in the MySQL DB. | **V1.2.3**: Use parameterized queries and input sanitization for all database interactions. | An attacker inserts SQLi payloads (e.g., `' OR '1'='1`) into the login fields to bypass authentication. | **Test:** Submit SQL injection strings to the login endpoint. Validate that the API rejects the request with `401 Unauthorized` and does not expose database errors. |
| **SMTP/IMAP Injection via unsanitized input** | Mail System Interface | Tampering | User input (e.g., email fields, subject, headers) is not properly sanitized before being used in mail protocols, allowing attackers to inject additional commands or headers (e.g., adding recipients, altering message content). | **V1.3.11**: Sanitize and validate all user inputs used in email construction; strip CRLF characters; use safe mail libraries; enforce strict input validation and encoding; avoid direct concatenation into mail headers. | An attacker injects line breaks (`\r\n`) and headers like `Bcc:` into forms to use the mail server for sending SPAM. | **Test:** Send HTTP requests containing CRLF sequences in email text fields. Verify that the system sanitizes or blocks the malicious characters. |
| **Brute Force/Credential Stuffing** | Login Endpoint | Spoofing | Attackers use automated scripts to test leaked passwords against the BioCantinas API. | **V6.1.1**: Implement rate limiting, anti-automation, and account lockout after 3 failed attempts. | An attacker runs an automated script with hundreds of passwords against a single legitimate user account. | **Test:** Trigger multiple rapid, sequential login requests. Validate that the API blocks the IP/Account with `429 Too Many Requests` after the 3rd failed attempt. |
| **Weak passwords due to use of context-specific words** | Authentication System | Spoofing | Users create passwords using predictable, organization-related terms (e.g., company name, project names, roles), making them easier to guess or brute-force by attackers with knowledge of the organization. | **V6.1.2**: Maintain and enforce a deny-list of context-specific words (organization name, products, internal terms); integrate with password validation mechanisms; combine with strong password policies (length, complexity, banned passwords list); periodically update the list and document it clearly. | An attacker tries to guess credentials using easy words associated with the company's context (e.g., `BioCantinas123`). | **Test:** Attempt to create or change a password to a term included in the deny-list. The system must reject the submission and display a policy error. |
| **Account compromise via weak knowledge-based authentication** | Authentication System | Spoofing | The application uses password hints or security questions (e.g., “mother’s maiden name”), which are often easily guessable or obtainable via social engineering or public information, allowing attackers to bypass authentication. | **V6.4.1**: Eliminate password hints and security questions; replace with stronger recovery mechanisms (e.g., MFA, email-based reset with secure tokens, identity verification); follow modern authentication best practices. | An attacker gathers public data from the user's social media to answer security questions and steal account access. | **Test:** Architecturally validate the complete absence of "security question" fields and test the secure token-based reset flow via email. |
| **Account lockout or forced insecure recovery due to expired authentication factors** | Authentication System | Denial of Service | Users are not notified in advance about expiring credentials (e.g., passwords, tokens, certificates), leading to sudden expiration, potential service disruption, or reliance on less secure recovery processes. | **V6.4.5**: Send advance notifications before expiration (multiple reminders); implement automated renewal alerts; allow secure self-service renewal; define appropriate validity periods and grace windows. | A user's account expires abruptly, forcing them to use slow or insecure support channels to regain access. | **Test:** Verify the automatic transmission of expiration warnings in the system and test if the self-service flow allows early renewal. |
| **Unauthorized access due to admin-controlled password reset** | Authentication System | Elevation of Privilege | Administrative users can reset and set a user’s password directly, allowing them to know the password and potentially access the account without the user’s consent. | **V6.4.6**: Ensure admins can only trigger reset workflows (e.g., send reset link/token) without setting the password; enforce user-controlled password creation; log and audit all reset actions; apply least privilege principles. | A malicious Admin directly sets a known password for another user's account to log in covertly. | **Test:** Access the admin panel and validate that the Admin can only click "Send reset link", with no input field available to type a password. |
| **Unauthorized access due to lack of session inactivity timeout** | Session Management | Elevation of Privilege | The application does not enforce an inactivity timeout, allowing sessions to remain active indefinitely. An attacker with access to an unattended device or stolen session can continue to use the session without re-authentication. | **V6.7.3** Implement inactivity timeouts based on risk (e.g., shorter for sensitive operations); enforce automatic session expiration; require re-authentication after timeout; document timeout values and rationale; consider absolute session lifetime limits. | An attacker uses a computer left unattended with an active BioCantinas session at a counter for several hours. | **Test:** Leave a session token inactive beyond the specified timeout limit. Submit a new API request using that token and validate that it returns `401 Unauthorized`. |
| **Session Hijacking** | Browser Storage | Spoofing | An attacker steals an active session token from a shared computer's LocalStorage. | **V7.4.1**: Implement absolute session timeouts (20 min) and clear tokens upon logout. | An attacker inspects and copies the token from LocalStorage after a legitimate user leaves the browser. | **Test:** Click "Logout" and verify that the old token is immediately invalidated on the server, rejecting any subsequent HTTP requests. |
| **Privilege Escalation** | Admin API | Elevation of Privilege | A Supplier attempts to call the `ApproveSupplier` endpoint directly without Admin rights. | **V8.1.1**: Implement strict server-side Role-Based Access Control (RBAC). | A user with `Supplier` privileges sends a direct HTTP request to the Admin-only approval route. | **Test:** Authenticate as a `Supplier`, intercept the traffic, and submit a request to `/api/admin/ApproveSupplier`. The server must return `403 Forbidden`. |
| **JWT Payload Tampering** | Access Token | Tampering | A user modifies the claims in their JWT (e.g., changing role: `Dietitian` to `Admin`). | **V9.1.1**: Always validate the digital signature of the JWT before accepting its contents. | A user modifies the `role` claim in their token locally but lacks the secret key to correctly re-sign the token. | **Test:** Alter the `role` claim inside the JWT and send the modified request. The server must detect the invalid signature and reject the transaction. |
| **Credential Sniffing** | Network Traffic | Information Disclosure | Plaintext credentials (email/password) intercepted during transmission over the Internet. | **V12.1.1**: Enforce TLS 1.2/1.3 for all communications between Frontend and Backend. | An attacker sniffs network packets on a public Wi-Fi network to read passwords traveling without encryption. | **Test:** Attempt to perform a request using unencrypted HTTP (port 80). The server must reject the connection or force a redirect to HTTPS. |
| **Insecure Password Creation** | User Profile | Elevation of Privilege | Users choose easily guessable passwords, making accounts vulnerable to takeover. | **V6.2.1**: Enforce a 10-character minimum with uppercase, numbers, and special characters. | A user attempts to register an account by setting `12345` or `qwerty` as their password. | **Test:** Attempt to submit a weak or short password on the registration/profile form. The system must block creation and mandate the minimum criteria. |
| **Action Repudiation** | Audit Logs | Repudiation | An admin denies rejecting a valid supplier, and there is no trace of the specific action | **V16.2.1**: Ensure every security-relevant event includes metadata (Who, What, When, Where). | An admin rejects a legitimate supplier and denies doing so, leveraging the lack of detailed system audit logs. | **Test:** Perform a rejection action and validate in the database/log file that the event recorded the Admin's ID, timestamp, IP, and exact action. |

### 2.2. Supplier Approval

| Threat | Targeted Element | STRIDE Category | Description | Mitigation (ASVS 5.0) | Abuse Case | Conceptual Verification / Test |
| :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| **Registration Flooding (DoS)** | BioCantinas API | Denial of Service | An attacker submits thousands of fake applications or oversized files to exhaust CPU, Memory, or Database storage. | **V2.4.1**: Implement rate limiting and anti-automation (CAPTCHA) on the public registration endpoint. | An attacker uses a script to submit thousands of registration forms per second, trying to crash the server. | **Test:** Simulate a registration flood attack with concurrent requests. Validate that the rate-limiting mechanism blocks the excess traffic. |
| **Malicious File Upload** | BioCantinas API | Tampering | A supplier uploads a PDF containing a malicious script (XSS) or a virus. | **V5.2.2**: Validate magic bytes for PDF format and enforce a 5MB size limit, restrict extensions to **.PDF** | An attacker disguises an executable virus by changing its extension (e.g., from `script.exe` to `document.pdf`) and uploads it. | **Test:** Upload an executable file renamed to `.pdf` and a genuine PDF exceeding 5MB. The system must reject both cases. |
| **PDF Path Traversal** | Database | Tampering | An attacker uses a malicious filename during upload to overwrite critical system files. | **V5.3.2**: Use internally generated filenames (e.g., GUIDs) for storage instead of the original user-submitted name. | An attacker submits a document named `../../../../etc/passwd` to overwrite operating system files. | **Test:** Upload a file containing path traversal characters (`../`) in its name. Validate on the server that the file was saved using a random UUID instead. |
| **Unauthorized Approval Bypass** | Update Approval Status | Elevation of Privilege | An attacker or unauthorized user calls the approval endpoint directly to approve their own application without Admin review. | **V8.1.1**: Enforce strict server-side Role-Based Access Control (RBAC). Only the Administrator role can modify SupplierStatus. | A user attempts to send a status update request to `/api/supplier/status` to self-approve their application. | **Test:** Attempt to submit a status modification request authenticated as a non-admin account. The server must respond with `403 Forbidden`. |
| **Supplier Data Interception** | Internet Boundary | Information Disclosure | Sensitive data (NIF, Address, BIO Certificate) is intercepted in transit between the Supplier and the API. | **V12.2.1**: Mandate the use of TLS 1.2/1.3 (HTTPS) for all external communication. | An attacker intercepts network traffic to steal biological certificates and fiscal data submitted by suppliers. | **Test:** Inspect server response headers and verify that policies like HSTS force encrypted communication via TLS and prevent downgraded connections. |
| **Decision Email Tampering** | Email System Boundary | Tampering | An attacker intercepts the "Approved" email and replaces the credential setup link with a phishing URL. | **V12.3.1**: Secure communication with the Notification Service; REQ4.3: Use short-lived (24h) and single-use setup tokens. | An attacker intercepts or attempts to reuse an old credential configuration link sent via email to gain access. | **Test:** Attempt to use the same setup link twice or after 24 hours have passed. The system must treat the token as expired/invalid. |
| **Denial of Approval Action** | Administrator Action | Repudiation | An Administrator approves a supplier without a proper interview (violating REQ4.2) and later denies the action. | **V16.2.1**: Log all status changes with metadata: Actor ID, Target Supplier ID, Timestamp, and previous/new state. | An administrator improperly approves a partner without an interview and deletes or denies the action to avoid accountability. | **Test:** Perform an approval action and verify that the generated log entry contains the previous state, new state, and the ID of the responsible Admin. |

### 2.3. Supplier Management

| Threat | Targeted Element | STRIDE Category | Description | Mitigation (ASVS 5.0) | Abuse Case | Conceptual Verification / Test |
| :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| **Mass Data Exfiltration** | Database Server | Information Disclosure | Attackers use automated scripts to perform excessive API calls to dump the entire supplier database. | **V2.4.1**: Implement anti-automation and rate-limiting controls to prevent mass data exfiltration and ensure requests follow realistic human timing. | An attacker scrapes the API by continuously and rapidly querying all supplier profiles to dump the full database. | **Test:** Run an automated script making hundreds of read requests to the API. Validate that the system throttles the requests and limits the per-page volume. |
| **CSRF on Management Actions** | BioCantinas API | Tampering | An admin is tricked into clicking a link that triggers a hidden request to deactivate a supplier account. | **V4.1.4**: Ensure state-changing operations (Edit/Delete) require modern anti-CSRF protections or custom headers. | An authenticated administrator visits a malicious website that silently triggers an invisible request to delete a supplier in BioCantinas. | **Test:** Replicate an HTTP deletion request while omitting anti-CSRF tokens or custom application headers. The API must reject the request. |
| **Insecure Direct Object Reference (IDOR)** | BioCantinas API | Elevation of Privilege | An attacker (or a non-admin user) modifies the `supplierID` in the API request to edit a supplier they shouldn't access. | **V8.1.1**: Verify that authorization rules restrict data-specific access based on permissions. | Supplier A changes the ID in the HTTP request body from `101` to `102` to modify Supplier B's profile data. | **Test:** Authenticate as Supplier A and submit data changes targeting Supplier B's ID. The server must deny access with a `403` status. |
| **Unauthorized Account Deactivation** | Edit/Delete Process | Elevation of Privilege | A user with lower privileges calls the `DeleteSupplier` endpoint (REQ7.2) without being an Administrator. | **V8.2.2**: Ensure all management endpoints verify the `Administrator` role on every request. | A regular user attempts to directly invoke the `/api/supplier/delete` endpoint to deactivate an account. | **Test:** Send a deletion request authenticated with a regular user token (lacking privileges). Validate that the server responds with `403 Forbidden`. |
| **Race Condition** | Database Server | Tampering | Two admins edit the same supplier simultaneously, leading to data corruption or inconsistent states. | **V15.4.2**: Implement atomic operations or optimistic locking to prevent data corruption during concurrent edits. | Two administrators edit the exact same supplier record at the exact same moment, causing data corruption or inconsistent states. | **Test:** Send two concurrent update requests for the same record with conflicting data. The system must process one and reject the other. |
| **Administrative Repudiation** | Database Server | Repudiation | An admin deletes or changes a supplier's contract terms and later denies it; there is no record of the previous value. | **V16.2.1**: Log metadata (who, what, when) for every administrative "Write" operation on supplier data. | An admin changes a partner's contract terms and denies doing so, relying on the anonymity of unlogged modifications. | **Test:** Complete a contract modification and check the audit logs. Ensure that the old value, new value, and author's identity are correctly preserved. |
| **Sensitive Data Leakage in Logs** | System Logs | Information Disclosure | The API logs the full payload of a supplier update, accidentally including the supplier's private bio-certificate details or passwords. | **V16.2.5**: Ensure sensitive data is scrubbed or masked before being written to security logs. | An attacker gains access to log files and finds plaintext passwords or industrial secrets recorded due to over-verbose logging. | **Test:** Force a processing error involving confidential fields. Review the generated log entry and ensure that sensitive data is masked with `***`. |

### 2.4. Meal Planning Management

| Threat | Targeted Element | STRIDE Category | Description | Mitigation (ASVS 5.0) | Abuse Case | Conceptual Verification / Test |
| :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| **Menu Tampering** | API BioCantinas | Tampering | An attacker modifies dish of a menu. | **V1.2.3**: Validate and sanitize all inputs; **V8.1.1**: Restrict menu editing only to the `Dietitian` role. | A supplier attempts to modify weekly menu dishes to force the consumption of their own stock. | **Test:** Send a menu update request using credentials that do not belong to the `Dietitian` role. The server must reject the operation. |
| **Impersonation Publishing** | Publishing Process | Spoofing | An unauthorized user poses as a Dietitian to publish a fake or harmful meal plan. | **V9.1.1**: Validate JWT signature and `role` claim on every publishing request. | An attacker forges an HTTP header pretending to be a dietitian to approve and publish false or harmful menus. | **Test:** Submit a meal plan with a modified JWT token or one whose role claim is invalid. The system must validate the token signature and trigger an error. |
| **Specific Diet Data Leak** | Database Server | Information Disclosure | Unauthorized access to meal plans containing notes on allergies. | **V14.1.1**: Classify allergy data as sensitive and apply encryption at rest. | An attacker tries to query database tables or extract reports to expose allergy and sensitive medical notes belonging to other users. | **Test:** Attempt to download meal planning data via the API using a profile without clinical privileges. Ensure that health note fields are omitted or blank. |
| **DoS via Complex Plans** | BioCantinas System | Denial of Service | Mass submission of overly complex meal plans to exhaust the nutritional calculation processor. | **V13.1.2**: Implement timeouts and resource limits for heavy calculation processes on the API. | An attacker sends giant menus containing millions of nutritional variables to tie up the processor and freeze the application. | **Test:** Submit a deliberately heavy or complex payload to the calculation engine. Validate that the API terminates the process via a controlled timeout. |
| **Action Repudiation** | Audit Logs | Repudiation | A dietitian publishes a plan with severe errors and later denies being the author of the submission. | **V16.2.1**: Log detailed metadata (who, what, when) for every published or edited menu. | A dietitian mistakenly publishes a menu containing dangerous allergens and denies being the person who uploaded the file. | **Test:** Publish a menu and inspect the audit log entry, verifying the explicit presence of the responsible dietitian's ID. |

### 2.5. Order Product
# Phase 2: Sprint 1

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

## Development

All the functionalities mentioned in Phase 1 are implemented (some with not all the requirements), constituting the basic operations of create, read, update and delete.

For this project, the following set of development good practices were adopted.

### Audits

In every controller of the BioCantinas App, there are various and relevant audits like the ones exemplified below.

```java
log.info("Authentication attempt for email: {}", dto.getEmail());

LoginResponse loginResponse = authenticationService.login(dto);

if (loginResponse == null) {
    log.warn("Authentication failed for email: {}", dto.getEmail());
    return ResponseEntity.status(401).build();
}

HttpHeaders headers = new HttpHeaders();
headers.add(
    HttpHeaders.AUTHORIZATION,
    loginResponse.getTokenType() + " " + loginResponse.getToken()
);

UserDTO user = loginResponse.getUser();

if (user != null) {
    log.info(
        "User authenticated successfully. User id: {}, email: {}, role: {}",
        user.getId(),
        user.getEmail(),
        user.getRole()
    );
}
```

### Code Reviews

In the repository, it is mandatory that every Pull Request has at least two approvals to be merged, at least a label associated and that all the checks are successful. When the PR is merged, it happens by squash and merge, meaning all the commits in the PR are merged to the other branch as one single commit.

![img.png](images/pr.png)
![img_1.png](images/pr1.png)

### Repository and Team Rules

- There are 3 branches, `dev`, `e2e` and `main`:
    - `dev` is used for active development where new functionalities are merged.
    - `e2e` receives code from `dev` and is used for end-to-end validation in a more complete environment.
    - `main` represents the code that is fully validated and ready for production.
- Direct commits to `dev`, `e2e` and `main` are not allowed — all changes go through Pull Requests.
- Before merge to dev, branches must follow the naming convention `/feature` for new features or `/bugfix` for bug corrections, and must reference the associated issue. Example: `feature/10-implement-release-workflows`.
- Every commit must have an associated issue, otherwise the push will be rejected.

---

## Build and Test

This section summarises the quality and security practices adopted throughout the development lifecycle, organised by type of practice and indicating when each one runs. Implementation details and pipeline configuration are covered in the [Pipeline Automation](#pipeline-automation) section.

### Inventory of Components

A **Software Bill of Materials (SBOM)** is generated automatically using the CycloneDX Maven Plugin, producing a complete inventory of all Maven dependencies with their versions and licenses. It is generated on every Pull Request and on every Release, and uploaded as an artifact (`sbom` and `final-sbom-<version>` respectively).

### Execution of Test Plans

| Test Type | Plugin | Trigger | Report Artifact |
|---|---|---|---|
| Unit Tests | Maven Surefire | Every commit | `unit-test-reports` |
| Integration Tests | Maven Failsafe | Every Pull Request | `integration-test-reports` |

### Dynamic Analysis

Dynamic Application Security Testing (DAST) is performed on every Release using **OWASP ZAP**. The application is started against a real MySQL 8 database spun up as a service container, and ZAP runs a baseline scan against the live endpoints, testing for OWASP Top 10 vulnerabilities such as XSS, SQL Injection, and insecure headers. Results are uploaded as artifacts and automatically created as a GitHub Issue.

### Configuration Validation

On every commit, the CI pipeline verifies that the required `application.properties` (or `application.yml`) is present, and validates the Docker Compose file syntax using `docker compose config`, catching misconfigurations before they reach further stages.

### Artifact Scanning

The final Docker image is scanned for HIGH and CRITICAL CVEs using **Trivy**, covering both the application layer and the OS base image. This runs on every Pull Request and every Release, with results uploaded to the GitHub Security tab in SARIF format.

### Other Relevant Practices

| Practice | Tool | Trigger |
|---|---|---|
| SAST (lightweight) | Semgrep (`p/java`, `p/owasp-top-ten`, `p/secrets`) | Every commit |
| SAST (deep) | GitHub CodeQL | Every PR and Release |
| SCA | OWASP Dependency Check | Every PR and Release |
| Secret Scanning | Dedicated workflow (`secretDetector.yml`) | Every commit |
| Lint | Checkstyle (Google Java Style Guide) | Every commit |

**Note on IAST**

IAST (Interactive Application Security Testing) combines elements of SAST and DAST by instrumenting the application at runtime during test execution, detecting vulnerabilities with greater precision and fewer false positives than either approach alone. The three most common tools are:

| Tool | Vendor | Notes |
|---|---|---|
| **Contrast Security** | Contrast Security | Agent-based, deep runtime analysis, integrates with CI/CD |
| **Seeker** | Synopsys | Strong compliance reporting, integrates with existing test suites |
| **Hdiv Detection** | Hdiv Security | Lightweight agent, focused on OWASP Top 10 |

All three are commercial products with no free tier suitable for open-source or academic projects. For this reason, IAST was not implemented in this project. SAST (CodeQL + Semgrep) and DAST (OWASP ZAP) were adopted as open-source alternatives that together cover a similar range of vulnerability detection.


---

## Pipeline Automation

Three distinct pipelines were defined applying the concept of shift-left security, with checks appropriate for each stage of the development lifecycle.

### CI Pipeline (commit)

Runs on every push. Designed to be fast and provide immediate feedback.

```yaml
on:
  push:
  workflow_dispatch:
```

#### Job 1: Build

Compiles the project and packages it, skipping tests to keep the pipeline fast. All subsequent jobs depend on this one.

```yaml
build:
  name: Build
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: 21
        distribution: temurin
    - name: Cache Maven packages
      uses: actions/cache@v4
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    - name: Build
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
```

- `actions/cache@v4`: Caches the Maven local repository to avoid re-downloading dependencies on every run.
- `-DskipTests`: Tests are handled in a dedicated job.

#### Job 2: Lint (Checkstyle)

Enforces the Google Java Style Guide using Checkstyle.

```yaml
lint:
  name: Lint (Checkstyle)
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Run Checkstyle
      working-directory: cantinas-cinfaes-backend
      run: mvn checkstyle:check -Dcheckstyle.config.location=google_checks.xml
```

#### Job 3: Unit Tests

Runs unit tests using Maven Surefire and uploads the results as an artifact.

```yaml
unit-tests:
  name: Unit Tests
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Run Unit Tests
      working-directory: cantinas-cinfaes-backend
      run: mvn test
      env:
        JWT_TOKEN: ${{ secrets.JWT_TOKEN }}
    - name: Upload Unit Test Reports
      uses: actions/upload-artifact@v4
      with:
        name: unit-test-reports
        path: cantinas-cinfaes-backend/target/surefire-reports
```

#### Job 4: Basic SAST (Semgrep)

Lightweight static analysis covering Java vulnerabilities, OWASP Top 10, and secret detection.

```yaml
sast-basic:
  name: SAST Basic (Semgrep)
  runs-on: ubuntu-latest
  needs: build
  steps:
    - uses: actions/checkout@v4
    - uses: returntocorp/semgrep-action@v1
      with:
        config: >-
          p/java
          p/owasp-top-ten
          p/secrets
```

#### Job 5: Configuration Validation

Verifies that required configuration files exist and that Docker Compose is syntactically valid.

```yaml
config-validation:
  name: Configuration Validation
  runs-on: ubuntu-latest
  needs: build
  steps:
    - name: Validate configuration files
      run: |
        test -f cantinas-cinfaes-backend/src/main/resources/application.properties \
          || test -f cantinas-cinfaes-backend/src/main/resources/application.yml
        echo "Config files present"
    - name: Validate Docker Compose
      run: |
        if [ -f docker-compose.yml ]; then
          docker compose config --quiet && echo "Docker Compose valid"
        fi
```

---

### Security Pipeline (pull request)

Acts as a security gate before code is merged, running deeper checks that would be too slow for every commit.

```yaml
on:
  pull_request:
  workflow_dispatch:
```

#### Job 1: SAST (CodeQL)

Deep bytecode-level static analysis. Results are uploaded to the GitHub Security tab.

```yaml
sast:
  name: SAST (CodeQL)
  runs-on: ubuntu-latest
  permissions:
    actions: read
    contents: read
    security-events: write
  steps:
    - name: Initialize CodeQL
      uses: github/codeql-action/init@v3
      with:
        languages: java
    - name: Build with Maven (for CodeQL)
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v3
      with:
        category: "/language:java"
```

- CodeQL requires a full build to analyse the compiled bytecode, unlike Semgrep which analyses source code directly.

#### Job 2: SCA (OWASP Dependency Check)

Scans all Maven dependencies against the NVD for known CVEs.

```yaml
sca:
  name: SCA (OWASP Dependency Check)
  runs-on: ubuntu-latest
  continue-on-error: true
  steps:
    - name: Run OWASP Dependency Check
      working-directory: cantinas-cinfaes-backend
      env:
        NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
      run: >
        mvn org.owasp:dependency-check-maven:check
        -Dnvd.api.key=$NVD_API_KEY
        -Dnvd.api.delay=6000
        -Dnvd.api.maxRetryCount=5
        -DfailBuildOnCVSS=11
    - name: Upload Dependency Check Report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: dependency-check-report
        path: cantinas-cinfaes-backend/target/dependency-check-report.html
```

- `continue-on-error: true`: The pipeline continues even if vulnerabilities are found, ensuring the full report is always generated.
- `-DfailBuildOnCVSS=11`: Set above the maximum CVSS score so the build never hard-fails — findings are reviewed manually from the uploaded report.

#### Job 3: SBOM (CycloneDX)

Generates a Software Bill of Materials for the PR, providing a component inventory.

```yaml
sbom:
  name: SBOM (CycloneDX)
  runs-on: ubuntu-latest
  needs: sca
  steps:
    - name: Generate SBOM
      working-directory: cantinas-cinfaes-backend
      run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
    - name: Upload SBOM
      uses: actions/upload-artifact@v4
      with:
        name: sbom
        path: cantinas-cinfaes-backend/target/bom.json
```

#### Job 4: Integration Tests

Runs integration tests using Maven Failsafe after SAST completes.

```yaml
integration-tests:
  name: Integration Tests
  runs-on: ubuntu-latest
  needs: sast
  steps:
    - name: Run Integration Tests
      working-directory: cantinas-cinfaes-backend
      run: mvn verify
      env:
        JWT_TOKEN: ${{ secrets.JWT_TOKEN }}
    - name: Upload Integration Test Reports
      uses: actions/upload-artifact@v4
      with:
        name: integration-test-reports
        path: cantinas-cinfaes-backend/target/failsafe-reports
```

#### Job 5: Container Scanning (Trivy)

Builds the Docker image and scans it for HIGH and CRITICAL vulnerabilities.

```yaml
container-scanning:
  name: Container Scanning (Trivy)
  runs-on: ubuntu-latest
  permissions:
    security-events: write
  steps:
    - name: Build JAR first
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Build Docker image
      run: docker build -t myapp:${{ github.sha }} cantinas-cinfaes-backend/
    - name: Trivy scan
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: myapp:${{ github.sha }}
        format: sarif
        output: trivy-results.sarif
        severity: HIGH,CRITICAL
    - uses: github/codeql-action/upload-sarif@v3
      with:
        sarif_file: trivy-results.sarif
```

- Results are uploaded in SARIF format, integrating natively with the GitHub Security tab as code scanning alerts.

---

### DAST Pipeline (release)

The most comprehensive pipeline, triggered when a GitHub Release is published. Runs a full suite of security scans against the released code and artifact.

```yaml
on:
  release:
    types: [published]
```

#### Job 1: Full Security Scan (CodeQL + Semgrep)

Combines CodeQL and Semgrep for maximum SAST coverage at release time.

```yaml
full-security-scan:
  name: Full Security Scan (CodeQL + Semgrep)
  runs-on: ubuntu-latest
  permissions:
    actions: read
    contents: read
    security-events: write
  steps:
    - name: Initialize CodeQL
      uses: github/codeql-action/init@v3
      with:
        languages: java
    - name: Build with Maven
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v3
    - name: Semgrep Full Scan
      uses: returntocorp/semgrep-action@v1
      with:
        config: >-
          p/java
          p/owasp-top-ten
          p/secrets
```

#### Job 2: Full CVE Scan (OWASP DC + Trivy FS)

Full SCA with OWASP Dependency Check plus a Trivy filesystem scan covering the OS layer and configuration files.

```yaml
full-cve-scan:
  name: Full CVE Scan (OWASP DC + Trivy FS)
  runs-on: ubuntu-latest
  needs: full-security-scan
  steps:
    - name: OWASP Dependency Check
      working-directory: cantinas-cinfaes-backend
      env:
        NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
      run: >
        mvn org.owasp:dependency-check-maven:check
        -Dnvd.api.key=${{ secrets.NVD_API_KEY }}
        -Dnvd.api.delay=2000
        -Dnvd.api.maxRetryCount=10
    - name: Trivy Filesystem Scan
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: fs
        scan-ref: .
        severity: HIGH,CRITICAL
    - name: Upload CVE Report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: full-cve-report
        path: cantinas-cinfaes-backend/target/dependency-check-report.html
```

- `scan-type: fs`: Scans the entire filesystem, going beyond declared dependencies to catch vulnerabilities in the OS layer and scripts.

#### Job 3: Artifact Scanning (Trivy)

Scans the final Docker image — the exact artifact deployed to production — for HIGH and CRITICAL CVEs.

```yaml
artifact-scanning:
  name: Artifact Scanning (Trivy)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  permissions:
    security-events: write
  steps:
    - name: Build JAR
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Build Docker image
      run: docker build -t myapp:${{ github.sha }} cantinas-cinfaes-backend/
    - name: Trivy image scan
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: myapp:${{ github.sha }}
        format: sarif
        output: trivy-results.sarif
        severity: HIGH,CRITICAL
    - uses: github/codeql-action/upload-sarif@v3
      with:
        sarif_file: trivy-results.sarif
```

#### Job 4: Dynamic Analysis (OWASP ZAP)

Starts the application against a real MySQL database and runs a ZAP baseline scan against the live endpoints.

```yaml
dast:
  name: Dynamic Analysis (OWASP ZAP)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  services:
    mysql:
      image: mysql:8
      env:
        MYSQL_ROOT_PASSWORD: testpassword
        MYSQL_DATABASE: cantinasDB
      ports:
        - 3306:3306
      options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=5
  steps:
    - name: Start Spring Boot application
      working-directory: cantinas-cinfaes-backend
      run: java -jar target/*.jar &
      env:
        SPRING_PROFILES_ACTIVE: test
        SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/cantinasDB
        SPRING_DATASOURCE_USERNAME: root
        SPRING_DATASOURCE_PASSWORD: testpassword
    - name: Wait for application to be ready
      run: |
        for i in {1..24}; do
          if curl -sf http://localhost:8080/actuator/health; then
            echo "Application is up!"
            exit 0
          fi
          sleep 5
        done
        exit 1
    - name: OWASP ZAP Baseline Scan
      uses: zaproxy/action-baseline@v0.14.0
      with:
        target: 'http://localhost:8080'
        fail_action: false
        issue_title: 'ZAP Baseline Scan Report'
```

- The pipeline waits up to 120 seconds for the application to become healthy via `/actuator/health` before ZAP begins.
- `fail_action: false`: Findings are reported without failing the pipeline, and are automatically created as a GitHub Issue.

#### Job 5: Final SBOM (CycloneDX)

Generates the official component inventory for the release, tagged with the version number.

```yaml
final-sbom:
  name: Final SBOM (CycloneDX)
  runs-on: ubuntu-latest
  needs: full-cve-scan
  steps:
    - name: Generate Final SBOM
      working-directory: cantinas-cinfaes-backend
      run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
    - name: Upload Final SBOM
      uses: actions/upload-artifact@v4
      with:
        name: final-sbom-${{ github.ref_name }}
        path: cantinas-cinfaes-backend/target/bom.json
```

- Tagged with `github.ref_name` (e.g. `final-sbom-v1.0.0`), making it easy to correlate with a specific release.

#### Job 6: Release Publication

Creates the GitHub Release and publishes the JAR as a release asset. This job only runs after all security scans (`artifact-scanning`, `dast`, and `final-sbom`) have completed successfully, ensuring the release is only published once the artifact has passed all checks.

```yaml
release:
  name: Release Publication
  runs-on: ubuntu-latest
  needs: [artifact-scanning, dast, final-sbom]
  permissions:
    contents: write
  steps:
    - name: Build project
      working-directory: cantinas-cinfaes-backend
      run: mvn clean package -DskipTests
    - name: Create GitHub Release
      uses: softprops/action-gh-release@v2
      with:
        files: cantinas-cinfaes-backend/target/*.jar
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

- `needs: [artifact-scanning, dast, final-sbom]`: The release is only published after all three parallel scanning jobs finish, making it the final gate of the pipeline.
- `softprops/action-gh-release@v2`: Creates the GitHub Release and attaches the built JAR as a downloadable release asset.

### Release Flow

The DAST pipeline is triggered by the `release: published` GitHub event, meaning it does not run on a tag push alone. The full flow to publish a release is:

**1. Create and push a tag locally:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

**2. Create the GitHub Release manually:**

Go to **Releases → Draft a new release**, select the tag `v1.0.0`, and publish it.

**3. The pipeline triggers automatically:**

The `release: published` event fires and the `dast.yml` pipeline starts, running all security scans in sequence.

**4. The JAR is attached to the release:**

Once all scans complete, the final `release` job runs and attaches the built JAR to the GitHub Release as a downloadable asset.

```
git tag v1.0.0 → git push origin v1.0.0
  └─► GitHub: create Release manually with that tag
        └─► dast.yml triggers (release: published)
              └─► full-security-scan
                    └─► full-cve-scan
                          ├─► artifact-scanning
                          ├─► dast (ZAP)
                          └─► final-sbom
                                └─► release (attaches JAR to GitHub Release)
```

This approach ensures the JAR is only attached to the release **after all security scans have passed**, preventing the publication of an artifact that has not been fully validated.

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

## Secure Development Requirements Reference

The following practices, defined in the Secure Development Requirements section, underpin all controls above and are continuously enforced during development:

| Practice | Implementation | Verification |
| :--- | :--- | :--- |
| **Secure Coding Guidelines** | OWASP Secure Coding Practices applied across all critical forms (Menu, Reservations, Stock Operations, Supplier flows). | Code review checklists aligned to OWASP Top 10 are mandatory for all PRs touching authentication, data access, or external integrations. |
| **Dependency Management** | Third-party libraries monitored via OWASP Dependency Check in CI. Vulnerable `pom.xml` packages trigger build failure. | SCA report reviewed at each release. No unmaintained or unvetted packages merged without documented exception. |
| **Secure Code Review** | Security-focused review required for all changes to JWT logic, RBAC mechanisms, and MySQL data access layers. SonarQube assists but does not replace manual review. | PR approval gate requires at least one security-aware reviewer for critical components. |
| **SAST** | SonarQube / CodeQL integrated into GitHub Actions; scans run on every pull request and on every push to protected branches. | SAST stage is a required check; PRs with HIGH or CRITICAL findings are blocked from merging. |
| **Secret Management** | All secrets (JWT signing keys, MySQL connection strings, External Portal API certificates) stored exclusively in GitHub Secrets. Hardcoded values blocked by secret scanning. | Secret scanning runs on every commit; any detected credential pattern fails the pipeline and triggers an alert. |
| **Secure Logging** | Security events (authentication, unauthorized access attempts, External Portal errors) are logged without sensitive data. Log format validated per V16.2.x controls. | Log injection tests run as part of integration test suite. Sensitive data masking validated via V16.2.5 test procedure. |
| **Automated Security Tests** | Unit tests cover JWT generation/validation logic. Integration tests verify secure interactions between .NET 8 backend, MySQL, and External Portal API. End-to-end tests simulate real-world attack scenarios. | Security test suite runs in every CI build. Test failures block merge and artifact publication. Coverage reports reviewed at sprint close. |

## Conclusion

This sprint translated the threat model and ASVS 5.0 requirements from Phase 1 into concrete engineering practices, establishing the security baseline of the BioCantinas backend.
The three-pipeline architecture — commit, pull request, and release — operationalises shift-left security across the development lifecycle, combining SAST (Semgrep, CodeQL), SCA (OWASP Dependency Check), DAST (OWASP ZAP), container scanning (Trivy), secret scanning, and SBOM generation. No artifact reaches distribution without passing the full validation suite.
Of the 40 ASVS 5.0 controls mapped across the six functional areas, 22 are already compliant, covering the most critical controls: JWT integrity, RBAC enforcement, TLS communication, session management, file upload validation, and security logging. The remaining 18 have defined test plans and are prioritised for Sprint 2, notably rate limiting, session inactivity timeouts, and audience-restricted JWT validation.
The project demonstrates that a shift-left approach — anchoring every development decision to a traceable security requirement — is both achievable and effective, producing a codebase that is auditable, testable, and aligned with industry-standard verification criteria.