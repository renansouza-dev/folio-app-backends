# folio-app-backends

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=com.renansouza%3Afolio-app-backends)](https://sonarcloud.io/summary/new_code?id=com.renansouza%3Afolio-app-backends)

A modular, micro-service-inspired backend suite for the **Folio App**, built with Java and Maven. It includes modules for account management and trade transactions.
Also implement a security library to enhance and address security concerns.

---

## Table of Contents

- [Modules](#modules)
- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)

---

## Modules

| Module Name              | Description                                                          |
|--------------------------|----------------------------------------------------------------------|
| `folio-app-web`          | Parent pom project for web apis                                      |
| `folio-app-accounts`     | Web API that handles exchange user accounts                          |
| `folio-app-transactions` | Web API that processes and records transactional exchange operations |
| `folio-app-security`     | Manages authentication, authorization, and related services          |


---

## Features

- Modular design following microservices separation of concerns
- Centralized web interface to route and manage module interactions
- Secure, authenticated operations across modules
- Maven-based build automation via `mvnw` for consistent environment setup

---

## Getting Started

### Prerequisites

- Java JDK 21+ (or your project's targeted Java version)
- Maven (or use the provided Maven Wrapper)
- Docker & Docker Compose (if using Docker configuration)
