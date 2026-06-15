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

| Asset | Use case(s) | Sprint introduced | Re-assessed in Sprint 2 |
|---|---|---|---|





FAZER O RESTO 




| Deployment chain (GitHub Actions → GHCR → VM → Docker) | n/a | Sprint 2 | Yes (new) |

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






FAZER







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
