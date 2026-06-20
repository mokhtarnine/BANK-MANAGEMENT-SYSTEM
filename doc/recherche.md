# Bank Management System - Technical Research

## 1. Project Context

The Bank Management System (BMS) is a Java desktop application developed for a Java II group project. It simulates the main operations performed by a bank employee:

- employee authentication;
- customer creation and search;
- checking and savings account management;
- deposits, withdrawals, and transfers;
- transaction-history consultation;
- low-balance monitoring;
- data persistence and audit logging.

The project brings together the principal topics studied during the semester: object-oriented programming, collections, custom exceptions, Swing, threads, synchronization, JDBC, logging, and JUnit 5.

## 2. Problem Studied

A banking application must protect data consistency while keeping its interface simple to use. The important problems considered in this project are:

- preventing negative or invalid transaction amounts;
- preventing operations on closed accounts;
- preventing an account from being closed while it still has money;
- protecting account balances when several operations run concurrently;
- ensuring that a transfer either completes fully or makes no balance change;
- preserving customers, accounts, and transaction history between sessions;
- showing background warnings without updating Swing components from the wrong thread;
- separating GUI code from business and database logic.

## 3. Selected Solution

The application uses a layered architecture:

```text
Swing GUI
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
SQLite database
```

Each layer has one main responsibility:

- **GUI layer:** displays information and captures user actions.
- **Controller layer:** provides the operations used by the GUI and coordinates services and threads.
- **Service layer:** applies banking rules and creates transaction history.
- **Repository layer:** saves and loads data with JDBC.
- **Model layer:** represents users, customers, employees, accounts, and transactions.
- **Thread layer:** processes queued requests and monitors low balances.

This separation makes the program easier to test, explain, and maintain.

## 4. Technologies

| Technology | Use in the project |
|---|---|
| Java 17 | Main programming language |
| Java Swing | Desktop graphical interface |
| FlatLaf | Light modern look and feel for Swing |
| Maven | Build, dependency management, tests, and application launch |
| SQLite | Embedded local database stored in `bank.db` |
| JDBC | Database connections and SQL operations |
| JUnit 5 | Automated model, service, controller, logging, and repository tests |
| java.util.logging | Audit actions, warnings, and serious errors |
| Git | Version control |

Main Maven dependencies:

- `org.junit.jupiter:junit-jupiter`
- `org.xerial:sqlite-jdbc`
- `com.formdev:flatlaf`

## 5. Object-Oriented Design

### 5.1 User hierarchy

`User` is an abstract base class containing shared fields such as ID, full name, username, and password.

It has two subclasses:

- `Customer`, which owns an email and a list of accounts;
- `Employee`, which has a bank role such as `ADMIN` or `EMPLOYEE`.

### 5.2 Account hierarchy

`Account` is abstract because withdrawal rules differ between account types.

- `CheckingAccount` supports an overdraft limit.
- `SavingsAccount` prevents the balance from going below zero and supports an interest rate.

The common `deposit`, `closeAccount`, balance, closed status, transaction list, and lock behavior are stored in `Account`.

### 5.3 Immutable transactions

`Transaction` is declared `final`, and all its fields are `final`. A transaction records:

- a UUID transaction ID;
- a `TransactionType`;
- the amount;
- the timestamp;
- the balance after the operation;
- the employee who performed the operation.

The supported transaction types are `DEPOSIT`, `WITHDRAW`, `TRANSFER_IN`, and `TRANSFER_OUT`.

## 6. Collections

The project uses typed collections:

- `HashMap<String, Customer>` for fast customer lookup by ID;
- `ArrayList<Account>` for all bank accounts;
- `ArrayList<Account>` inside each customer;
- `LinkedList<Transaction>` for account transaction history;
- `LinkedList<TransactionRequest>` through the transaction queue;
- `HashSet<String>` to avoid repeating the same low-balance alert continuously.

No raw collection types are used.

## 7. Exception Handling

The custom exception hierarchy communicates banking errors clearly:

```text
BankException
|- InsufficientFundsException
|- AccountClosedException
|- AccountNotEmptyException
|- OverdraftAlertException
|- RepositoryException
```

`InvalidAmountException` extends `RuntimeException` because invalid values such as zero or negative amounts are programming or input errors.

Examples of enforced rules:

- a deposit or withdrawal amount must be positive;
- a savings withdrawal cannot exceed the balance;
- a checking withdrawal cannot exceed the balance plus overdraft limit;
- a closed account cannot receive or send money;
- an account with a non-zero balance cannot be closed.

The transaction dialog catches different exception types and displays suitable messages with `JOptionPane`.

## 8. Concurrency and Atomic Transfers

### 8.1 Account locks

Each account owns a `ReentrantLock`. Deposit, withdrawal, closing, and interest operations acquire this lock and release it in a `finally` block.

### 8.2 Atomic transfer

