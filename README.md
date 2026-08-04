# Booking System

REST API for booking shared resources (meeting rooms, restaurant tables, etc.) with time-conflict detection — prevents double-booking the same room for overlapping time ranges.

## Features

- Create, list, cancel, and reschedule bookings
- Automatic conflict detection: overlapping bookings for the same room are rejected
- Cancellation is a soft delete (status change to `CANCELLED`) — booking history is preserved, not erased
- Centralized error handling with proper HTTP status codes (404 not found, 409 conflict) instead of raw 500s

## Tech stack

- Java 17, Spring Boot 3.5.16 (Spring MVC, Spring Data JPA)
- Hibernate + PostgreSQL (H2 in-memory for repository tests)
- JUnit 5, Mockito, AssertJ, `@DataJpaTest`
- Maven

## Architecture

Layered: `controller` → `service` (interface + impl) → `repository` (Spring Data JPA) → `entity`, with `dto` for request/response shapes and a global `exception` handler (`@RestControllerAdvice`) translating domain exceptions into HTTP responses.

## Getting started

### Prerequisites

- Java 17
- Maven
- PostgreSQL running locally

### Setup

1. Create the database:
   ```bash
   createdb booking_system
   ```
2. Update `src/main/resources/application.properties` with your own Postgres username/password.
3. Run the app:
   ```bash
   mvn spring-boot:run
   ```
   Starts on `http://localhost:8080`. Hibernate creates the schema automatically (`ddl-auto=update`).

### Running tests

```bash
mvn test
```

## API

| Method | Endpoint                 | Description                    |
|--------|---------------------------|---------------------------------|
| POST   | `/rooms`                  | Create a room                  |
| GET    | `/rooms`                  | List rooms                     |
| POST   | `/users`                  | Create a user                  |
| GET    | `/users`                  | List users                     |
| POST   | `/bookings`                | Create a booking               |
| GET    | `/bookings`                | List bookings                  |
| POST   | `/bookings/{id}/cancel`   | Cancel a booking (soft delete) |
| PATCH  | `/bookings/{id}`          | Reschedule a booking           |

### Example: creating a booking

```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{"roomId":1,"userId":1,"startTime":"2026-08-10T10:00:00","endTime":"2026-08-10T11:00:00"}'
```

A second request for the same room with an overlapping time range is rejected:

```json
{"status":409,"message":"Room 1 is already booked for that time range"}
```

## Roadmap

- Concurrency-safe conflict checking under simultaneous requests (optimistic locking or a DB-level exclusion constraint)
- Request validation (e.g. `start < end`)
- Authentication & authorization — the `Role` field on `User` is modeled but not yet enforced
- API documentation (OpenAPI/Swagger)
