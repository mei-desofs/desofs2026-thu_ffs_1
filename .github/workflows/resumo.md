## Resumo Geral do Fluxo das Pipelines

O projeto **CantinasCinfães** utiliza várias pipelines de **GitHub Actions** para automatizar processos de desenvolvimento, qualidade, segurança e release da aplicação.

Cada pipeline possui um objetivo específico e é executada em momentos diferentes do ciclo de desenvolvimento do projeto.

---





# 1. CI Pipeline (`ci.yml`)

### Quando executa?
- Sempre que existe um `push`
- Pode também ser executada manualmente

### Objetivo
Garantir que o código continua funcional e com qualidade antes de ser integrado no projeto.

### Fluxo

#### 1. Build
- Compila o projeto Spring Boot
- Verifica se o código gera corretamente o ficheiro `.jar`

#### 2. Lint (Checkstyle)
- Analisa estilo e qualidade do código
- Verifica regras e boas práticas Java

#### 3. Unit Tests
- Executa testes unitários automáticos
- Garante que funcionalidades individuais continuam corretas

#### 4. Basic SAST (Semgrep)
- Faz análise estática de segurança ao código
- Procura vulnerabilidades comuns:
    - SQL Injection
    - Secrets expostos
    - Problemas OWASP Top 10

#### 5. Configuration Validation
- Verifica ficheiros importantes:
    - `application.properties`
    - `application.yml`
    - `docker-compose.yml`

---

# 2. Security Pipeline (`security.yml`)

### Quando executa?
- Em cada `Pull Request`
- Manualmente

### Objetivo
Executar verificações avançadas de segurança antes de aceitar alterações no projeto.

### Fluxo

#### 1. SAST (CodeQL)
- Analisa o código Java profundamente
- Procura vulnerabilidades e más práticas

#### 2. SCA (OWASP Dependency Check)
- Analisa dependências Maven
- Procura CVEs conhecidos nas bibliotecas utilizadas

#### 3. SBOM Generation
- Gera uma lista completa das dependências do projeto
- Facilita auditorias de segurança

#### 4. Integration Tests
- Testa comunicação entre componentes:
    - Backend
    - Base de dados
    - APIs

#### 5. Container Scanning (Trivy)
- Analisa a imagem Docker
- Procura vulnerabilidades HIGH e CRITICAL

---

# 3. Release Pipeline (`release.yml`)

### Quando executa?
- Quando é publicada uma Release no GitHub

### Objetivo
Executar validações completas antes da versão final ser disponibilizada.

### Fluxo

#### 1. Full Security Scan
- Executa:
    - CodeQL
    - Semgrep
- Faz análise completa ao código

#### 2. Full CVE Scan
- Procura vulnerabilidades nas dependências
- Usa:
    - OWASP Dependency Check
    - Trivy

#### 3. Artifact Scanning
- Analisa o `.jar`
- Analisa a imagem Docker final

#### 4. DAST (OWASP ZAP)
- Executa testes dinâmicos à aplicação já em execução
- Simula ataques reais:
    - XSS
    - SQL Injection
    - Headers inseguros
    - Endpoints vulneráveis

#### 5. Final SBOM
- Gera SBOM final da release
- Guarda lista oficial de dependências da versão

---

# 4. Release Please Pipeline

### Quando executa?
- Sempre que existe `push` para `main`

### Objetivo
Automatizar releases e versionamento.

### O que faz?
- Analisa commits
- Cria Pull Requests de release
- Gera automaticamente:
    - Tags
    - Changelog
    - Releases GitHub

---

# 5. Label Reminder Pipeline

### Quando executa?
- Quando um Pull Request:
    - é criado
    - recebe/remova labels
    - recebe novos commits

### Objetivo
Garantir organização dos Pull Requests.

### O que verifica?
Cada PR deve ter:
- Uma label de tipo:
    - `bug`
    - `enhancement`
    - `documentation`

- Uma label SemVer:
    - `major`
    - `minor`
    - `patch`

