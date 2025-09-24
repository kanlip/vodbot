# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vodbot is a video recording SaaS platform that enables e-commerce sellers to record packing session videos for orders from multiple platforms (TikTok Shop, Shopee, Lazada). The application is built with Spring Boot 3.4.7 using Spring Modulith architecture and handles order management, user accounts, and platform integrations with a clean hexagonal architecture pattern.

## Technology Stack

- **Java 21** (enforced, supports Java 17-21)
- **Spring Boot 3.4.7** with Spring Modulith for modular architecture
- **PostgreSQL** database with Liquibase for migrations
- **AWS S3** integration (with LocalStack for development)
- **Docker Compose** for local development dependencies
- **Maven** for build management
- **Lombok** for reducing boilerplate
- **MapStruct** for object mapping
- **Hibernate Search** with Lucene backend
- **OpenFeign** for HTTP clients
- **Testcontainers** for integration testing

## Development Commands

### Build and Run
```bash
# Clean and compile
./mvnw clean compile

# Run application (requires Docker Compose services)
./mvnw spring-boot:run

# Build JAR
./mvnw clean package
```

### Testing
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ApplicationTests

# Run integration tests (uses Testcontainers)
./mvnw verify
```

### Database Operations
```bash
# Start required services (PostgreSQL + LocalStack)
docker compose up -d

# Liquibase operations
./mvnw liquibase:update
./mvnw liquibase:status
```

### Development Environment
```bash
# Start all dependencies
docker compose up -d

# Application will be available at:
# - Main app: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - API docs: http://localhost:8080/v3/api-docs
```

## Architecture Patterns

### Spring Modulith Structure
The application follows Spring Modulith architecture with modules:
- `order` - Order management domain
- `users` - User and organization management
- `webhook` - Webhook handling and events
- `shared` - Common domain objects and utilities

Each module defines allowed dependencies in `package-info.java` using `@ApplicationModule`.

### Hexagonal Architecture
Each module follows hexagonal architecture:
- `domain/` - Core business entities and logic
- `port/in/` - Input ports (use cases/interfaces)
- `port/out/` - Output ports (repository interfaces)
- `adapter/in/` - Input adapters (controllers, web)
- `adapter/out/` - Output adapters (persistence, external services)
- `application/` - Application services coordinating use cases

### Database Schema
- Uses Liquibase for database migrations in `src/main/resources/db/changelog/`
- Master changelog: `db.changelog-master.yaml`
- Individual changesets in `changesets/` directory

## Configuration

### Application Properties
Main configuration in `src/main/resources/application.yaml` with environment variable overrides:

**Database:**
- `SPRING_DATASOURCE_URL` (default: jdbc:postgresql://localhost:5432/mydatabase)
- `SPRING_DATASOURCE_USERNAME` (default: root)
- `SPRING_DATASOURCE_PASSWORD` (default: secret)

**E-commerce Platform APIs:**
- Lazada: `LAZADA_APP_KEY`, `LAZADA_APP_SECRET`, `LAZADA_PRODUCT_URL`
- Shopee: `SHOPEE_ACCESS_TOKEN`, `SHOPEE_PARTNER_ID`, `SHOPEE_PRODUCT_URL`
- TikTok: `TIKTOK_ACCESS_TOKEN`, `TIKTOK_APP_KEY`, `TIKTOK_APP_SECRET`, `TIKTOK_SHOP_CIPHER`

**AWS S3:**
- `SPRING_CLOUD_AWS_S3_BUCKET`, `SPRING_CLOUD_AWS_S3_REGION`, `SPRING_CLOUD_AWS_ENDPOINT`

### Docker Compose Services
- **PostgreSQL 16.10** on port 5432
- **LocalStack** on port 4566 for AWS services (S3)

## Key Implementation Notes

### Entity Mapping
- Uses MapStruct for entity-to-domain object mapping
- Annotation processors configured for Lombok, MapStruct, Hibernate, and Bean Validation

### Testing Strategy
- Integration tests use Testcontainers with PostgreSQL and LocalStack
- Test configuration in `TestcontainersConfiguration.java`
- Modulith testing support included

### Code Generation
Annotation processors generate code for:
- Lombok (getters, setters, builders)
- MapStruct (object mappings)
- Hibernate (JPA metamodel)
- Bean Validation (constraint validators)

### Video Recording & Platform Integration
The core business logic revolves around:
- **Order Processing**: Fetching orders from e-commerce platforms (Shopee, Lazada, TikTok Shop)
- **Video Recording**: Managing packing session video recordings for each order
- **AWS S3 Storage**: Storing recorded videos and related media files
- **Platform APIs**: Integrating with e-commerce platforms through Feign clients with different authentication patterns

The application serves as a bridge between e-commerce platforms and video recording workflows, enabling sellers to provide video proof of packing for customer orders.