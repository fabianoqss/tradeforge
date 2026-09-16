# TradeForge

TradeForge is an investment/trading simulation platform. Users trade virtual assets against real market prices, using a fictitious cash balance instead of real money.

No real funds are involved anywhere in the system — it is a sandbox for simulating portfolio management and order execution against live market data.

## Table of contents

- [Overview](#overview)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Domain model](#domain-model)
- [Core flow](#core-flow)
- [Getting started](#getting-started)
- [Roadmap](#roadmap)

## Overview

Each user owns a virtual portfolio with a cash balance and can buy and sell assets at prices sourced from external market data APIs.

Example:

```text
User holds:
  Cash Balance: $100,000
  10 AAPL
  5  NVDA
```

The platform fetches real market quotes and lets the user place buy/sell orders against them, tracking positions, average price, and profit & loss over time.

## Tech stack

- Java 21
- Spring Boot 4
- Spring Web / Spring MVC
- Spring Data JPA / Hibernate
- Spring Security (OAuth2 Authorization & Resource Server)
- PostgreSQL
- Flyway
- Bean Validation
- Lombok
- Maven

WebSocket support is included for future real-time features (live quotes, portfolio updates). R2DBC is intentionally not used — the project relies on the JDBC/JPA stack throughout.

## Architecture

TradeForge is built as a **modular monolith**, not a microservices system: a single Spring Boot application and a single deployment, with clear logical boundaries between business domains.

```text
TradeForge
│
├── auth
├── user
├── portfolio
├── asset
├── position
├── order
├── trading
├── market
├── risk
├── admin
├── ai
└── shared
```

This keeps operational complexity low (one process, one database, one deploy) while still enforcing separation of concerns between domains as the codebase grows.

## Domain model

### User

Represents a platform account.

| Field     | Description                     |
|-----------|----------------------------------|
| id        | Primary key                     |
| name      | Full name                       |
| email     | Login identifier                |
| password  | BCrypt-hashed password          |
| cpf       | Unique government identifier    |
| roles     | `USER`, `ADMIN`                 |

### Portfolio

The user's virtual wallet — a 1:1 relationship with `User`.

| Field        | Description                    |
|--------------|----------------------------------|
| id           | Primary key                    |
| user         | Owning side of the relationship (`user_id` FK, `NOT NULL UNIQUE`) |
| cashBalance  | Available fictitious cash      |
| positions    | Assets currently held           |

A portfolio cannot exist without a user, and is created together with the user account at registration time.

### Asset

A tradable instrument.

```text
symbol: AAPL
name: Apple Inc.
assetType: STOCK
exchange: NASDAQ
currency: USD
tradable: true
```

Assets are managed by administrators, who can register, enable, or disable them for trading.

### Position

How much of a given asset a given portfolio holds:

```text
Portfolio
├── Position — AAPL, quantity: 10, average price: $200
└── Position — NVDA, quantity: 5,  average price: $180
```

| Field         | Description                          |
|---------------|----------------------------------------|
| id            | Primary key                          |
| portfolio     | Owning portfolio                     |
| asset         | Related asset                        |
| quantity      | Units currently held                 |
| averagePrice  | Volume-weighted average entry price  |
| realizedPnl   | Profit/loss already realized         |

```text
Portfolio 1:N Position
Position  N:1 Asset
```

A portfolio can hold at most one `Position` per `Asset` (`UNIQUE(portfolio_id, asset_id)`).

### Order *(planned)*

A user's instruction to buy or sell an asset.

Types: `BUY`, `SELL`, `MARKET`, `LIMIT` (later: `STOP`, `STOP_LIMIT`).

```text
CREATED → PENDING → FILLED
                  → CANCELLED
                  → REJECTED
```

### Execution *(planned)*

The actual fulfillment of an `Order`. Orders and executions are modeled separately: an order expresses intent, an execution records what actually happened — a single order may later be filled through multiple executions.

## Core flow

```text
User
  └─ places an Order
       └─ Trading Engine
            ├─ validates the asset
            ├─ fetches the current quote
            ├─ checks available balance
            ├─ applies business/risk rules
            └─ executes the trade
                 └─ Execution
                      ├─ updates the Position (quantity, average price)
                      └─ updates the Portfolio (cash balance)
```

Example — buying 10 AAPL at $200/share with $10,000 in cash:

```text
Cost = 10 × $200 = $2,000

After execution:
  Cash    = $8,000
  Position: AAPL, quantity = 10, averagePrice = $200
```

**Average price** is volume-weighted across purchases:

```text
100 shares × $30 = $3,000
 50 shares × $40 = $2,000
─────────────────────────
150 shares → ($3,000 + $2,000) / 150 = $33.33
```

**P&L** is tracked in two forms:

- **Unrealized** — on an open position: `(currentPrice - averagePrice) × quantity`
- **Realized** — booked once a position is (partially) sold

All monetary values use `BigDecimal`, never floating-point types.

Market data is abstracted behind a provider-agnostic interface so the domain layer never depends on a specific vendor:

```java
public interface MarketDataClient {
    Quote getQuote(String symbol);
}
```

```text
External Market API → MarketDataClient → MarketDataService → Trading Engine
```

## Getting started

### Prerequisites

- JDK 21
- Maven
- PostgreSQL

### Configuration

The application is configured entirely through environment variables (`src/main/resources/application.yml`):

| Variable             | Description                          |
|----------------------|----------------------------------------|
| `DATASOURCE_URL`      | JDBC URL of the PostgreSQL database   |
| `DATASOURCE_USERNAME` | Database username                     |
| `DATASOURCE_PASSWORD` | Database password                     |
| `CLIENT_ID`           | OAuth2 client id                      |
| `CLIENT_SECRET`       | OAuth2 client secret                  |
| `JWT_DURATION`        | Access token lifetime                 |
| `CORS_ORIGINS`        | Allowed CORS origins                  |

### Running

```bash
mvn spring-boot:run
```

Database migrations are managed by Flyway and run automatically on startup, creating the schema and seeding the default roles (`ROLE_USER`, `ROLE_ADMIN`).

## Roadmap

- **Trading engine** — order placement, validation, and execution against live quotes
- **Concurrency & consistency** — safe concurrent balance updates via transactions, optimistic/pessimistic locking, and database constraints, avoiding naive CRUD-style writes
- **Market data integration** — pluggable client for an external quotes provider
- **Admin capabilities** — user management, asset lifecycle, risk rules, auditing (administrators manage the platform, never portfolios or executed trades directly)
- **Real-time updates** — WebSocket-based live quotes, portfolio and P&L updates, order notifications
- **AI Portfolio Analyst** — natural-language portfolio insights, risk explanations, news summarization/sentiment, and RAG over financial documents, layered on top of the deterministic trading core rather than replacing it
