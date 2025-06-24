# 💳 CashCard API

A secure Spring Boot REST API for managing virtual cash cards. This application demonstrates full CRUD operations, pagination, validation, exception handling, role-based security, and OpenAPI documentation using Swagger.
## 🚀 Features

Create, read, update, and delete individual cash cards
-	Bulk update and bulk delete endpoints
-	Spring Security with Basic Authentication and Role-based access control
-	DTO pattern for request/response separation
-	Validation and error handling using @Valid and @RestControllerAdvice
-	Pagination & sorting
-	Swagger/OpenAPI integration for documentation, and Postman for testing
-	Unit and integration tests using JUnit
-	H2 in-memory database for testing and demo
-	Dockerized for consistent deployment
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
## 🔐 Security Setup
-	Basic authentication is required for all endpoints except / and /swagger-ui/**.
-	Role-based access control:
-	CARD-OWNER: Can access /cashcards/**
-	ADMIN:
    -	Can access /h2-console/**
    - Method level authorization
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

- To run tests:
```mvn test```
---
📦 DTOs

DTOs ensure request/response payloads are clean and validated.
•	CashCardRequestDTO: for create/update (requires amount > 0)
•	CashCardResponseDTO: for responses (returns id and amount)
•	CashCardBulkUpdateDTO: for bulk update requests (requires id > 0, amount > 0)

---
💥 Global Exception Handling

Implemented via @RestControllerAdvice:
-	Returns meaningful validation errors (@Valid + @ExceptionHandler)
-	Handles JSON parse errors (e.g., sending "amount": "abc")
-	Returns 404 when cash card is not found or not owned
-	Returns consistent error responses like:
---
## 🔐 Authentication

This app uses **Basic Authentication** with samples users defined in-memory.

| Username   | Password | Role         |
|------------|----------|--------------|
| sarah1     | abc123   | CARD-OWNER   |
| hank-owns-no-cards | qrs456 | NON-OWNER    |
| kumar2     | xyz789   | CARD-OWNER   |

---

## 🔄 API Endpoints

| Method | Endpoint             | Description                       | Auth Required | Authorization |
|--------|----------------------|-----------------------------------|---------------|---------------|
| GET    | `/cashcards/{id}`    | Retrieve a cash card by ID        | ✅            |CARD-OWNER     |
| GET    | `/cashcards`         | List all cards (with paging)      | ✅            |CARD-OWNER     |
| GET    | `/cashcards/filter`  | List all cards by range(with paging)      | ✅            |ADMIN          |
| POST   | `/cashcards`         | Create a new cash card            | ✅            |CARD-OWNER     |
| PUT    | `/cashcards/{id}`    | Update an existing card           | ✅            |CARD-OWNER     |
| PUT    | `/cashcards/bulk`    | Update all selected cards         | ✅            |CARD-OWNER     |
| DELETE | `/cashcards/{id}`    | Delete a cash card by ID          | ✅            |CARD-OWNER     |
| DELETE | `/cashcards/bulk`    | Delete all selected cards         | ✅            |CARD-OWNER     |



