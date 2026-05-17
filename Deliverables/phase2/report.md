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

### Code Reviews

In the repository, it is mandatory that every Pull Request has at least two approvals to be merged, at least a label associated and that all the checks are successful. When the PR is merged, it happens by squash and merge, meaning all the commits in the PR are merged to the other branch as one single commit.

![img.png](pr.png)
![img_1.png](pr1.png)

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

---

## Test Planning

## Conclusion