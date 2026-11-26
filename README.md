
# Online Banking System

A REST API for a simple online bank, built with Spring Boot. Users register, log in, open accounts, deposit money, transfer between accounts and view their transfer history. The project focuses on **correctness under concurrency** and **account security**: no overdrafts, no lost updates, no deadlocks, and users can only touch their own accounts.

## Tech stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Spring Data JPA, Spring Security 7)
- Hibernate 7 with MariaDB
- JWT authentication (jjwt)
- Lombok
- JUnit 5, Mockito, MockMvc

## Features

- User registration with BCrypt-hashed passwords
- Stateless login with JWT
- Accounts linked to their owner, with an initial deposit and a unique account number
- Deposits and transfers between accounts (same currency only)
- No overdrafts, and rejection of same-account, unknown-account and non-positive transfers
- Transfer history for every transfer, completed or failed, with pagination
- Ownership checks on every account operation
- Pessimistic locking, optimistic locking (`@Version`) and automatic retry on conflict
- Consistent JSON error responses, including 401 and 403 from the security layer

## Getting started

### Prerequisites

- JDK 21
- Maven
- MariaDB running locally with a database named `bank`

### Configuration

Set these in `src/main/resources/application.properties` (or as environment variables). Do not commit real secrets.

```properties
spring.datasource.url=jdbc:mariadb://localhost/bank
spring.datasource.username=<db user>
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}          # at least 32 characters
jwt.expiration-ms=3600000         # 1 hour

spring.jpa.open-in-view=false
```

### Run

```bash
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

### Test

```bash
mvn test
```

The `@SpringBootTest` tests use the real database configured above. Each run creates uniquely named users and accounts.

## API

All endpoints except `/api/auth/**` require the header `Authorization: Bearer <token>`.

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a user |
| POST | `/api/auth/login` | Log in and receive a JWT |
| POST | `/api/account/creation` | Open an account (currency, optional initial deposit) |
| GET | `/api/account/{id}` | View one of your accounts |
| POST | `/api/account/{id}/deposite` | Deposit into one of your accounts |
| GET | `/api/account/{id}/transfers` | Paged transfer history for one of your accounts |
| POST | `/api/transfers` | Transfer money from one of your accounts |

### Example

```bash
# register and log in
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Password123!"}'
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Password123!"}'

# transfer 10.00 between two of your accounts
curl -X POST localhost:8080/api/transfers \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"fromAccountId":1,"toAccountId":2,"amount":10.00}'
```

### History pagination

`GET /api/account/{id}/transfers?page=0&size=20` returns newest first. The response is a `PagedModel`: a `content` array plus a `page` block (`size`, `number`, `totalElements`, `totalPages`). Client-supplied sorting is ignored, and the page size is capped by `spring.data.web.pageable.max-page-size`.

### Error responses

Every error has the shape `{"error": "<message>"}`.

| Status | When |
|---|---|
| 400 | Invalid input: non-positive amount, same-account transfer, currency mismatch, validation failure |
| 401 | Missing, invalid or expired token; wrong username or password |
| 403 | Transferring from an account you do not own |
| 404 | Unknown account, or an account that belongs to someone else (same response, so ids cannot be probed) |
| 409 | Insufficient funds, other state conflicts, or a concurrency conflict after all retries |

## Security

### Why JWT

JWT was chosen because the API is stateless: the client sends the token with every request, so the server stores no session and any instance can verify it. The trade-off is that a token cannot be revoked before it expires, so the expiry is kept short and the signing secret must be kept private.

### How it works

- Passwords are stored only as BCrypt hashes.
- `JwtAuthFilter` reads the bearer token, verifies the signature and expiry, and sets the authenticated user.
- `JsonAuthEntryPoint` returns a JSON 401 and `JsonAccessDeniedHandler` returns a JSON 403 for errors raised in the security filter chain. Errors raised inside controllers and services are handled by `GlobalExceptionHandler`.
- Every account is linked to its owner. Viewing, depositing, history and transfers are all checked against the logged-in user.
- A failed login returns the same message for an unknown user and a wrong password.

## Money handling and concurrency

### Rules

- Amounts are `BigDecimal` with two decimal places.
- A transfer needs a positive amount, two different existing accounts, matching currencies and enough balance.
- The debit and credit happen in one database transaction, so they succeed or fail together.

### The lost update problem

If two transfers read the same balance at the same moment, both can write back a value based on the old balance, and one transfer is lost. For example, with a balance of 1000 and 20 simultaneous transfers of 10, the correct result is 800. Without protection the final balance can come out higher, which means money was created.

### Protections

| Layer | What it does |
|---|---|
| Pessimistic lock (`SELECT ... FOR UPDATE`) | Concurrent transfers on the same account wait in line |
| `@Version` on `Account` | Safety net: a conflicting write is rejected instead of silently overwriting |
| Retry in `TransferFacade` | On a concurrency failure (optimistic conflict, deadlock, lock timeout) the transfer is retried in a fresh transaction, up to 5 attempts with randomized backoff |
| Lock ordering | Accounts are always locked lowest id first, so opposite transfers (A to B and B to A) cannot deadlock |

The retry sits outside the transactional method on purpose: each attempt must start a new transaction and read fresh data. Only concurrency errors are retried, never business errors such as insufficient funds.

### Pessimistic vs optimistic locking

Choose pessimistic locking when conflicts on the same row are frequent, as with a busy account: writers queue instead of failing and retrying. Choose optimistic locking when conflicts are rare and holding database locks would be wasteful. This project uses pessimistic locking as the main protection and `@Version` plus retry as the safety net.

### Transfer history

Every transfer is recorded. Completed transfers are saved in the same transaction as the money movement. Failed transfers are saved in a separate transaction (`TransferHistoryService`, `REQUIRES_NEW`) after the rollback, so the failure record survives. A failed transfer is recorded only when both accounts exist, because the history table has non-null foreign keys to both accounts.

## Tests

| Test | What it covers |
|---|---|
| `TransferServiceTest` (Mockito) | Overdraft, same account, non-positive amounts, unknown accounts, ownership, happy path |
| `TransferFacadeTest` (Mockito) | Retry then success, giving up after the maximum attempts, no retry for business errors |
| `SecurityAttackTest` (MockMvc) | No token, bad token, reading or depositing into another user's account, reading another user's history, transferring from another user's account, balance unchanged after attacks |
| `ConcurrencyTest` | 20 simultaneous transfers of 10 from one account (expect 800 and 200), repeated runs, and opposite transfers A to B and B to A with no deadlock and no lost money |

## Project structure

```
src/main/java/vidat/com/Online_Banking_system
├── config        SecurityConfig, JwtAuthFilter, JsonAuthEntryPoint, JsonAccessDeniedHandler
├── controllers   UsersController, AccountController, TransferController
├── dtos          Request and response objects, TransferHistoryItem
├── entities      Users, Account, Transfer, Role, Status
├── exceptions    GlobalExceptionHandler, InsufficientFundsException, AccountNotFoundException
├── repositories  UserRepository, AccountRepository, TransferRepository
└── services      AuthService, JwtService, AccountService, TransferService,
                  TransferFacade, TransferHistoryService, MyUserDetailService
```

## Known limitations

- Failed transfers involving an unknown account are not stored in the history.
- JWTs cannot be revoked before they expire.
- Deposits do not create history records.
- The tests run against the configured database rather than an isolated test database.
