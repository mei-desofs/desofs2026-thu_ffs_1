# Phase 2: Sprint 1

------

### Table of Contents

## Introduction

This document covers the security engineering practices adopted during the development of the Cantinas de Cinfães backend 
— a Spring Boot REST API for managing canteen operations, including user authentication, meal scheduling, and email notifications.
The project follows a shift-left security approach, integrating security checks throughout the development 
lifecycle via three automated GitHub Actions pipelines: one for commits, one for Pull Requests, and one for 
Releases. Practices include SAST, SCA, DAST, secret scanning, artifact scanning, SBOM generation, and automated testing, 
all traceable to security requirements defined using the OWASP ASVS.

## Development

All the functionalities mentioned in Phase 1 are implemented (some with not all the requirements), constituting the basic operations
of create, read, update and delete. 

For this project, the following set of development good practices were adopted.

### Audits

In every controller of the BioCantinas App, there are various and relevant audits like the ones exemplified below.

### Code Reviews

In the repository, it is mandatory that every Pull Request has at least two approvals to be merged, at least a label 
associated and that all the checks are successful. When the PR is merged, it happens by squash and merge, meaning
all the commits in the PR are merged to the other branch as one single commit.

![img.png](pr.png)
![img_1.png](pr1.png)

### Repository and Team Rules

- There are 3 branches, dev, e2e and main:
  - dev is used for active development where the new functionalities are merged to.
  - e2e receives the code from dev and is ready to be tested in a more completed way
  - main represents the code that is actually fully validated and ready for production
- It is not possible to do a direct commit to branches dev, e2e and main. It can only be done through PRs.
- The code needs to be implemented in a branch with the naming /feature for new features or /bugfix to correct bugs and needs
to mention the issue. Example: feature/10-implement-release-workflows
- Every commit has to have an issue associated, otherwise the push won't be successful

## Build and Test

This section documents the quality and security practices adopted throughout the development lifecycle, providing evidence of their execution across the defined pipelines.

### Inventory of Components 

A Software Bill of Materials (SBOM) is generated automatically using the **CycloneDX Maven Plugin** to produce a complete and auditable inventory of all project dependencies and their versions.

The SBOM is generated in two moments:

- During every **Pull Request** (Security pipeline), producing `bom.json` uploaded as the `sbom` artifact.
- During every **Release** (DAST pipeline), producing a version-tagged `bom.json` uploaded as `final-sbom-<version>`.
```yaml
- name: Generate SBOM
  working-directory: cantinas-cinfaes-backend
  run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
- name: Upload SBOM
  uses: actions/upload-artifact@v4
  with:
    name: sbom
    path: cantinas-cinfaes-backend/target/bom.json
```

The output follows the CycloneDX standard (`bom.json`), listing every Maven dependency with its group ID, artifact ID, version, and known licenses. This inventory is used as input for Software Composition Analysis and serves as a compliance artifact for each release.

### Execution of Test Plans

#### Unit Tests

Unit tests are executed on every commit using the **Maven Surefire Plugin**. The `test` Spring profile is activated to replace the production MySQL database with an in-memory H2 instance, so no real infrastructure is required.

```yaml
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

Test reports are uploaded as artifacts (`unit-test-reports`) in every CI run, providing a full record of test results including pass/fail counts and execution times.

#### Integration Tests

Integration tests are executed on every Pull Request using the **Maven Failsafe Plugin**, after the SAST scan completes. They also use the `test` Spring profile with H2 to avoid requiring the real database.

```yaml
- name: Run Integration Tests
  working-directory: cantinas-cinfaes-backend
  run: mvn verify -DskipUnitTests -Dspring.profiles.active=test
  env:
    JWT_TOKEN: ${{ secrets.JWT_TOKEN }}
- name: Upload Integration Test Reports
  uses: actions/upload-artifact@v4
  with:
    name: integration-test-reports
    path: cantinas-cinfaes-backend/target/failsafe-reports
```

Reports are uploaded as artifacts (`integration-test-reports`) and are available for review after each Pull Request pipeline execution.

### Dynamic Analysis

Dynamic Application Security Testing (DAST) is performed on every release using **OWASP ZAP** (Zed Attack Proxy). Unlike static analysis, DAST tests the application while it is running, simulating real attacks against the live endpoints.

A MySQL 8 database is spun up as a service container, and the Spring Boot application is started and verified to be healthy before ZAP begins scanning.

```yaml
services:
  mysql:
    image: mysql:8
    env:
      MYSQL_ROOT_PASSWORD: testpassword
      MYSQL_DATABASE: cantinasDB
    ports:
      - 3306:3306
    options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=5
 
