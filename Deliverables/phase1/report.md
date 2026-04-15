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

### Secure development requirements

### Abuse and Misuse cases

## Project Design

### Threat Modeling

### Secure Design

### Secure Architecture

### Secure Test Planning

## Conclusion