### O que acontece?
- Se faltar alguma label:
    - o bot comenta automaticamente no PR
- Quando tudo estiver correto:
    - o comentário é removido

---

# 6. Secret Detector Pipeline

### Quando executa?
- Em Pull Requests

### Objetivo
Detetar secrets expostos no código.

### O que procura?
- Passwords
- API Keys
- Tokens JWT
- Chaves privadas
- Credenciais hardcoded

### Benefício
Evita exposição acidental de informação sensível no repositório.

---

# Resumo Final do Fluxo SSDLC

## Durante Desenvolvimento
➡️ `CI Pipeline`
- Build
- Lint
- Unit Tests
- SAST básico

---

## Antes de aceitar código (Pull Request)
➡️ `Security Pipeline`
- CodeQL
- Dependency Check
- Integration Tests
- Container Scan
- Secret Scan

➡️ `Label Reminder`
- Organização dos Pull Requests

➡️ `Secret Detector`
- Deteção de credenciais expostas

---

## Antes de uma Release
➡️ `Release Pipeline`
- Security Scan completo
- CVE Scan
- DAST
- Artifact Scanning
- SBOM final

➡️ `Release Please`
- Automatização da release e versionamento

---

# Objetivo Geral

Este fluxo permite implementar práticas de **Secure Software Development Life Cycle (SSDLC)** no projeto **CantinasCinfães**, garantindo:

- Qualidade do código
- Segurança contínua
- Deteção precoce de vulnerabilidades
- Controlo de dependências
- Proteção contra exposição de secrets
- Automatização de testes e releases
- Maior confiança antes de colocar software em produção


# Resumo Simples dos Termos de Segurança e CI/CD

| Termo | Explicação Simples |
|:------|:-------------------|
| **CI (Continuous Integration)** | Processo automático que verifica o código sempre que alguém faz alterações no projeto. |
| **Pipeline** | Conjunto de tarefas automáticas executadas pelo GitHub Actions. |
| **Build** | Compilação do projeto para verificar se o código funciona corretamente. |
| **Lint / Checkstyle** | Ferramenta que verifica organização, estilo e boas práticas do código. |
| **Unit Tests** | Testes automáticos a pequenas partes do código (funções/métodos). |
| **Integration Tests** | Testes que verificam comunicação entre vários componentes (API, DB, etc.). |
| **SAST (Static Application Security Testing)** | Análise de segurança ao código sem executar a aplicação. |
| **CodeQL** | Ferramenta GitHub que procura vulnerabilidades no código fonte. |
| **Semgrep** | Ferramenta de análise de segurança rápida baseada em regras OWASP. |
| **DAST (Dynamic Application Security Testing)** | Testes de segurança executados com a aplicação já a correr. |
| **OWASP ZAP** | Ferramenta DAST que simula ataques reais à aplicação. |
| **SCA (Software Composition Analysis)** | Análise de bibliotecas/dependências usadas no projeto. |
| **OWASP Dependency Check** | Ferramenta que procura vulnerabilidades conhecidas nas dependências Maven. |
| **CVE (Common Vulnerabilities and Exposures)** | Vulnerabilidade pública e conhecida numa biblioteca ou software. |
| **CVSS** | Sistema que dá uma pontuação de gravidade a vulnerabilidades. |
| **SBOM (Software Bill of Materials)** | Lista completa de dependências e bibliotecas usadas no projeto. |
| **CycloneDX** | Ferramenta usada para gerar o SBOM. |
| **Trivy** | Ferramenta que analisa vulnerabilidades em Docker e ficheiros. |
| **Container Scanning** | Verificação de vulnerabilidades em imagens Docker. |
| **Artifact Scanning** | Análise de ficheiros finais da aplicação, como `.jar` ou Docker images. |
| **Secret Scanning** | Procura passwords, tokens ou API keys expostas no código. |
| **JWT (JSON Web Token)** | Token usado para autenticação de utilizadores. |
| **TLS / HTTPS** | Comunicação segura e encriptada entre cliente e servidor. |
| **RBAC (Role-Based Access Control)** | Sistema de permissões baseado no papel do utilizador. |
| **OWASP Top 10** | Lista das vulnerabilidades web mais críticas. |
| **SQL Injection** | Ataque que tenta manipular queries SQL através de inputs maliciosos. |
| **XSS (Cross-Site Scripting)** | Ataque onde scripts maliciosos são executados no browser do utilizador. |
| **CSRF (Cross-Site Request Forgery)** | Ataque que força um utilizador autenticado a executar ações sem saber. |
| **DoS (Denial of Service)** | Ataque que tenta tornar a aplicação indisponível. |
| **MFA (Multi-Factor Authentication)** | Autenticação usando mais do que um fator (password + código, por exemplo). |
| **Rate Limiting** | Limitação do número de pedidos para evitar abuso/brute force. |
| **Docker** | Plataforma usada para correr aplicações em containers. |
| **GitHub Actions** | Ferramenta do GitHub para automação de pipelines CI/CD. |
| **Release** | Versão oficial publicada da aplicação. |
| **Changelog** | Histórico automático das alterações feitas entre versões. |
| **Release Please** | Ferramenta que automatiza versões, tags e releases. |





