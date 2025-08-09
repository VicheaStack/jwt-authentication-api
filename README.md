# Spring Boot JWT Authentication API

A simple, production-ready example of a JSON Web Token (JWT) based authentication API using Spring Boot, Spring Security, and Spring Data JPA.

This README explains how to run the project, environment variables, API endpoints, example requests, and common configuration.

---

## Table of Contents

* [Features](#features)
* [Requirements](#requirements)
* [Environment Variables](#environment-variables)
* [Build & Run](#build--run)
* [API Endpoints](#api-endpoints)

  * [Authentication](#authentication)
  * [Protected User Routes](#protected-user-routes)
* [Sample Requests](#sample-requests)
* [Security Notes](#security-notes)
* [Testing](#testing)
* [FAQ & Tips](#faq--tips)

---

## Features

* User registration and login
* JWT access token generation and validation
* Role-based authorization (e.g., `ROLE_USER`, `ROLE_ADMIN`)
* Token expiry configuration
* Example protected endpoints
* Easy to extend for refresh tokens, OAuth2, or distributed sessions

## Requirements

* Java 11+ (recommended Java 17)
* Maven (or use the included `mvnw` wrapper)
* PostgreSQL or MySQL (or H2 for quick dev)

## Environment Variables

Configure these in `application.properties` or as environment variables.

```
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jwt_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secret
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# JWT
JWT_SECRET=verySecretKeyChangeThis
JWT_EXPIRATION_MS=3600000

# Server (optional)
SERVER_PORT=8080
```

> Tip: Keep `JWT_SECRET` secret and long. Use a secure secret manager in production.

## Build & Run

1. Clone the repo:

```bash
git clone https://github.com/<your-username>/spring-jwt-api.git
cd spring-jwt-api
```

2. Build and run with Maven:

```bash
./mvnw clean package
java -jar target/spring-jwt-api-0.0.1-SNAPSHOT.jar
```

3. Or run from your IDE (main class: `com.example.Application`)

If you use Docker, add a `Dockerfile` and link to your DB container.

## API Endpoints

### Authentication

* `POST /api/auth/register` — Register a new user.

  * Body: `{ "username": "user1", "email": "user1@example.com", "password": "pass123" }`
  * Response: `201 Created`

* `POST /api/auth/login` — Authenticate and receive a JWT.

  * Body: `{ "username": "user1", "password": "pass123" }`
  * Response: `{ "accessToken": "<jwt>", "tokenType": "Bearer" }`

* `POST /api/auth/refresh` — (Optional) Exchange refresh token for a new access token. Implementation not included by default.

### Protected User Routes

* `GET /api/users/me` — Get current user profile (requires Authorization header `Bearer <token>`).
* `GET /api/admin/stats` — Admin-only endpoint (requires `ROLE_ADMIN`).

## Sample Requests

### Login (curl)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass123"}'
```

### Access protected endpoint

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <accessToken>"
```

### Example success response (login)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

## Security Notes

* Use HTTPS in production to protect tokens in transit.
* Use a strong, randomly-generated `JWT_SECRET` (store outside source control).
* Set a reasonable token expiry (`JWT_EXPIRATION_MS`) and consider refresh tokens for long sessions.
* Consider storing blacklisted tokens or implementing token revocation if users log out.
* Prefer asymmetric signing (RS256) for larger systems where public/private key rotation is needed.

## Testing

* Unit tests: JUnit + Mockito for service and repository layer tests.
* Integration tests: Use `@SpringBootTest` with `Testcontainers` (Postgres) or H2 for quick runs.

## FAQ & Tips

**Q: Where do I set roles for new users?**
A: In registration flow you can assign default `ROLE_USER`. Create an `roles` table and grant `ROLE_ADMIN` manually via migration or admin route.

**Q: How do I add refresh tokens?**
A: Add a `refresh_tokens` table linked to users, issue refresh tokens with longer expiry, and provide an endpoint to exchange refresh -> access.

