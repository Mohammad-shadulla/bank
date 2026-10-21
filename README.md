# Mini Banking & Money Transfer System

A Spring Boot REST API for bank account management and secure money transfers. Each user registers, logs in with a JWT token, and can only access their own accounts.

## Features

* **Account management:** create, list, view, check balance, and delete accounts (deletion only when the balance is 0)
* **Transactions:** deposit, withdraw, transfer between accounts, and view transaction history
* **Authentication:** user registration with BCrypt password hashing and JWT login
* **Authorization:** all endpoints except `/auth/**` need a valid token, and users can only access their own accounts
* **Rate limiting:** the transfer endpoint allows 5 requests per minute (Resilience4j RateLimiter) and then returns HTTP 429
* **Safe transfers:** transfers run in a database transaction, so both balances update together or not at all
* **Validation and error handling:** clear error messages through a global exception handler

## Tech Stack

* **Language:** Java (built with JDK 21)
* **Frameworks:** Spring Boot, Spring Security, Spring Data JPA
* **Database:** MySQL
* **Libraries:** Hibernate, JJWT, Resilience4j, Lombok
* **Build tool:** Maven

## API Endpoints

| Method | Endpoint | Description | Token needed |
|---|---|---|---|
| POST | /auth/register | Register a new user | No |
| POST | /auth/login | Login and get a JWT token | No |
| GET | /api/me | Get the logged-in username | Yes |
| POST | /api/accounts | Create an account | Yes |
| GET | /api/accounts | List my accounts | Yes |
| GET | /api/accounts/{number} | Get account details | Yes |
| GET | /api/accounts/{number}/balance | Get account balance | Yes |
| POST | /api/accounts/{number}/deposit | Deposit money | Yes |
| POST | /api/accounts/{number}/withdraw | Withdraw money | Yes |
| POST | /api/transfer | Transfer money (5 per minute) | Yes |
| GET | /api/accounts/{number}/history | Transaction history | Yes |
| DELETE | /api/accounts/{number} | Delete an account (balance must be 0) | Yes |

Send the token in the header: `Authorization: Bearer <token>`

## Getting Started

**Requirements:** JDK 17 or higher, MySQL, and an IDE such as IntelliJ IDEA.

1. Clone the repository:

```bash
   git clone https://github.com/Mohammad-shadulla/bank.git
   cd bank
```

2. Create a MySQL database named `bank_db`.

3. Create the file `src/main/resources/application-local.properties` with these two lines:

```properties
   spring.datasource.password=YOUR_DB_PASSWORD
   jwt.secret=YOUR_LONG_SECRET_KEY_AT_LEAST_32_CHARACTERS
```

This file is ignored by Git, so your password and secret stay private. Check `application.properties` for the database URL and username.

4. Run the app with the `local` profile:

```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell, use `.\mvnw` instead of `./mvnw`.

The API starts on `http://localhost:8080`.

## Testing the API

The file `bank-tests.http` has ready-made requests that run in IntelliJ IDEA. Run **Login** first, because it saves the token for the other requests.

Quick flow: register a user, log in, create an account, deposit money, and transfer it to another account.

## Planned Improvements

* Unit tests for the service layer
* Swagger / OpenAPI documentation
* Frontend