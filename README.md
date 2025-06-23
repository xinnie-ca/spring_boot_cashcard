# 💳 CashCard API

A secure RESTful Spring Boot application for managing virtual "Cash Cards". Built with clean architecture, layered design, and best practices for modern backend development.

## 🚀 Features

- Full REST API for managing CashCards
- Basic Authentication with Role-Based Access Control (`CARD-OWNER`, `ADMIN`)
- In-memory H2 Database for quick prototyping
- DTOs for request/response separation and validation
- Global Exception Handling for user-friendly error responses
- API documentation with Swagger UI
- Unit and Integration Testing with `TestRestTemplate`
- Docker support for containerized deployment
---
## 📦 Technologies

- Java 21
- Spring Boot
- Spring Web + Spring Security- Uses DTOs for request/response decoupling
- H2 (in-memory dev DB) 
- JPA/Hibernate
- Swagger (springdoc-openapi)
- Docker support

---

## 📐 Architecture
<pre>

com.example.cashcard
├── config           # SecurityConfig
├── controller       # CashCardController
├── dto              # DTO classes for requests and responses
├── model            # CashCard entity
├── repository       # CashCardRepository
├── service          # CashCardService
├── error            # Exception Handler
└── CashcardApplication.java

</pre>

---
### ✅ Run Locally

Make sure you have Java 21+ and Maven installed.

```bash
mvn clean spring-boot:run
```
🐳 Or Run with Docker

```bash
mvn clean package
docker build -t cashcard-app .
docker run -p 8080:8080 cashcard-app
```
Access the app at:
http://localhost:8080

Swagger UI:
http://localhost:8080/swagger-ui.html

H2 Console:
http://localhost:8080/h2-console
---
🧪 Testing

Integration tests are included using @SpringBootTest, TestRestTemplate, and @DirtiesContext.

To run tests:
```mvn test```

---

## 🔐 Authentication

This app uses **Basic Authentication** with samples users defined in-memory.

| Username   | Password | Role         |
|------------|----------|--------------|
| sarah1     | abc123   | CARD-OWNER   |
| hank-owns-no-cards | qrs456 | NON-OWNER    |
| kumar2     | xyz789   | CARD-OWNER   |


Users with the role `CARD-OWNER` can access endpoints under `/cashcards/**`.
User with thte role `ADMIN` can access ednpoints under `/h2-console/**`.

---

## 🔄 API Endpoints

| Method | Endpoint             | Description                       | Auth Required |
|--------|----------------------|-----------------------------------|---------------|
| GET    | `/cashcards/{id}`    | Retrieve a cash card by ID        | ✅            |
| GET    | `/cashcards`         | List all cards (with paging)      | ✅            |
| POST   | `/cashcards`         | Create a new cash card            | ✅            |
| PUT    | `/cashcards/{id}`    | Update an existing card           | ✅            |
| PUT    | `/cashcards/bulk`    | Update all existing cards         | ✅            |
| DELETE | `/cashcards/{id}`    | Delete a cash card by ID          | ✅            |


