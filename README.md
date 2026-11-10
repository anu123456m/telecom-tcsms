# Telecom Customer & Subscription Management System (TCSMS)

**Developed by:** Anu M  
**GitHub Repository:** [telecom-tcsms](https://github.com/anu123456m/telecom-tcsms)

## Project Overview

The Telecom Customer & Subscription Management System (TCSMS) is a menu-driven Java console application developed using JDBC and MySQL. It manages telecom customers, subscription plans, SIM assignments, usage records, billing, and payments.

The project follows a layered package structure consisting of `controller`, `service`, `dao`, `model`, `dto`, `exception`, `validation`, `security`, `util`, and `main`.

It uses plain JDBC with `PreparedStatement` for database operations without relying on frameworks.

## Technologies Used

- Java 8 language level (compiled using JDK 21)
- JDBC
- MySQL 8
- Maven
- IntelliJ IDEA
- Git and GitHub

## Database Design

The database contains 16 normalized tables with:

- Primary keys and foreign keys
- UNIQUE, NOT NULL, and CHECK constraints
- Indexes
- Status columns
- Created and updated timestamps

The database schema and sample seed data are available in `database/schema.sql`.

## Implemented Features

### 1. Authentication and Security
- Username and password authentication
- CAPTCHA verification
- Account lockout after three failed attempts for 15 minutes
- Login history and last login tracking
- Logout functionality
- OTP-based password recovery
- Salted PBKDF2 password hashing

### 2. Customer Registration
- Unique email and mobile number validation
- Password complexity validation
- Minimum age requirement of 18 years
- Mandatory field validation

### 3. Plan Catalogue
- Search and filter plans by price and data allowance
- Sort and compare plans
- Java Streams, lambdas, Predicate, and Comparator

### 4. Subscription Management
- Plan subscription
- Plan upgrades and downgrades
- Add-on management
- Subscription history
- JDBC transaction management for SIM assignment and subscription changes
- Validation to prevent inactive, duplicate, and admin-only plans

### 5. Usage and Billing
- JDBC batch processing for usage records
- Usage analytics using the Stream API
- Usage by type and monthly usage reports
- Top data consumers
- Monthly bill generation
- 18% tax calculation and discount rules

### 6. Payment Management
- Transaction-based payment processing
- Bill and payment amount validation
- Payment record creation and bill status updates
- Audit logging
- Commit and rollback handling
- Savepoint implementation for notification handling
- Duplicate payment protection using row locking and unique references

Concurrency testing was performed using two threads attempting to pay the same bill.

## Design Patterns

- **DAO Pattern:** Separates database operations from business logic.
- **Singleton Pattern:** Provides a shared instance where required.
- **Strategy Pattern:** Supports flexible discount rule implementation.

## Modules in Progress

- Complaint Management
- Administrator Module:
  - Customer management
  - Plan management
  - SIM management
  - Subscription management
- Stream-based reports with CSV and text export
- Background services:
  - BillingScheduler
  - PaymentNotificationService
  - UsageProcessor
  - AccountMonitor

Java concurrency features being used include `ExecutorService`, `ScheduledExecutorService`, `Callable`, `Future`, and `BlockingQueue`.

## How to Run

1. Clone the repository:

   ```bash
   git clone https://github.com/anu123456m/telecom-tcsms.git
   ```

2. Open the project in IntelliJ IDEA.

3. Execute `database/schema.sql` in MySQL 8.

4. Copy `src/main/resources/db.properties.example` to `db.properties` and configure your MySQL credentials.

5. Run `com.amdocs.telecom.main.DataSeeder` once.

6. Run `com.amdocs.telecom.main.Main` to start the application.

## Sample Login Credentials

**Customer**
- Username: `arjun`
- Password: `Test@1234`

**Administrator**
- Username: `admin`
- Password: `Admin@123`

These are sample development credentials. Do not use real passwords in a public repository.

## Project Status

Core modules, including authentication, registration, plan catalogue, subscriptions, usage management, billing, and payments, have been implemented.

Complaint management, administrator operations, reporting, and background services are currently in progress.

---

**Developed by Anu M**
