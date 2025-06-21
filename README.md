# 💳 CashCard API

A secure RESTful Spring Boot application for managing virtual "Cash Cards". Built with clean architecture, layered design, and best practices for modern backend development.

## 🚀 Features

- 🔐 **Spring Security** with Basic Auth and role-based access control (RBAC)
- 💡 **Ownership-based authorization**: Users can only access their own cash cards
- 📦 CRUD operations: Create, read, update, and delete cash cards
- 📄 Pagination & sorting support for listing cards
- ✅ Comprehensive integration tests using `TestRestTemplate` and `JsonPath`
- 🧪 Test data isolated with `@Sql` and `@DirtiesContext`
- 💾 In-memory H2 database with console enabled for local dev
- 🧱 Clean architecture: Controller → Service → Repository

---

## 📐 Architecture
com.example.cashcard
├── config          # Spring Security configuration
├── controller      # REST controllers
├── model           # Entity classes
├── repository      # Spring Data JPA interfaces
├── service         # Business logic
└── CashcardApplication.java

---

## 🔐 Authentication

This app uses **Basic Authentication** with users defined in-memory.

| Username   | Password | Role         |
|------------|----------|--------------|
| sarah1     | abc123   | CARD-OWNER   |
| hank-owns-no-cards | qrs456 | NON-OWNER    |
| kumar2     | xyz789   | CARD-OWNER   |

Users with the role `CARD-OWNER` can access endpoints under `/cashcards/**`.

---

## 🔄 API Endpoints

| Method | Endpoint             | Description                       | Auth Required |
|--------|----------------------|-----------------------------------|---------------|
| GET    | `/cashcards/{id}`    | Retrieve a cash card by ID        | ✅            |
| GET    | `/cashcards`         | List all cards (with paging)      | ✅            |
| POST   | `/cashcards`         | Create a new cash card            | ✅            |
| PUT    | `/cashcards/{id}`    | Update an existing card           | ✅            |
| DELETE | `/cashcards/{id}`    | Delete a cash card by ID          | ✅            |

---

## 🧪 Running Tests

The project includes full **integration tests** using Spring Boot’s test framework:
Integration tests are written using JUnit 5 and Spring Boot Test.

```bash
mvn test
mvn clean install
mvn spring-boot:run```
