# 💳 CashCard API

A secure RESTful Spring Boot application for managing virtual "Cash Cards". Built with clean architecture, layered design, and best practices for modern backend development.


## 🚀 Technologies

- Java 21
- Spring Boot
- Spring Web + Spring Security
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
├── model            # CashCard entity
├── repository       # CashCardRepository
├── service          # CashCardService
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



---

## 🔐 Authentication

This app uses **Basic Authentication** with users defined in-memory.

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
| DELETE | `/cashcards/{id}`    | Delete a cash card by ID          | ✅            |


