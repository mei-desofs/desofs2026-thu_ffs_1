# Security Configuration and Installation - Phase 2, Sprint 2

This document describes the secure deployment procedure introduced in Sprint 2. The application is shipped as a Docker image pushed to GitHub Container Registry (GHCR), deployed to a dedicated server by the [release pipeline](../PipelineAutomation/pipelineAutomation.md). All secrets are managed via GitHub Secrets and the server is treated as managed infrastructure.

## Table of Contents

1. [Deployment Topology](#1-deployment-topology)
2. [Container Hardening](#2-container-hardening)
3. [Secrets Management](#3-secrets-management)
4. [Installation Procedure](#4-installation-procedure)
5. [Configuration Management](#5-configuration-management)
6. [Deployment Traceability](#6-deployment-traceability)

---

## 1. Deployment Topology

The server hosts:

- The Docker container running the Spring Boot application, pulled from GHCR and exposed on port 80 (mapped to container port 8080).
- A MySQL 8.0 instance reachable by the container via the `DB_URL` environment variable.

The container is started with `docker run` by the deploy job over SSH, with all configuration injected as environment variables at runtime — no secrets are baked into the image.

---

## 2. Container Hardening

The production image is built from `cantinas-cinfaes-backend/Dockerfile`:

| | |
|---|---|
| Base image | `eclipse-temurin:21-jre-alpine` |
| OS patch step | `RUN apk update && apk upgrade --no-cache` |
| Exposed port | 8080 |

The JAR is compiled by the CI runner (`mvn clean package`) before the image is built; only the pre-built JAR is copied into the image. This keeps build tooling (Maven, JDK, source code) entirely off the runtime image.

Hardening controls in place:

- **OS packages patched at build time.** `apk update && apk upgrade --no-cache` runs as the first layer after the base image, ensuring Alpine system packages (including OpenSSL) are at their latest patched versions when the image is built. This resolved all High/Medium/Low Trivy CVEs identified in Sprint 2.
- **No build tooling at runtime.** The image contains only the JRE and the application JAR; Maven and the JDK are never present in the deployed image.
- **Minimal port exposure.** Only port 8080 is exposed; mapped to port 80 on the host.
- **Restart policy.** Container runs with `--restart unless-stopped`; restarts automatically on failure without operator intervention.
- **No credentials in image.** All secrets are injected as environment variables at `docker run` time; the image contains no credentials.

Image lifecycle on the server:

- The deploy job pulls the tagged image from GHCR (`ghcr.io/<owner>/cantinas-cinfaes-backend:<tag>`).
- The previous container is stopped and removed before the new one starts.
- Stale images are pruned with `docker image prune -f` after each deployment.

---

## 3. Secrets Management

All secrets are stored as GitHub repository secrets and injected into the workflow at runtime; nothing is committed to source control.

| Secret | Purpose | Sink |
|---|---|---|
| `DB_URL` | JDBC connection string | Passed to the container as `-e DB_URL` |
| `DB_USERNAME` | Database user | Passed to the container as `-e DB_USERNAME` |
| `DB_PASSWORD` | Database password | Passed to the container as `-e DB_PASSWORD` |
| `MAIL_USERNAME` | SMTP account | Passed to the container as `-e MAIL_USERNAME` |
| `MAIL_PASSWORD` | SMTP password | Passed to the container as `-e MAIL_PASSWORD` |
| `JWT_TOKEN` | JWT signing secret | Passed to the container as `-e JWT_TOKEN` |
| `RELEASE_PAT` | Fine-grained PAT for creating GitHub releases | Used only on the runner by `gh release create`; never reaches the server |
| `DEPLOY_HOST` | Target server hostname/IP | Used only by the SSH action on the runner |
| `DEPLOY_USER` | SSH username | Used only by the SSH action on the runner |
| `DEPLOY_SSH_KEY` | SSH private key | Used only by the SSH action on the runner; never written to disk |
| `DEPLOY_PORT` | SSH port | Used only by the SSH action on the runner |
| `NVD_API_KEY` | NVD API key for OWASP Dependency-Check | Used only by the OWASP DC Maven step; never reaches the server |
| `VIRUS_TOTAL` | VirusTotal API key for file scanning | Passed to the container as `-e VIRUS_TOTAL` |

Compliance points:

- No secret value is echoed in workflow logs; GitHub Actions automatically masks registered secrets.
- The runtime image does not embed any secret values; they are injected by `docker run` at container start.
- `RELEASE_PAT` has fine-grained permissions: `contents: write` and `actions: write` only.

---

## 4. Installation Procedure

The pipeline performs every install step automatically. For reference, the deploy job executes the following on the server over SSH:

```bash
# Pull the new image from GHCR
echo "<GITHUB_TOKEN>" | docker login ghcr.io -u <actor> --password-stdin
docker pull ghcr.io/<owner>/cantinas-cinfaes-backend:<tag>

# Stop and remove the existing container
docker stop cantinas-app 2>/dev/null || true
docker rm cantinas-app 2>/dev/null || true

# Start the new container with all secrets injected
docker run -d \
  --name cantinas-app \
  --restart unless-stopped \
  -p 80:8080 \
  -e DB_URL="<secret>" \
  -e DB_USERNAME="<secret>" \
  -e DB_PASSWORD="<secret>" \
  -e MAIL_USERNAME="<secret>" \
  -e MAIL_PASSWORD="<secret>" \
  -e JWT_TOKEN="<secret>" \
  -e VIRUS_TOTAL="<secret>" \
  ghcr.io/<owner>/cantinas-cinfaes-backend:<tag>

# Prune stale images
docker image prune -f
```

Manual prerequisites on the server (one-off):

1. Install Docker Engine.
2. Ensure the SSH key used by the workflow is authorised for the deploy user.
3. Provision a MySQL 8.0 instance and a least-privilege application database user.
4. Open port 80 on the host firewall.

---

## 5. Configuration Management

Production configuration is split into three layers:

| Layer | Source of truth | Notes |
|---|---|---|
| Application defaults | `application.properties` (committed) | Non-sensitive defaults: actuator exposure, mail host/port, email domain allow-list, file upload limits. |
| Runtime secrets | GitHub Secrets → SSH job → `docker run -e` | Database credentials, JWT secret, mail credentials. Not present in the image. |
| Test profile | `src/test/resources/application.properties` + `application-test.properties` | H2 in-memory DB auto-configured by Spring Boot (no explicit datasource URL in test profile); stub mail via default fallbacks. Activated by `-Dspring.profiles.active=test` in CI. Not deployed to production. |

Hardening highlights enforced through configuration:

- Actuator exposes only `/actuator/health` (`management.endpoints.web.exposure.include=health`).
- Email sender domain is restricted to an explicit allow-list (`app.email.allowed-domains`).
- File upload capped at 5 MB (`spring.servlet.multipart.max-file-size=5MB`).
- CORS and JWT validation enforced in Java configuration (`ConfigCORS`, `SecurityConfig`), not in `application.properties`.
- VirusTotal API key injected at runtime via `${VIRUS_TOTAL}` GitHub Secret; not committed to source.

---

## 6. Deployment Traceability

Each deployment is traceable through:

- **Tagged releases.** The release job in `dast.yml` creates a GitHub release tagged `v<version>` (main) or `v<version>-beta.<run>` (dev), with the JAR and SBOM (`bom.json`) attached as assets. The deployed image tag matches the release tag.
- **SBOM.** A CycloneDX `bom.json` is generated by the `final-sbom` job and attached to every release, providing a full inventory of third-party dependencies at release time.
- **Workflow logs.** Each release pipeline run records the image tag, the Git SHA (`--target`), and the SSH commands executed on the server.
- **Security scan gate.** The release job only runs after `artifact-scanning`, `dast`, and `final-sbom` all pass — ensuring no release is created from a build that failed security checks.
