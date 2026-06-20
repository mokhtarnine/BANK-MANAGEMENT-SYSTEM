# Bank Management System

A Java Swing desktop application for managing bank customers, checking and savings accounts, transactions, alerts, and persistent account data.

The project was created for Java II and demonstrates layered architecture, object-oriented programming, collections, custom exceptions, concurrency, JDBC, logging, Swing, and JUnit 5.

## Features

- Employee login
- Customer creation and search
- Checking and savings accounts
- Account-number search and account-type filtering
- Deposits, withdrawals, and atomic transfers
- Account closure when the balance is zero
- Transaction history with employee and balance information
- Dashboard totals for customers, accounts, and balances
- Background low-balance monitoring
- Transaction queue using `wait()` and `notifyAll()`
- SQLite persistence between application sessions
- Audit logging to `logs/bank.log`
- FlatLaf light styling with shared `UIStyle`

## Requirements

- Java Development Kit (JDK) 17 or newer
- Apache Maven 3.9 or newer

The project is compiled for Java 17.

## Build

Open a terminal in the project root:

```powershell
cd "C:\Users\mokht\OneDrive\Desktop\JAVA PROJECT\BMS"
```

Compile the application:

```powershell
mvn compile
```

Run all automated tests:

```powershell
mvn test
```

Create the JAR package:

```powershell
mvn package
```

## Run

Start the Swing application with Maven:

```powershell
mvn exec:java
```

The application opens `LoginFrame`, then displays `MainFrame` after successful authentication.

## Demo Login Accounts

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `employee` | `emp123` | EMPLOYEE |

Pressing Enter in the login window activates the Login button.

These credentials are hard-coded for a student demonstration. They are not intended for a production system.

## Application Workflow

1. Log in with one of the demonstration employee accounts.
2. Open the **Customers** tab and create or search for a customer.
3. Select a customer and click **View Accounts**.
4. Open a checking or savings account.
5. Select an account to view its transaction history.
6. Use **Deposit**, **Withdraw**, or **Transfer**.
7. Use the Dashboard to review totals.
8. Check the Alerts tab for low-balance warnings.

An account can be closed only when its balance is exactly zero.

## Architecture

```text
GUI (Swing + FlatLaf)
        |
        v
BankController
        |
        v
BankService / AuthService / AuditService
        |
        v
BankRepository
        |
        v
SQLite (bank.db)
```

### Packages

| Package | Responsibility |
|---|---|
| `com.bank.model` | Users, customers, employees, accounts, and transactions |
| `com.bank.exception` | Banking and repository exceptions |
| `com.bank.service` | Authentication, business rules, persistence coordination, and logging |
| `com.bank.controller` | API used by the GUI and thread coordination |
| `com.bank.repository` | SQLite/JDBC save and load operations |
| `com.bank.thread` | Transaction queue, worker, requests, and alert monitoring |
| `com.bank.gui` | Login, main frame, panels, and transaction dialog |
| `com.bank.gui.style` | Reusable Swing colors, fonts, buttons, tables, and padding |

The GUI sends operations through `BankController`. SQL code is kept in the repository layer.

## Project Structure

```text
BMS/
|-- pom.xml
|-- README.md
|-- bank.db
|-- doc/
|   |-- recherche.md
|   |-- StructureProject.md
|   `
|-- logs/
|   `-- bank.log
`-- src/
    |-- main/
    |   |-- java/com/bank/
    |   |   |-- Main.java
    |   |   |-- controller/
    |   |   |-- exception/
    |   |   |-- gui/
    |   |   |   `-- style/
    |   |   |-- model/
    |   |   |-- repository/
    |   |   |-- service/
    |   |   `-- thread/
    |   `-- resources/
    |       |-- logging.properties
    |       `-- database/schema.sql
    `-- test/java/com/bank/
```

## Database

SQLite stores data in `bank.db`. Tables are created automatically from `src/main/resources/database/schema.sql`.

The database contains:

- `customers`
- `accounts`
- `transactions`

The repository preserves checking/savings account types, balances, closed status, overdraft limits, interest rates, transaction history, and transaction employee information.

The database file is created relative to the directory where the application is launched. Run Maven from the project root to use the included `bank.db`.

## Concurrency

- Each account uses a `ReentrantLock`.
- Transfers lock the complete operation and both involved accounts.
- A failed transfer does not partially change balances or histories.
- `TransactionQueue` uses synchronized methods, `wait()`, and `notifyAll()`.
- `TransactionWorker` processes queued requests in the background.
- `AlertMonitor` checks balances every three seconds.
- Background alerts update Swing through `SwingUtilities.invokeLater()`.

The configured low-balance threshold is `100.0`.

## Exceptions

Important custom exceptions include:

- `InsufficientFundsException`
- `AccountClosedException`
- `AccountNotEmptyException`
- `InvalidAmountException`
- `OverdraftAlertException`
- `RepositoryException`

The GUI catches transaction exceptions and displays messages using `JOptionPane`.

## Logging

Audit records are written to:

```text
logs/bank.log
```

Configuration is stored in:

```text
src/main/resources/logging.properties
```

Log levels:

- `INFO`: successful operations
- `WARNING`: rejected operations, failed login attempts, and low-balance alerts
- `SEVERE`: unexpected errors

## Testing

Tests are located in `src/test/java/com/bank`.

The suite covers model rules, service operations, controller behavior, background requests, repository persistence, transaction history, atomic-transfer failure, and audit logging.

Repository tests use a temporary SQLite database. Service and controller tests can disable real persistence, so tests do not depend on the project `bank.db`.

Current verified result:

```text
Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Documentation

- Technical research: `doc/recherche.md`
- Project structure: `doc/StructureProject.md`
- UML diagram: `doc/uml.png`

## Known Limitations

- Employee accounts are hard-coded.
- Passwords are stored as plain text for demonstration.
- Alerts are logged and displayed but are not stored in a separate database table.
- The alert threshold is fixed.
- Savings interest has no GUI command.
- Account-number generation is based on the current account count.
- `EmployeeTest.java` is currently empty.

## Submission Note

For the final ZIP submission, include source code, `pom.xml`, documentation, and `README.md`. Exclude generated build output such as the `target/` directory and compiled `.class` files.
