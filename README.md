# OrderHub

OrderHub is a lightweight order and user service designed for quick deployment in internal environments. It provides a clean REST API, in-memory persistence for fast iteration, and a small footprint suitable for demos and integration testing.

## Features

- RESTful endpoints for users and orders
- In-memory persistence with simple statistics
- Input validation and structured responses
- Easy local setup with Maven

## Running

Requirements:
- Java 17+
- Maven 3.9+

Tested with:
- JDK 17
- JDK 25

```bash
mvn spring-boot:run
```

The service listens on `http://localhost:8080`.

## Tests

```bash
mvn test
```

## API

- `POST /users` create a user
- `GET /users/{id}` fetch a user
- `POST /orders` create an order
- `GET /orders/{id}` fetch an order
- `GET /orders?userId=...` list orders
- `GET /orders/stats` basic service stats

## Example

```bash
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@example.com","name":"A"}'
```

```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":"a@example.com","items":[{"sku":"sku-1","quantity":1,"unitPrice":12.5}]}'
```
