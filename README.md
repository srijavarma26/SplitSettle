# SplitSettle — Distributed Expense Splitting & Settlement System

SplitSettle is a production-ready microservices-based expense splitting system (similar to Splitwise) built with Java 17, Spring Boot 3, Spring Cloud Gateway, OpenFeign, and MySQL. It is designed to demonstrate clean microservice architecture, strict database-per-service isolation, inter-service communication, and a greedy graph-based debt simplification algorithm.

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    Client["Client / Postman"] -->|Port 8080| Gateway["API Gateway (Spring Cloud Gateway)"]
    
    Gateway -->|/api/v1/auth/** & /api/v1/users/**| UserService["User Service (Port 8081)"]
    Gateway -->|/api/v1/groups/** & /api/v1/users/{id}/balances| ExpenseService["Expense Service (Port 8082)"]
    Gateway -->|/api/v1/settlements/**| SettlementService["Settlement Service (Port 8083)"]

    ExpenseService -->|OpenFeign / REST| UserService
    SettlementService -->|OpenFeign / REST| ExpenseService

    UserService --> UserDB[("MySQL: user-db")]
    ExpenseService --> ExpenseDB[("MySQL: expense-db")]
    SettlementService --> SettlementDB[("MySQL: settlement-db")]
```

---

## 🧩 Microservices Breakdown

| Service | Port | Database | Responsibilities |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | None | Centralized routing for external requests into microservices. |
| **User Service** | `8081` | `splitsettle_user_db` | User registration, JWT-based authentication, password hashing (BCrypt), user profile retrieval. |
| **Expense Service** | `8082` | `splitsettle_expense_db` | Group creation, group member management, expense recording (`EQUAL`, `PERCENTAGE`, `EXACT_AMOUNT`), calculation of net user balances. Calls User Service via OpenFeign to validate user existence. |
| **Settlement Service** | `8083` | `splitsettle_settlement_db` | **Debt Simplification Algorithm**: Calculates minimum required transactions to settle all group debts using dual Max-Heaps. Manages settlement records & marks payments as paid. Calls Expense Service via OpenFeign. |

---

## 🧮 Debt Simplification Algorithm

When group members incur expenses, a naive debt graph can result in up to $N(N-1)$ individual transactions (e.g., $A$ owes $B$, $B$ owes $C$, $C$ owes $A$).

SplitSettle utilizes a **Greedy Dual-Priority-Queue (Max-Heap) Algorithm**:
1. Calculate the **net balance** for each user ($B_{net} = \text{Total Credited} - \text{Total Debited}$).
2. Separate users into two Max-Heaps:
   - **Debtors Heap**: Users with negative net balances (ordered by absolute debt amount descending).
   - **Creditors Heap**: Users with positive net balances (ordered by credit amount descending).
3. In each step, extract the largest debtor $D$ and largest creditor $C$:
   - $\text{Settle Amount} = \min(|B_D|, |B_C|)$
   - Record transaction: $D \rightarrow C$ for $\text{Settle Amount}$.
   - Update balances. Re-insert remaining balances into respective heaps if $> 0.00$.
4. **Result**: Reduces $N$ participants' transactions to **at most $N-1$ transactions**, eliminating circular and transitive debts.

---

## 🚀 Quickstart — Running with Docker Compose

### Prerequisites
- Docker & Docker Compose installed.

### Step 1: Start All Services & Databases
Run the following command from the root directory:
```bash
docker-compose up --build -d
```

### Step 2: Verify Running Containers
```bash
docker-compose ps
```
You will see 7 containers running:
- `splitsettle-user-db`
- `splitsettle-expense-db`
- `splitsettle-settlement-db`
- `splitsettle-user-service`
- `splitsettle-expense-service`
- `splitsettle-settlement-service`
- `splitsettle-api-gateway`

---

## 📮 API Documentation & Sample cURL Requests

All requests can be routed through the **API Gateway** on port `8080`.

### 1. User Registration & Login (User Service)

#### Register User 1 (Alice)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice@example.com",
    "password": "password123"
  }'
```
*Response (`id: 1`)*

#### Register User 2 (Bob)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Jones",
    "email": "bob@example.com",
    "password": "password123"
  }'
```
*Response (`id: 2`)*

#### Register User 3 (Charlie)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Charlie Brown",
    "email": "charlie@example.com",
    "password": "password123"
  }'
```
*Response (`id: 3`)*

#### Login User
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

---

### 2. Group & Expense Management (Expense Service)

#### Create a Group
```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Summer Vacation",
    "description": "Trip to Hawaii",
    "createdBy": 1
  }'
```
*Response (`groupId: 1`, members: `[1]`)*

#### Add Members to Group
```bash
# Add Bob (User ID 2)
curl -X POST http://localhost:8080/api/v1/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{"userId": 2}'

# Add Charlie (User ID 3)
curl -X POST http://localhost:8080/api/v1/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{"userId": 3}'
```

---

### 3. Add Expenses (EQUAL, PERCENTAGE, EXACT_AMOUNT)

#### Add Expense 1: EQUAL Split (Dinner - $90 paid by Alice)
```bash
curl -X POST http://localhost:8080/api/v1/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidByUserId": 1,
    "amount": 90.00,
    "description": "Team Dinner",
    "splitType": "EQUAL"
  }'
```
*(Alice: +$60.00, Bob: -$30.00, Charlie: -$30.00)*

#### Add Expense 2: PERCENTAGE Split (Groceries - $100 paid by Bob)
```bash
curl -X POST http://localhost:8080/api/v1/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidByUserId": 2,
    "amount": 100.00,
    "description": "Groceries",
    "splitType": "PERCENTAGE",
    "splits": [
      {"userId": 1, "percentage": 40.00},
      {"userId": 2, "percentage": 30.00},
      {"userId": 3, "percentage": 30.00}
    ]
  }'
```

#### Add Expense 3: EXACT_AMOUNT Split (Cab fare - $60 paid by Charlie)
```bash
curl -X POST http://localhost:8080/api/v1/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidByUserId": 3,
    "amount": 60.00,
    "description": "Airport Cab",
    "splitType": "EXACT_AMOUNT",
    "splits": [
      {"userId": 1, "amount": 20.00},
      {"userId": 2, "amount": 40.00},
      {"userId": 3, "amount": 0.00}
    ]
  }'
```

---

### 4. Group Balances & User Summary

#### Get Group Net Balances
```bash
curl -X GET http://localhost:8080/api/v1/groups/1/balances
```

#### Get User's Overall Owed / Owed-To Summary
```bash
curl -X GET http://localhost:8080/api/v1/users/1/balances
```

---

### 5. Settlement Service (Debt Simplification & Marking Paid)

#### Generate Simplified Settlement Plan
```bash
curl -X GET http://localhost:8080/api/v1/settlements/group/1
```
*Returns the optimal minimum transactions required to settle all balances.*

#### Mark Settlement Transaction as Paid
```bash
curl -X POST http://localhost:8080/api/v1/settlements/1/pay
```

---

## 🧪 Unit Tests

The core **Debt Simplification Algorithm** is unit-tested with JUnit 5.

To run the unit tests via Maven (when Java & Maven are installed locally):
```bash
cd settlement-service
mvn test
```
Or during `docker-compose build`, tests automatically run in the Maven build stage!

---

## 📁 Project Structure

```
SplitSettle/
├── api-gateway/           # Spring Cloud Gateway (Port 8080)
├── user-service/          # User management & JWT Auth (Port 8081)
├── expense-service/       # Groups & Expenses (Port 8082)
├── settlement-service/    # Debt Simplification & Settlements (Port 8083)
├── docker-compose.yml     # Complete orchestration setup
└── README.md              # Project documentation
```
