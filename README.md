# Internship Manager

A secure, production-ready Spring Boot backend for managing academic internship workflows. It provides role-based REST APIs for students, teachers, and administrators to handle candidacies, task tracking, document exchange, and automated communication.

## Core Capabilities

* **Access Control:** Stateless JWT authentication and role-aware authorization (`ADMIN_IT`, `ENSEIGNANT`, `ETUDIANT`).
* **Lifecycle Management:** Subject proposals, student candidacies, mentor assignments, and academic period tracking.
* **Workflow Automation:** Scheduled meeting reminders, email notifications, and optional AI-chatbot integration.
* **Operational Features:** File upload/download capabilities, WebSocket live messaging, and async email processing.

## Technology Stack

| Layer | Technology |
|---|---|
| **Language & Framework** | Java 17, Spring Boot 3.5.14 |
| **Persistence** | MySQL 8.x, Spring Data JPA, Hibernate |
| **Security** | Spring Security, JWT |
| **Communication** | REST API, WebSockets, Spring Mail (Thymeleaf) |
| **Tooling** | Maven Wrapper (`mvnw`), Lombok, MapStruct |

## Prerequisites

* Java 17+
* Maven 3.9+ (or use included `mvnw`)
* MySQL 8.x
* Git
* *Optional:* MailHog or local SMTP provider for testing email workflows.

## Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/jecem-ben-slama/InternshipManagerApplication.git
cd internship-manager
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Update `.env` with your local MySQL credentials. Do not commit this file to version control.


### 3. Build the project

```bash
./mvnw clean install
# On Windows: .\mvnw.cmd clean install
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

### 5. Verify Startup and Auto-Seeding

Check the console output for a successful initialization on `http://localhost:8080`. On the first boot, if the database is empty, the application will automatically seed three default users (Admin, Teacher, Student) to facilitate immediate testing.

### 6. Quick Start: Authentication

To verify the API is operational, authenticate using the auto-seeded IT Administrator account to acquire a JWT.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "email@test",
    "password": "password123"
  }'
```

*(Note: Replace the email and password above with the exact credentials configured in  seeder file). Use the returned token in the `Authorization: Bearer <token>` header for all subsequent API requests.*

### 8. Background Processes: Automated Reminders

The application runs scheduled background tasks, including an automated 24-hour meeting reminder cron job.

**Local Development Warning:** To prevent console errors from failed SMTP connections or accidental spam to dummy addresses, ensure reminders are disabled locally unless explicitly testing email workflows. Set the following in your `.env`:

```
REMINDER_ENABLED=false
# If using MailHog locally (http://localhost:8025), set to true and map the correct ports.
```

## API Documentation

* **Web Reference:** [Fern API Documentation](https://iit-851148.docs.buildwithfern.com/internship-manager/introduction)
* **Local Reference:** See the `API-DOCS/` folder in the root directory for a detailed PDF and a Postman collection (`Internship Manager.postman_collection.json`).


## License

MIT License. See the `LICENSE` file for details.
