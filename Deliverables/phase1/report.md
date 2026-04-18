# Phase 1: 

------

## Table of Contents

------

## Introduction

The present report has the purpose to document the application of the first two steps of the Secure Software Development Life Cycle (SSDLC) 
process: Analysis/Requirements and Design, in the system BioCantinas, a software solution to promote healthy eating and 
sustainable food education, to reduce food waste and to help the business of local producers. ´

In the first step of the SSDLC process, it will be done a study on the functional and non-functional requirements, secure development
requirements and use and abuse cases.

The second step evolves around the Threat Modeling analysis that aims to identify, categorize, and recommend mitigations 
for potential security vulnerabilities before they can be exploited in a production environment.

This report also explores matters like a STRIDE-based threat analysis tables with detailed mitigations for the identified threats,
Data Flow Diagrams (DFD) and demonstrations of the Use and Abuse Cases.

To identify and to address the security risks and vulnerabilities in the beginning of the development lifecycle is holds
a great importance and adds value to software development. Trough SSDLC it is possible to a secure and robust platform that protects sensitive data while maintaining
the integrity of the BioCantinas system, preventing potential security breaches and consistency.

## Project Analysis

### Project Description

This project, originally proposed by the Municipality of Cinfães exposes the initiative of the preparation and distribution of meals
prepared with fresh and fully organic products, grown by the local agriculture producers of the municipality. The application, intended to
be used in school and nursing home environments, aims to facilitate the process of supplier contracting,
product procurement, and meal planning. The meals are cooked in Cinfães's central canteen with the organic products
received and then distributed to the other canteens, according to the demand of each one.

BioCantina's main goals are to promote local agriculture business, to encourage healthy eating habits in the community, to
monitor and prevent food waste by providing accurate meal planning and inventory management, and to contribute to the 
sustainability of the local food system.

The application will provide a smooth flow throughout the entire different processes, from the initial meal planning, to the procurement of ingredients, 
to the preparation and distribution of meals to assure an efficient and sustainable system.

### Domain Model


### Entry and Exit Points

#### Entry Points
Entry points represent the interfaces through which data enters the system from external sources:

*API Endpoints*
 - Authentication endpoints for user login and password recovery
 - Supplier application endpoints (for unregistered suppliers)
 - Menu management endpoints 
 - Meal reservation endpoints 
 - Delivery management endpoints 
 - Stock management endpoints 
 - Payment and transaction endpoints

*File Upload Interfaces*
 - Supplier certification document upload endpoints 
 - Product or delivery-related document submission endpoints

*Database Connection*
 - Secure connection to the database server
 - Authentication mechanisms for API-to-database communication
 - Encrypted storage of sensitive data (in compliance with GDPR)


#### Exit Points
Exit points represent interfaces through which data leaves the system:

*API Responses*
 - JSON data responses to client requests
 - Authentication tokens issued to authenticated clients
 - Error messages and validation results
 - Success/failure status codes for operations

*Database Operations*
 - SQL queries to store, retrieve, or modify data
 - Database backup operations

*System Logs*
 - API access logs with operation details
 - Security event logs
 - Error and exception logs


### Application Users

*Unregistered User*:
 - User who accesses the platform without authentication.
 - Has limited access to the system functionalities.
 - Can apply to the Supplier Program to become a registered supplier.
 - Cannot access protected areas or personalized features.

*Administrator*:
 - Registered user with full access to the system.
 - Responsible for keeping the system efficient, maintainable, and operational.
 - Manages users, suppliers, and overall system configurations.
 - Oversees platform performance and administrative processes.

*Dietitian*:
 - Registered user responsible for planning weekly meals.
 - Creates and publishes menus on the platform.
 - Ensures menus include nutritional and allergen information.
 - Supports healthy and sustainable meal planning.

*Central Canteen Manager*:
 - Registered user responsible for receiving organic products from suppliers.
 - Manages stock, including product quantities and availability.
 - Oversees meal preparation according to the planned menus.
 - Ensures proper coordination between supply and meal production.


### Use Cases

### Requirements

TODO functional and non functional requirements

### Non-Functional Requirements

The non-functional requirements define the quality attributes and technical constraints that the **CantinasCinfães** system must satisfy to ensure that the platform is secure, reliable, efficient, and easy to use and maintain. These requirements support the operation of school and nursing home canteens, as well as the interaction between consumers, suppliers, dietitians, canteen managers, and administrators.

## 1. Performance

