# TradeForge

TradeForge is a personal project built to deepen my knowledge of Java backend development, Spring Boot, domain modeling, databases, transactions, concurrency, external API integration, and — later on — artificial intelligence.

This README documents the vision, architecture, and design decisions guiding the project as it evolves.

## Mentorship approach

While developing this project with AI assistance, the goal is not to have the entire codebase implemented automatically. Instead, the collaboration follows these principles:

1. Don't auto-implement large parts of the project unless necessary.
2. Explain the concept and business rule first.
3. Guide toward the solution rather than just handing it over.
4. Review the code that gets written.
5. Point out modeling or architectural mistakes.
6. When multiple solutions exist, explain the trade-offs.
7. Prioritize Java/Spring best practices.
8. Avoid introducing microservices without a very strong justification.
9. Avoid adding technology just for the sake of complexity.
10. Keep in mind that the goal is to learn by building.

## 1. Overview

TradeForge is an **investment/trading simulation platform**.

There is no real money involved. Each user has a virtual wallet with a fictitious balance and can buy and sell assets using prices obtained from financial market APIs.

Example:

A user holds:

- Cash Balance: $100,000
- 10 AAPL
- 5 NVDA

The system fetches market prices through an external API and lets the user create buy and sell orders.

## 2. Architecture

**Important:** this project will **not** use microservices.

It is being built as a **modular monolith** in Spring Boot.

Conceptual structure:

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

A single Spring Boot application, a single deployment, with logical separation between the different domains.

## 3. Current stack

Backend:

- Java
- Spring Boot
- Spring Web / Spring MVC
- Spring Data JPA
- Hibernate
- Spring Security
- PostgreSQL
- Bean Validation
- Flyway
- Lombok
- Maven
- Spring Boot DevTools

WebSocket is being considered for later stages, mainly for real-time quote updates.

R2DBC is not being used.

## 4. Core domain

At this stage, the main entities being modeled are:

### User

Represents a platform user.

Example attributes:

- id
- name
- email
- password
- role
- status
- createdAt

There will initially be two roles:

- USER
- ADMIN

### Portfolio

Represents the user's virtual financial wallet.

Example attributes:

- id
- user
- cashBalance
- positions

The initial design is a 1:1 relationship between User and Portfolio.

The foreign key will likely live on the Portfolio side:

```text
Portfolio
    |
    +-- user_id
```

Therefore:

```text
User 1:1 Portfolio
```

### Asset

Represents a tradable asset.

Example:

```text
symbol: AAPL
name: Apple Inc.
assetType: STOCK
exchange: NASDAQ
currency: USD
tradable: true
```

Possible attributes:

- id
- symbol
- name
- assetType
- exchange
- currency
- tradable

The ADMIN will be able to register, enable, or disable assets available for trading.

### Position

A Position represents how much of a given asset a given portfolio holds.

Example:

```text
Portfolio
|
+-- Position
|   Asset: AAPL
|   Quantity: 10
|   Average Price: $200
|
+-- Position
    Asset: NVDA
    Quantity: 5
    Average Price: $180
```

A Position should have roughly:

- id
- portfolio
- asset
- quantity
- averagePrice
- realizedPnl

Relationships:

```text
Portfolio 1:N Position
Position N:1 Asset
```

Position is the entity that answers:

"This portfolio holds X units of this asset at an average price of Y."

The same portfolio should never have two different Positions for the same Asset.

A constraint similar to the following is planned:

```text
UNIQUE(portfolio_id, asset_id)
```

### Order

To be implemented later.

Represents a user's instruction to buy or sell a given asset.

Initial scope:

- BUY
- SELL
- MARKET
- LIMIT

Later:

- STOP
- STOP_LIMIT

An Order will have a lifecycle similar to:

```text
CREATED
   |
PENDING
   |
   +----> CANCELLED
   |
   +----> REJECTED
   |
   +----> FILLED
```

### Execution

Order and Execution will not be the same entity.

Order represents the trading intent.

Execution represents the actual fulfillment of the order.

In the future, an Order may have multiple Executions/Fills, though this can be simplified for the MVP.

## 5. Main system flow

The expected basic flow is:

