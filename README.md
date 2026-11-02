# Mini Banking & Money Transfer System

A Spring Boot REST API for bank account management and secure money transfers. Each user registers, logs in with a JWT token, and can only access their own accounts.

## Features

* **Account management:** create, list, view, check balance, and delete accounts (deletion only when the balance is 0)
* **Transactions:** deposit, withdraw, transfer between accounts, and view transaction history
* **Authentication:** user registration with BCrypt password hashing and JWT login
* **Authorization:** all endpoints except `/auth/**` need a valid token, and users can only access their own accounts
* **Role-based access:** users have a USER or ADMIN role stored in the JWT, and the admin-only endpoint `/api/admin/accounts` returns 403 for normal users
* **Rate limiting:** the transfer endpoint allows 5 requests per minute (Resilience4j RateLimiter) and then returns HTTP 429
* **Safe transfers:** transfers run in a database transaction, so both balances update together or not at all
* **Validation and error handling:** clear error messages through a global exception handler
* **Unit tests:** JUnit 5 and Mockito tests for the service layer (deposit, withdraw, transfer, ownership and validation rules)
* **API documentation:** interactive Swagger UI with JWT authorization

## Tech Stack

* **Language:** Java (built with JDK 21)
* **Frameworks:** Spring Boot, Spring Security, Spring Data JPA
* **Database:** MySQL
* **Libraries:** Hibernate, JJWT, Resilience4j, springdoc-openapi (Swagger), Lombok
* **Testing:** JUnit 5, Mockito
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
| GET | /api/admin/accounts | List all accounts (admin only) | Yes (ADMIN) |

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

## API Documentation (Swagger)

With the app running, open `http://localhost:8080/swagger-ui/index.html`.

1. Open **POST /auth/login**, click **Try it out**, enter a username and password, and click **Execute**
2. Copy the token from the response (only the long text, without quotes)
3. Click **Authorize**, paste the token, and click **Authorize**
4. All protected endpoints now work from the page

## Admin Access

New users get the `USER` role. To make a user an admin, run this in MySQL and log in again to get a new token:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

## Testing

* **Unit tests:** `./mvnw test -Dtest=BankServiceTest`
* **API requests:** the file `bank-tests.http` has ready-made requests that run in IntelliJ IDEA. Run **Login** first, because it saves the token for the other requests.

## Planned Improvements

* Frontend