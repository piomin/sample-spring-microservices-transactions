# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A distributed transaction coordination system for microservices using the **Saga pattern**. The project demonstrates event-driven distributed transactions with Spring Boot, RabbitMQ, and Eureka service discovery.

## Technology Stack

- **Language**: Kotlin 2.1.21 on Java 21
- **Framework**: Spring Boot 3.5.0, Spring Cloud 2025.0.0
- **Messaging**: RabbitMQ via Spring AMQP
- **Database**: PostgreSQL 15 via Spring Data JPA
- **Service Discovery**: Netflix Eureka
- **Testing**: JUnit 5, Testcontainers
- **Build**: Maven, Jib for Docker images

## Project Modules

| Module | Port | Role |
|---|---|---|
| `order-service` | 8080 | Saga orchestrator; calls Product and Account services |
| `account-service` | random (Eureka) | Manages account balances |
| `product-service` | random (Eureka) | Manages product inventory |
| `transaction-server` | 8888 | Central distributed transaction coordinator |
| `discovery-server` | 8761 | Eureka service registry |

## Build & Run Commands

```bash
# Build all modules (skip tests)
mvn clean package -DskipTests

# Build Docker images via Jib
mvn clean package -DskipTests -Pbuild-image

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ProductControllerTests -pl product-service

# Run with coverage report
mvn test jacoco:report

# Start all infrastructure and services
docker-compose up
```

## Architecture

### Distributed Transaction Flow

1. Client sends an order request to **Order Service**
2. Order Service (orchestrator) calls **Product Service** (REST, load-balanced via Eureka) to reserve inventory
3. Order Service calls **Account Service** (REST, load-balanced via Eureka) to withdraw funds
4. Transaction events are published to RabbitMQ topic exchange `trx-events`
5. **Transaction Server** coordinates and tracks transaction state
6. All services receive completion/rollback events via RabbitMQ

### Communication Patterns

- **Synchronous**: `@LoadBalanced` `RestTemplate` for REST calls between services
- **Asynchronous**: RabbitMQ topic exchange `trx-events` for saga event coordination
- **Internal**: Spring `ApplicationEventPublisher` → `EventBus` class → RabbitMQ

### Key Patterns

- `@Transactional` on service methods
- `@Async` on service classes for non-blocking processing
- `@RabbitListener` on event handlers for message consumption
- `@TestMethodOrder` / `@Order` for ordered integration tests

## Testing

Tests are integration tests using **Testcontainers** — no local PostgreSQL or RabbitMQ needed. Containers are started automatically per test class.

Test files:
- `account-service/src/test/kotlin/AccountControllerTests.kt`
- `product-service/src/test/kotlin/ProductControllerTests.kt`
- `transaction-server/src/test/kotlin/TransactionControllerTests.kt`

## Configuration

Each service has `application.yml` with two profiles:
- **Default** (no profile): connects to `localhost` for PostgreSQL/RabbitMQ/Eureka
- **`docker`**: connects to container hostnames (`postgres`, `rabbitmq`, `discovery-server`)

Docker Compose database credentials: user/password/db = `trx`/`trx123`/`trx`

## CI/CD

CircleCI (`.circleci/config.yml`) runs two jobs:
1. `test` — Maven test on Ubuntu with Java 21
2. `analyze` — SonarCloud analysis (project: `piomin_sample-spring-microservices-transactions`)