- name: Start Spring Boot application
  working-directory: cantinas-cinfaes-backend
  run: java -jar target/*.jar &
  env:
    SPRING_PROFILES_ACTIVE: test
    SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/cantinasDB
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: testpassword
 
- name: OWASP ZAP Baseline Scan
  uses: zaproxy/action-baseline@v0.14.0
  with:
    target: 'http://localhost:8080'
    fail_action: false
    issue_title: 'ZAP Baseline Scan Report'
```

The ZAP Baseline Scan tests the application for common web vulnerabilities such as those listed in the OWASP Top 10, including XSS, SQL Injection, insecure headers, and misconfigured CORS policies. Results are uploaded as artifacts in HTML, Markdown, and JSON formats (`zap-report`) and are also automatically created as a GitHub Issue for visibility.

### Configuration Validation

Configuration validation runs on every commit as part of the CI pipeline, ensuring that required configuration files are present and syntactically correct before any further checks run.

```yaml
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

- The first step verifies that at least one application configuration file (`application.properties` or `application.yml`) exists, preventing deployments with missing configuration.
- The second step runs `docker compose config` to validate the Docker Compose file syntax without starting any services, catching misconfigurations early.

### Artifact Scanning 

Artifact scanning analyses the final Docker image — the exact artifact that would be deployed to production — for known vulnerabilities in both the application dependencies and the underlying OS base image.

It is performed using **Trivy** in two pipelines:

- On every **Pull Request** (Security pipeline), as part of the `container-scanning` job.
- On every **Release** (DAST pipeline), as part of the `artifact-scanning` job.
```yaml
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

The image is built from the actual JAR artifact to replicate the production environment as closely as possible. Trivy scans for HIGH and CRITICAL CVEs across the application layer and the base image (e.g. `eclipse-temurin:21`). Results are reported in SARIF format and uploaded directly to the GitHub Security tab, where they are visible as code scanning alerts.

### Other Relevant Practices

**Static Application Security Testing (SAST)**

SAST is performed at two levels:

- **Semgrep** runs on every commit (CI pipeline) as a lightweight scan covering `p/java`, `p/owasp-top-ten`, and `p/secrets` rulesets.
- **CodeQL** runs on every Pull Request and Release, performing a deeper bytecode-level analysis of the compiled Java application. Results are uploaded to the GitHub Security tab.

**Software Composition Analysis (SCA)**

SCA is performed using the **OWASP Dependency Check Maven Plugin**, scanning all declared Maven dependencies against the NVD (National Vulnerability Database) for known CVEs.

It runs on every **Pull Request** and every **Release**, producing an HTML report uploaded as an artifact (`dependency-check-report` / `full-cve-report`).

**Secret Scanning**

Secret scanning is performed using a dedicated workflow (`secretDetector.yml`) to detect hardcoded credentials, API keys, tokens, and other sensitive values accidentally committed to the repository. This prevents secrets from reaching the codebase and is a complement to the `p/secrets` Semgrep ruleset used in the CI pipeline.

**Lint**

Code style is enforced on every commit using **Checkstyle** with the Google Java Style Guide (`google_checks.xml`). This catches formatting inconsistencies, naming violations, and other style issues early, before they reach a Pull Request.

## Pipeline

To ensure safety during the development of software, different pipelines were created, applying the concept of shift-left security with the 
purpose of detecting issues as soon as possible in the development cycle. This also saves GitHub Action resources and does not delay the development.

Therefore, 3 distinct pipelines were defined. One for commits, one for Pull Requests and other for Releases, each one with the checks appropriate for that specific context.

### CI Pipeline (commit)

The CI pipeline runs on every push to any branch. It is designed to be fast and provide immediate feedback, covering only the essential checks: build, lint, unit tests, basic SAST and configuration validation.

```yaml
on:
  push:
  workflow_dispatch:
```

#### Job 1: Build 

This job compiles the project and packages it, skipping tests to keep it fast. It is the foundation for all other jobs in the pipeline.

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

- `actions/cache@v4`: Caches the Maven local repository (`.m2`) to speed up subsequent runs by avoiding re-downloading dependencies.
- `-DskipTests`: Skips test execution at this stage since tests are handled in a dedicated job.

#### Job 2: Lint 

This job enforces code style rules using Checkstyle with the Google Java style guide, ensuring consistent code quality across the team.

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

- `needs: build`: Only runs after a successful build.
- `google_checks.xml`: Enforces the Google Java Style Guide, catching formatting inconsistencies, naming violations, and other style issues early.

#### Job 3: Unit Tests

Runs the project's unit tests using Maven Surefire

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

- `-Dspring.profiles.active=test`: Activates the `test` Spring profile, which uses an in-memory H2 database instead of the real MySQL instance, so no database secrets are required.
- `JWT_TOKEN`: The only secret needed, to allow JWT-related tests to run correctly.
- Test reports are uploaded as artifacts for later inspection.

#### Job 4: Basic SAST (Semgrep)

Performs a lightweight Static Application Security Testing scan using Semgrep, covering Java-specific rules, OWASP Top 10, and secret detection.

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

- `p/java`: Rules targeting common Java vulnerabilities.
- `p/owasp-top-ten`: Rules mapped to the OWASP Top 10 security risks.
- `p/secrets`: Detects hardcoded secrets and credentials in the codebase.

#### Configuration Validation

Validates that required configuration files exist and that the Docker Compose file is syntactically correct.

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

- Ensures the application configuration file is present before any deployment attempt.
- `docker compose config --quiet`: Validates the Docker Compose syntax without starting any services.

### Security Pipeline (pull request)

The Security pipeline runs on every Pull Request. It acts as a security gate before code is merged, running deeper and more time-consuming checks that would be too slow for every commit.

#### Job 1: SAST (CodeQL)

Performs deep Static Application Security Testing using GitHub CodeQL, which builds and analyses the compiled bytecode for security vulnerabilities.

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

- `permissions: security-events: write`: Required for CodeQL to upload findings to the GitHub Security tab.
- CodeQL requires a full build to analyse the compiled output, unlike Semgrep which analyses source code directly.
- Results are visible in the repository's Security tab as code scanning alerts.

#### SCA (OWASP Dependency Check)

Performs Software Composition Analysis by scanning all Maven dependencies against the NVD (National Vulnerability Database) for known CVEs.

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

- `NVD_API_KEY`: Used to authenticate against the NVD API for faster and more reliable CVE data retrieval.
- `continue-on-error: true`: Allows the pipeline to continue even if vulnerabilities are found, so the full report is generated and other jobs still run.
- `-DfailBuildOnCVSS=11`: Set above the maximum CVSS score of 10 so the build never hard-fails — the report is used for manual review instead.
- The HTML report is always uploaded as an artifact for inspection.


#### Job 3: SBOM (CycloneDX)

Generates a Software Bill of Materials listing all project dependencies and their versions, providing a full inventory of components.

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

- `makeAggregateBom`: Generates a single aggregated BOM covering all Maven modules.
- The output `bom.json` follows the CycloneDX standard, compatible with most security tools and auditing platforms.

#### Job 4: Integration Tests

Runs the integration test suite using Maven Failsafe.

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

- `needs: sast`: Integration tests only run after the SAST scan succeeds.
- `-DskipUnitTests`: Runs only integration tests (Failsafe), since unit tests (Surefire) are already covered in the CI pipeline.
- `-Dspring.profiles.active=test`: Uses the test profile with H2 in-memory database, avoiding the need for real database secrets.

#### Job 5: Container Scanning (Trivy)

Builds the Docker image and scans it for HIGH and CRITICAL vulnerabilities using Trivy, uploading results to the GitHub Security tab.

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

- The Docker image is built from the actual JAR to replicate the production artifact.
- `format: sarif`: Outputs results in SARIF format so they integrate natively with the GitHub Security tab.
- `severity: HIGH,CRITICAL`: Filters to only report the most impactful vulnerabilities.

### DAST Pipeline (release)

The Release pipeline runs when a new tag matching `v*` is pushed. It is the most comprehensive pipeline, covering publication of the release artifact followed by a full suite of security scans before the release is considered complete.

```yaml
on:
  push:
    tags:
      - "v*"
```

#### Job 1: Full Security Scan (CodeQL + Semgrep)

Runs a full SAST scan combining CodeQL and Semgrep, more thorough than the PR pipeline as it runs on every release.

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
    - name: Build for CodeQL
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

- `permissions: security-events: write`: Required for CodeQL to upload findings to the GitHub Security tab.
- Combines both CodeQL (bytecode analysis) and Semgrep (source code analysis) for maximum coverage.
- Triggered automatically when a GitHub Release is published.

#### Job 2: Full CVE Scan (OWASP DC + Trivy FS)

Runs a full Software Composition Analysis with OWASP Dependency Check and also scans the filesystem with Trivy to catch vulnerabilities in the OS layer and other files.

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

- Unlike the PR pipeline, this uses a lower `delay` and higher `maxRetryCount` for a more thorough NVD query.
- `scan-type: fs`: Trivy scans the entire filesystem, not just the Docker image, catching vulnerabilities in configuration files and scripts as well.

#### Job 3: Artifact Scanning (Trivy)

Builds the final Docker image and scans it for vulnerabilities, representing the exact artifact that would be deployed to production.

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

- Scans the final Docker image — the actual artifact that will be deployed — for HIGH and CRITICAL CVEs.
- Results are uploaded to the GitHub Security tab in SARIF format.

#### Job 4: Dynamic Analysis (OWASP ZAP)

Starts the full application with a real MySQL database and runs an OWASP ZAP baseline scan against it, testing the running application for common web vulnerabilities.

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

- A real MySQL 8 service is spun up as a sidecar container, with health checks to ensure it is ready before the application starts.
- The Spring Boot application is started in the background and the pipeline waits up to 120 seconds for it to become healthy via the `/actuator/health` endpoint.
- `fail_action: false`: ZAP findings are reported but do not fail the pipeline, allowing the release to proceed while flagging issues for review.
- ZAP reports are uploaded in HTML, Markdown, and JSON formats as artifacts.

#### Job 5: Final SBOM (CycloneDX)

Generates the final Software Bill of Materials for the release, providing a complete and auditable inventory of all components included in the published artifact.

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

- The SBOM is tagged with the release version via `github.ref_name` (e.g. `final-sbom-v1.0.0`), making it easy to correlate with a specific release.
- The `bom.json` in CycloneDX format serves as the official component inventory for the release, useful for compliance and security audits.

## Test Planning

## Conclusion