# Fluxo Geral das Pipelines — CantinasCinfães SSDLC

```text
                    ┌─────────────────────┐
                    │ Developer faz Push │
                    └──────────┬──────────┘
                               │
                               ▼

                    ┌─────────────────────┐
                    │     CI Pipeline     │
                    │      (ci.yml)       │
                    └─────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
     [Build]              [Lint]             [Unit Tests]
   Compila app       Verifica código      Executa testes

                               │
                               ▼

                        [Basic SAST]
                     Procura vulnerabilidades

                               │
                               ▼

                   [Config Validation]
              Verifica configs e Docker

                               │
                               ▼
                    Código pronto para PR

════════════════════════════════════════════════════════════

                 ┌────────────────────────┐
                 │ Pull Request Criado    │
                 └──────────┬─────────────┘
                            │
                            ▼

               ┌─────────────────────────┐
               │   Security Pipeline     │
               │    (security.yml)       │
               └─────────────────────────┘
                            │
     ┌──────────────────────┼──────────────────────┐
     ▼                      ▼                      ▼
 [CodeQL]             [Dependency Check]      [SBOM]
 SAST Avançado        CVEs em libs         Lista dependências

                            │
                            ▼

                  [Integration Tests]
                Testa comunicação real

                            │
                            ▼

                 [Container Scanning]
                 Analisa imagem Docker

════════════════════════════════════════════════════════════

            ┌────────────────────────────┐
            │ Pipelines Auxiliares PR    │
            └────────────────────────────┘

        ┌─────────────────┐
        │ Label Reminder  │
        └─────────────────┘
        Verifica labels obrigatórias

        ┌─────────────────┐
        │ Secret Detector │
        └─────────────────┘
        Procura passwords/tokens expostos

════════════════════════════════════════════════════════════

                 ┌──────────────────────┐
                 │ Merge para main      │
                 └─────────┬────────────┘
                           │
                           ▼

                ┌──────────────────────┐
                │ Release Please       │
                └──────────────────────┘
                Cria releases automáticas
                Gera changelog e tags

════════════════════════════════════════════════════════════

               ┌────────────────────────┐
               │ Release Publicada      │
               └──────────┬─────────────┘
                          │
                          ▼

               ┌────────────────────────┐
               │   Release Pipeline     │
               │     (release.yml)      │
               └────────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    ▼                     ▼                     ▼
[Full Security]     [Full CVE Scan]      [Artifact Scan]
 CodeQL/Semgrep       CVEs completas      Analisa JAR/Docker

                          │
                          ▼

                     [DAST]
            Simula ataques reais à app

                          │
                          ▼

                    [Final SBOM]
              Gera inventário final

════════════════════════════════════════════════════════════

                    ✅ Aplicação Segura
                 ✅ Release Validada
              ✅ SSDLC Completo Aplicado