The system must provide adequate response times for the main operations, such as user authentication, menu consultation, meal reservation, stock updates, delivery validation, and report generation.

- The backend API must respond to requests in under 3 second under normal operating conditions.
- The system must support at least 100 requests per second in normal production usage.
- The system must remain responsive during peak periods, especially when menus are published or reservation deadlines are close.

## 2. Availability

The system must be accessible whenever users need to perform operational and planning tasks.

- The platform must be available 24/7, except during scheduled maintenance periods.
- The system must achieve at least 99% availability during regular operating hours.
- Essential functionalities, such as viewing menus, reserving meals, validating deliveries, and consulting stock, must remain available even in case of partial failures.
- The infrastructure must ensure stable operation for all supported schools and nursing homes.

## 3. Scalability

The system must support future growth in the number of users, canteens, suppliers, reservations, and stored data.

- The architecture must support horizontal scaling of backend services.
- The system must be able to handle up to 3 times the average number of active users without significant performance degradation.
- The platform must support the addition of new schools, nursing homes, and suppliers without major architectural changes.
- Storage and processing capacity must be adjustable according to operational demand.

## 4. Security

The system must protect personal data, operational data, and payment-related information against unauthorized access and common attacks.

- All communication between client, server, and external systems must be protected through HTTPS/TLS.
- Authentication must use secure session management and expiring tokens.
- The system must enforce role-based access control according to the defined roles:
    - System Administrator
    - Supplier
    - Central Canteen Manager
    - Canteen Manager
    - Dietitian
    - Consumer
- Users must only access the data and operations allowed by their role.
- Passwords must be stored using strong hashing algorithms.
- The system must include protection against common attacks such as:
    - SQL Injection
    - Cross-Site Scripting (XSS)
    - Cross-Site Request Forgery (CSRF)
    - Brute-force login attempts
- The system must comply with GDPR requirements and ensure the protection of personal and sensitive data.

## 5. Reliability and Integrity

The system must guarantee the correctness, consistency, and persistence of critical operations.

- Operations such as meal reservations, reservation cancellations, delivery validation, stock updates, and payment registration** must be processed reliably.
- Data related to reservations, stock, deliveries, and payments must not be lost in case of partial failures.
- The database must preserve data integrity and ensure correct transactional behavior.
- The system must maintain logs of important events, such as authentication attempts, reservations, delivery acceptance or rejection, stock alerts, and payment updates.
- Historical data must be preserved for auditability, traceability, and reporting purposes.

## 6. Usability and Accessibility

The system must be easy to use by all stakeholders, including school staff, nursing home staff, suppliers, and consumers.

- The user interface must be clear, intuitive, and easy to navigate.
- The system must support users with different levels of digital literacy.
- Common tasks, such as viewing menus, reserving meals, updating stock, and validating deliveries, must require as few steps as possible.

## 7. Maintainability

The system must be easy to maintain, extend, and evolve over time.

- The codebase must follow software engineering best practices, including modularity, separation of concerns, and documentation.
- New functionalities, such as additional reports or integrations, must be added without requiring major changes to the existing system.
- The system must support updates with minimal downtime.
- Critical features must be covered by automated tests, including:
    - Unit tests
    - Integration tests
    - Acceptance tests

## 8. Portability

The system must be deployable in different technical environments and remain compatible with the chosen technologies.

- The backend must be compatible with Linux-based environments.
- The system should support deployment using Docker containers.
- The solution must support deployment in both local and cloud environments.
- The API must follow RESTful principles and use JSON as the main data exchange format.
- The frontend must be compatible with modern web browsers and mobile environments.

## 9. Monitoring and Alerts

The system must provide operational visibility and support quick identification of issues.

- The system must expose technical and business-related metrics, such as:
    - Response times
    - Error rates
    - Resource usage
    - Number of reservations
    - Delivery events
    - Stock alerts
- The platform must generate alerts for situations such as:
    - Products close to expiration
    - Expired products
    - Minimum stock threshold reached
    - Rejected deliveries
    - Uncollected meals
    - Critical failures in system components
- Logs must be centralized and available for analysis and troubleshooting.
- The monitoring solution should be compatible with tools such as Prometheus and Grafana.

## 10. Interoperability

The system must support communication with external platforms relevant to the domain.

- The system must support integration with the Student/School Portal for authentication and student-related information.
- The system must support integration with the Government System to retrieve payment-related information.
- All integrations must use secure communication mechanisms and must not compromise the confidentiality or integrity of the system.
- The architecture should support future integrations with supplier databases, certification bodies, or reporting platforms.