```text
User
   |
   v
Creates an Order
   |
   v
Trading Engine
   |
   +--> validates the Asset
   |
   +--> checks the quote
   |
   +--> checks the balance
   |
   +--> checks business rules
   |
   v
Executes BUY/SELL
   |
   v
Execution
   |
   v
Updates the Position
   |
   v
Updates the Portfolio
```

Example:

The user holds:

```text
Cash = $10,000
```

Requests:

```text
BUY
10 AAPL
MARKET
```

Quote:

```text
AAPL = $200
```

Cost:

```text
10 × $200 = $2,000
```

After execution:

```text
Cash = $8,000

Position:
AAPL
quantity = 10
averagePrice = $200
```

## 6. Average price

If the user buys:

```text
100 shares × $30 = $3,000
50 shares × $40 = $2,000
```

The average price must weight the quantities:

```text
($3,000 + $2,000) / 150
= $33.33
```

This calculation is the responsibility of the domain/trading logic, not an LLM.

## 7. P&L

The system must distinguish between:

**Unrealized P&L**

Profit/loss of a position that is still open.

Example:

```text
averagePrice = $200
currentPrice = $230
quantity = 10

($230 - $200) × 10
= +$300
```

**Realized P&L**

Profit/loss actually realized when a sale occurs.

Monetary values must use `BigDecimal`, not `double`.

## 8. Market Data

The system must consume an external financial market API.

The plan is to keep an abstraction similar to:

```java
public interface MarketDataClient {

    Quote getQuote(String symbol);

}
```

This way, the domain layer stays decoupled from any specific provider.

The flow will be:

```text
External Market API
        |
        v
MarketDataClient
        |
        v
MarketDataService
        |
        v
Trading Engine
```

## 9. Concurrency

One of the advanced topics to explore in this project is concurrency.

Example problem:

```text
Balance = $10,000

Request A:
BUY $8,000

Request B:
BUY $8,000
```

If both requests read the balance at the same time, the system must not allow $16,000 worth of purchases to go through.

Topics to study and apply where needed:

- `@Transactional`
- optimistic locking
- pessimistic locking
- isolation levels
- database constraints
- idempotency

The goal is not to build a simple CRUD app, but to explore real consistency problems.

## 10. ADMIN

The ADMIN role is responsible for administering the platform, not for performing arbitrary operations on user wallets.

Initially:

- user management
- blocking/unblocking users
- registering assets
- enabling/disabling assets

Later:

- global risk rules
- order monitoring
- auditing
- status of external integrations

The ADMIN must never directly alter P&L, positions, or executed orders.

## 11. WebSocket

WebSocket is not a priority for the MVP.

Initially:

```text
GET /api/market/quote/AAPL
```

will be enough.

Later, WebSocket may provide:

- real-time quotes
- portfolio updates
- P&L updates
- order execution notifications

## 12. Artificial Intelligence

AI will be added **after the core Trading Engine is working**.

AI will not be responsible for deterministic financial calculations.

The plan is to later build something similar to an:

**AI Portfolio Analyst**

Possible features:

- explaining why the portfolio gained/lost value
- analyzing portfolio concentration
- explaining risks
- summarizing news related to held assets
- news sentiment analysis
- anomaly detection
- RAG over financial documents
- answering questions about the portfolio itself

Possible future architecture:

```text
Spring Boot
│
├── Trading Engine
├── Portfolio
├── Market
├── Risk Engine
└── AI Integration
         |
         +--> LLM
         |
         +--> RAG
         |
         +--> Python/FastAPI ML
```

Java/Spring will remain the main application.

Python may be used later, only where Machine Learning genuinely makes sense.

## 13. Educational goal

This project isn't just about shipping a working application.

The goal is to use it to deeply learn:

- Java
- Spring Boot
- JPA/Hibernate
- Spring Security
- PostgreSQL
- domain modeling
- transactions
- concurrency
- locking
- external APIs
- WebSocket
- testing
- software architecture
- Java + AI/ML integration

## Current status

The project is in the **initial entity and JPA relationship modeling** stage (User, Portfolio, Role, Asset, Position), with the authentication/registration flow being built first.
