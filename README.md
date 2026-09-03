# Internship Manager

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Internship Manager is a secure, production-oriented backend platform for managing internship workflows across an academic environment. It supports the complete internship lifecycle, from user onboarding and subject discovery to candidacy submission, mentor assignment, meetings, task tracking, document exchange, and communication workflows.

Built with Java 17 and Spring Boot 3, the application exposes a REST API for students, teachers, and administrative staff while enforcing role-based authorization and stateless JWT authentication.

## Executive Summary

This solution is designed to support the operational realities of internship coordination in higher education:

- Students can browse internship subjects, submit candidacies, and track the state of their participation.
- Teachers can propose and review subjects, evaluate student applications, accept or reject candidates, and coordinate academic follow-up.
- Administrative staff can manage users, academic periods, and system-level oversight without manual operational friction.
- The platform integrates email notifications, reminder scheduling, file uploads, and optional AI-assisted chatbot support to improve engagement and reduce administrative overhead.

The application is intentionally structured as a backend-first system to integrate cleanly with web or mobile clients while maintaining a robust security and business-logic foundation.

## Project Overview

### Core capabilities

- JWT-based authentication and role-aware access control
- User registration, profile management, and activation workflows
- Academic year management and subject lifecycle handling
- Student candidacy workflows with acceptance, refusal, and clarification states
- Teacher assignment and internship tracking
- Meeting scheduling, confirmation, and reminder processing
- Task management for assigned interns and supervising staff
- File upload and download for internship-related documentation
- Email templates for important operational communication
- Optional AI-powered chatbot guidance for users
- WebSocket-based communication for live message exchange

### Primary user roles

- `ADMIN_IT`: system administration, user management, and operational oversight
- `ENSEIGNANT`: subject proposal, candidacy review, assignment operations, meetings, and progress tracking
- `ETUDIANT`: subject browsing, candidacy submission, and participation in assigned internship workflows

## Architecture Highlights

The project follows a conventional layered Spring Boot architecture:

- `src/main/java/com/iit/internship_manager/domain` – domain entities, enums, and exceptions
- `src/main/java/com/iit/internship_manager/repositories` – data access interfaces using Spring Data JPA
- `src/main/java/com/iit/internship_manager/services` – business logic and orchestration services
- `src/main/java/com/iit/internship_manager/infrastucture` – security, configuration, mail, and cross-cutting infrastructure
- `src/main/java/com/iit/internship_manager/web` – REST controllers, DTOs, and exception handling
- `src/main/resources` – application settings, templates, and static resources

### Technology stack

| Layer             | Technology                 |
| ----------------- | -------------------------- |
| Language          | Java 17                    |
| Framework         | Spring Boot 3.5.14         |
| Persistence       | Spring Data JPA, Hibernate |
| Database          | MySQL                      |
| Security          | Spring Security, JWT       |
| Communication     | REST API, WebSocket        |
| Email             | Spring Mail, Thymeleaf     |
| Validation        | Jakarta Validation         |
| Build Tool        | Maven Wrapper (`mvnw`)     |
| Utility Libraries | Lombok, MapStruct          |

### Security model

Authentication is stateless and token-based:

- Users log in through `/api/auth/login`
- A JWT is returned to the client
- Requests include the token via the `Authorization: Bearer <token>` header
- Endpoint authorization is enforced with `@PreAuthorize` and security rules defined in `SecurityConfig`
- Sensitive operations remain restricted by role and business ownership validation

### Operational features

- Email reminders are scheduled using Spring scheduling
- File storage is configured for local uploads by default
- Async email processing is enabled to avoid blocking user interactions
- Background reminder jobs support internship follow-up tasks
- AI integration is optional and can be disabled without affecting core platform behavior

## Repository Structure

```text

├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .env.example
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/iit/internship_manager/
│   │   │       ├── domain/
│   │   │       ├── infrastructure/
│   │   │       ├── repositories/
│   │   │       ├── services/
│   │   │       └── web/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
├── uploads/
└── target/
```

## Prerequisites

Before running the project locally, ensure the following are installed:

- Java 17+
- Maven 3.9+ or a working Maven wrapper (`mvnw`)
- MySQL 8.x
- Git
- Optional: a local mail capture tool such as MailHog or a configured SMTP provider

## Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/jecem-ben-slama/InternshipManagerApplication.git
cd internship-manager
```

### 2. Configure environment variables

Create a local environment file from the template:

```bash
cp .env.example .env
```

Then update the values for your local setup. The application loads `.env` automatically through Spring config import support.

### 3. Create the MySQL database

Example:

```sql
CREATE DATABASE internship_management_db;
CREATE USER 'internship_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON internship_management_db.* TO 'internship_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Install dependencies and build the project

```bash
./mvnw clean install
```

On Windows PowerShell:

```powershell
./mvnw.cmd clean install
```

### 5. Run the application

```bash
./mvnw spring-boot:run
```

By default, the application will bind to:

- `http://localhost:8080`

### 6. Verify startup

Check the console output for a successful Spring Boot startup. The application connects to the configured MySQL instance and initializes Hibernate schema updates based on the app configuration.

## Environment Variables Reference

The project supports configuration through `.env` values that are mapped into Spring properties in `src/main/resources/application.properties`.

### Variable usage and recommendations

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`: MySQL connection details
- `JWT_SECRET`: mandatory secret for signing and validating JWT tokens; use a strong, unique value in production
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`: SMTP configuration for email sending
- `REMINDER_ENABLED`, `REMINDER_WINDOW_HOURS`, `REMINDER_CRON`: reminder automation behavior for scheduled notifications
- `OPENAI_*`: optional AI chatbot configuration; disabled by default
- `STORAGE_LOCAL_UPLOAD_DIR`: local storage folder for uploaded documents and profile media

> Important: do not commit `.env` to source control. Keep secrets in a secure local or deployment-managed environment.

## API Overview

The backend exposes REST endpoints under `/api` and uses a shared response envelope style.

### API documentation

- Full API reference and Postman-ready documentation: https://iit-851148.docs.buildwithfern.com/internship-manager/introduction
- Repository documentation bundle: `API-DOCS/`
  - `API_DOCS.pdf` — detailed API documentation
  - `Internship Manager.postman_collection.json` — Postman collection for quick testing

This folder is included in the project root for offline access and local API testing.

## Development Workflow

### Run tests

```bash
./mvnw test
```

### Run in development mode

```bash
./mvnw spring-boot:run
```

### Build a deployable package

```bash
./mvnw clean package
```

## Production Readiness Notes

Before deploying to production, review these operational areas:

- Rotate and secure `JWT_SECRET` via a dedicated secret manager
- Use a non-root database user with least-privilege access
- Protect SMTP credentials and avoid exposing them in source control
- Replace local file storage with a durable managed object store in production environments
- Restrict network exposure and enforce HTTPS/TLS for public endpoints
- Review all role and ownership checks to maintain least-privilege access
- Enable monitoring, audit logging, and alerting for authentication failures and sensitive administrative operations

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for the full terms and conditions.

## Contact and Ownership

For operational questions, environment setup, onboarding, or enhancement requests, contact the project owner or repository maintainers through the established internal project communication channels.