### Secure Development Requirements

These are required activities during software development to ensure that the CantinasCinfães application does not contain vulnerabilities.

#### 1. Secure Coding Guidelines
Follow recognized standards such as **OWASP Secure Coding Practices**, **CERT Secure Coding Standards**, or **Microsoft's Secure Coding Guidelines**.
Apply best practices:
* **Strict input validation:** Ensure all data coming from the Mobile and Web Applications is validated, particularly in critical forms like Menu Creation, Meals and Reservations Management, and Stock Operations.
* **Strong authentication and secure session management:** Implement JWT tokens securely to handle User Authentication and Password Recovery across all user roles (students, canteen staff, and suppliers).
* **Proper use of cryptography:** Encrypt sensitive data at rest in PostgreSQL, such as passwords and Payment History.
* **Secure error and exception handling:** Ensure no stack traces or database structures are exposed to the end-user, especially during integrations with the **School Portal**.
* **Principle of least privilege:** Implement strict Role-Based Access Control (RBAC) so that, for example, suppliers can only access the Supplier Application and not the internal Food Waste Monitoring or Reports.

#### 2. Dependency Management
* Monitor third-party libraries and frameworks, particularly **NuGet packages** for the **.NET 8.0 SDK**.
* Quickly update vulnerable dependencies (e.g., PDF generation libraries or PostgreSQL connectors).
* Avoid using unmaintained or suspicious packages.

#### 3. Secure Code Review
Perform security-focused code reviews for all new code and major changes. Use automated tools (e.g., **SonarQube**) to assist but not replace manual review.
Focus areas:
* **Critical components:** JWT authentication, authorization mechanisms, and PostgreSQL data access layers.
* **Common vulnerability patterns:** SQL Injection (especially in dynamic queries for Reports and Performance Indicators), XSS in the Web Application, and CSRF.
* **Business logic errors that could be exploited:** e.g., manipulating the Delivery Reception and Validation process, bypassing payment requirements for Meals, or unauthorized data sync with the **School Portal**.

#### 4. Static Application Security Testing (SAST)
* Use static code analysis tools (e.g., **SonarQube**) integrated directly into the **GitHub Actions** CI/CD pipelines to detect vulnerabilities early in the C# codebase.

#### 5. Secure Build and Deployment
* Ensure builds are automated, reproducible, and conducted in controlled environments via **GitHub Actions** workflows.
* **Prevent secret exposure:** Strictly use **GitHub Secrets** to manage sensitive data such as JWT secret keys, PostgreSQL connection strings, and **School Portal API keys/certificates**. Never hardcode these in the repository.

#### 6. Logging and Monitoring
* Implement secure logging of security-relevant events, such as User Authentication logins, unauthorized access attempts to Delivery History, or errors during communication with the **School Portal**.
* Ensure logs do not contain sensitive information (e.g., plain-text passwords or personal student data from the portal).

#### 7. Development of Automated Security Tests
Create and maintain automated security tests as part of the development lifecycle.
* Integrate security tests into the **GitHub Actions** CI/CD pipeline to continuously validate code security.
* Cover common vulnerabilities such as injection flaws, authentication issues, and access control weaknesses.
* Include **unit tests** focused on validating security-related functions (e.g., testing the JWT generation and validation logic).
* Implement **integration tests** to verify secure interactions between the .NET 8 backend, the PostgreSQL database, and the **School Portal API**.
* Develop **end-to-end tests** to simulate real-world security scenarios and verify protection mechanisms across the Web and Mobile Interfaces.

> **Note:** These requirements must be continuously reviewed and updated to adapt to evolving security threats.

---

### External Dependencies
This project will rely on external libraries and tools to provide key features:
* **School Portal:** External platform integration for student data synchronization and potentially federated authentication.
* **Authentication:** JWT (JSON Web Tokens).
* **Reporting:** PDF generation libraries for Reports and Performance Indicators.
* **Data Persistence:** PostgreSQL connectivity and migrations (via Entity Framework Core).
* **CI/CD:** Automation via **GitHub Actions**.

All of these will be managed as **NuGet packages**, API integrations, and GitHub Actions workflows, under the umbrella of the **.NET 8.0 SDK** and runtime. Proper version control, security audits (especially for the School Portal API connection), and update processes will be applied to maintain system stability and integrity.

### Abuse and Misuse cases

## Project Design

### Threat Modeling

### Secure Design

### Secure Architecture

### Secure Test Planning

## Conclusion