`BankService.transfer()` validates both accounts and locks the complete transfer operation. It also locks the source and target accounts before changing either balance.

The target account is validated before withdrawing from the source. Therefore, if the target is closed or another validation fails, the source balance and both transaction histories remain unchanged.

### 8.3 Transaction queue

`TransactionQueue` demonstrates thread communication:

- `enqueue()` adds a request and calls `notifyAll()`;
- `dequeue()` calls `wait()` while the queue is empty;
- `TransactionWorker` implements `Runnable` and processes queued requests.

The controller starts the worker thread and provides request methods for deposits, withdrawals, and transfers.

### 8.4 Alert monitor

`AlertMonitor` implements `Runnable` and checks accounts every three seconds. An alert is produced when an account balance falls below the configured threshold of `100.0`.

Previously alerted accounts are tracked to avoid repeating the same warning until their balance returns above the threshold.

Background alerts reach the Swing GUI through a callback. `MainFrame` uses `SwingUtilities.invokeLater()` before updating `AlertLogPanel`, which respects Swing thread-safety rules.

## 9. Persistence with SQLite

SQLite was selected because it is embedded, lightweight, and requires no external database server.

`DatabaseManager`:

- connects to `jdbc:sqlite:bank.db`;
- reads `src/main/resources/database/schema.sql`;
- creates missing tables when the application starts.

`JdbcBankRepository` uses:

- `PreparedStatement`;
- `ResultSet`;
- try-with-resources;
- JDBC transactions with commit and rollback;
- batch inserts;
- `RepositoryException` to wrap SQL failures.

The real database contains three tables:

### customers

Stores customer ID, full name, username, password, and email.

### accounts

Stores the customer relationship, account type, balance, closed status, checking overdraft limit, and savings interest rate.

### transactions

Stores transaction history and the employee information connected to each transaction.

The current implementation does not use separate `employees` or `alerts` tables. Default employees are created by `AuthService`, alerts are shown in the GUI and written to the audit log, and transaction employee details are stored with transaction records.

## 10. Graphical Interface

The application starts with `LoginFrame`. After successful authentication, `MainFrame` displays four tabs:

- **Dashboard:** total customers, total accounts, and aggregate balance;
- **Customers:** customer table, search, creation, refresh, and account navigation;
- **Accounts:** account search and type filter, account table, transaction history, and banking operations;
- **Alerts:** real-time low-balance and test alerts.

`TransactionDialog` supports deposits, withdrawals, and transfers.

The GUI performs banking operations through `BankController`. It does not access the repository or service layer directly.

FlatLaf provides the base light theme. `gui/style/UIStyle.java` centralizes:

- Segoe UI fonts;
- background and title colors;
- button appearance;
- table appearance;
- shared panel padding.

## 11. Authentication and Audit Logging

`AuthService` currently provides two demonstration employees:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `employee` | `emp123` | EMPLOYEE |

These accounts are stored in memory and are intended for a student demonstration, not production security.

`AuditService` writes to `logs/bank.log` using `logging.properties`:

- `INFO` for successful actions;
- `WARNING` for rejected operations, login failures, and low-balance alerts;
- `SEVERE` for unexpected errors.

The log file uses append mode, so earlier records are preserved.

## 12. Testing Strategy

The test suite covers:

- deposits and invalid amounts;
- normal and rejected withdrawals;
- checking-account overdraft behavior;
- account closing rules;
- customer creation and data;
- controller authentication and delegation;
- synchronous transactions;
- queued background transactions;
- account-table filtering;
- atomic transfer failure;
- SQLite save/load behavior;
- logging configuration and audit-file output.

Tests that do not need persistence use `BankService(false)` or `BankController(false)`. Repository tests use a temporary SQLite database so the real `bank.db` file is not modified.

The current complete Maven run executes 34 tests with no failures or errors.

## 13. Current Limitations and Future Improvements

The following points can be presented as known limitations:

- employee accounts are hard-coded and are not managed in the database;
- passwords are stored as plain text and are suitable only for demonstration;
- alerts are not stored in a dedicated database table;
- the alert threshold is fixed in the controller;
- savings interest exists in the model but has no GUI operation;
- account numbers are generated from the current account-list size;
- queued transaction errors are logged but are not returned to the GUI;
- `EmployeeTest.java` is currently empty;
- more parameterized and exception-message tests can be added.

Possible future work includes password hashing, employee management, configurable alerts, stronger account-number generation, alert persistence, and more complete test coverage.

## 14. Conclusion

The project demonstrates a complete multi-layer Java desktop application. It combines Swing and FlatLaf for the interface, collections for in-memory state, custom exceptions for business rules, locks and background threads for concurrency, SQLite/JDBC for persistence, logging for audit records, and JUnit 5 for verification.

The most important design decision is separation of responsibilities: the GUI communicates through the controller, business rules remain in the service and model layers, and SQL remains in the repository layer.
