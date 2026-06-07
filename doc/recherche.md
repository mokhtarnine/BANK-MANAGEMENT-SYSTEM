# Bank Management System - Project Research

## 1. Project Idea

The Bank Management System is a Java desktop application that helps manage basic banking operations. The system allows bank employees to manage customers, open accounts, perform transactions, and view transaction history through a graphical interface.

The project is designed to apply the main concepts studied in Java II, including object-oriented programming, Swing GUI, collections, exception handling, threads, synchronization, unit testing, database storage, and logging.

## 2. Problem

Manual bank management can be slow and difficult to control. Managing customers, accounts, deposits, withdrawals, and transfers manually can lead to errors such as incorrect balances, missing transaction history, or duplicated operations.

Another important problem is concurrent access. In a banking system, two operations can happen at the same time. For example, two transfers can access the same account balance at the same moment. If the system is not synchronized correctly, the balance can become incorrect.

The project also needs to handle invalid operations properly, such as withdrawing more money than available, using a negative amount, or trying to use a closed account.

## 3. Proposed Solution

The proposed solution is to build a desktop banking application using Java Swing.

The application will provide a graphical interface where the user can create customers, open checking or savings accounts, make deposits, withdrawals, and transfers, and view all transaction history.

The system will use a layered architecture:

GUI Layer -> Controller Layer -> Service Layer -> Repository Layer -> Database

This architecture keeps the project clean because each layer has a specific responsibility.

- GUI displays data and receives user actions.
- Controller connects the GUI with the business logic.
- Service layer contains the main banking rules.
- Repository layer handles data storage.
- Database stores customers, accounts, transactions, and alerts.

## 4. Main Features

The application will include the following features:

- Create and manage customers.
- Open checking accounts and savings accounts.
- Display all customers and their accounts.
- Search customers or accounts.
- Deposit money into an account.
- Withdraw money from an account.
- Transfer money between two accounts.
- Close an account only if its balance is zero.
- Store transaction history for each account.
- Display transaction history in the GUI.
- Show error messages when an invalid operation happens.
- Monitor low balances using a background thread.
- Log transactions and warnings.
- Save and load data from a database.

## 5. Technologies and Tools

| Part | Tool / Technology |
|---|---|
| Programming language | Java |
| GUI | Java Swing |
| Build tool | Maven |
| Database | SQLite |
| Database access | JDBC |
| Testing | JUnit 5 |
| Logging | java.util.logging.Logger |
| IDE | IntelliJ IDEA / Eclipse / NetBeans |
| Version control | Git and GitHub |

## 6. Database

The system will use SQLite as the database.

SQLite is a good choice for this project because it is simple, lightweight, and does not need a server. The whole database is stored in one local file, for example:

bank.db

The database will store the main application data.

### customers

Stores customer information.

| Field | Description |
|---|---|
| id | Customer ID |
| name | Customer name |
| email | Customer email |

### employees

Stores employee or user information.

| Field | Description |
|---|---|
| id | Employee ID |
| username | Login username |
| password | Login password |
| role | Employee role |

### accounts

Stores account information.

| Field | Description |
|---|---|
| account_number | Account number |
| customer_id | Owner customer ID |
| account_type | Checking or Savings |
| balance | Current balance |
| closed | Account status |

### transactions

Stores transaction history.

| Field | Description |
|---|---|
| id | Transaction ID |
| account_number | Related account |
| transaction_type | Deposit, withdrawal, transfer |
| amount | Transaction amount |
| balance_after | Balance after transaction |
| timestamp | Date and time |
| employee_id | Employee who made the transaction |

### alerts

Stores warnings such as low-balance alerts.

| Field | Description |
|---|---|
| id | Alert ID |
| account_number | Related account |
| message | Alert message |
| timestamp | Date and time |

Database access should be placed in the repository layer, not inside the GUI.

Correct flow:

MainFrame -> BankController -> BankService -> BankRepository -> SQLite

This makes the project easier to maintain and easier to explain during the presentation.

## 7. Project Architecture

The project will be divided into packages.

- controller
- exception
- gui
- model
- repository
- service
- thread

### model

Contains the main data classes:

- Customer
- Employee
- Account
- CheckingAccount
- SavingsAccount
- Transaction
- TransactionType

### gui

Contains Swing interface classes:

- MainFrame
- DashboardPanel
- CustomerListPanel
- AccountDetailPanel
- TransactionDialog
- AlertLogPanel

### controller

Contains:

- BankController

The controller receives actions from the GUI and calls the service layer.

### service

Contains:

- BankService
- AuthService
- AuditService

The service layer contains the main business logic.

### repository

Contains:

- BankRepository
- JdbcBankRepository
- DatabaseManager

This layer saves and loads data from SQLite.

### exception

Contains custom exceptions:

- BankException
- InsufficientFundsException
- AccountClosedException
- InvalidAmountException
- OverdraftAlertException

### thread

Contains thread-related classes:

- AlertMonitor
- TransactionQueue
- TransactionWorker
- TransactionRequest

## 8. Important Business Rules

The application must respect these rules:

- Deposit amount must be positive.
- Withdrawal amount must be positive.
- A withdrawal cannot make the balance negative.
- A transfer must remove money from one account and add it to another account.
- Transfer must be synchronized to avoid race conditions.
- A closed account cannot be used for transactions.
- An account can be closed only if its balance is zero.
- Each transaction must be saved in history.
- The GUI must show clear error messages.

## 9. Threads and Synchronization

The project will use threads in two ways.

First, an AlertMonitor thread will run in the background and check if some account balances are below a limit.

Second, a TransactionQueue and TransactionWorker will be used to process transaction requests. This will demonstrate the use of wait() and notifyAll().

Transfers must also be synchronized to protect account balances when two operations happen at the same time.

## 10. Implementation Steps

The project can be implemented using these steps:

1. Create the Maven project.
2. Create the package structure.
3. Create model classes such as Customer, Account, CheckingAccount, SavingsAccount, and Transaction.
4. Create custom exceptions.
5. Create the database using SQLite.
6. Create DatabaseManager to connect to the database and create tables.
7. Create BankRepository and JdbcBankRepository.
8. Create BankService with banking logic.
9. Implement deposit, withdrawal, transfer, and close account.
10. Create JUnit tests for the service and model classes.
11. Create the main Swing window.
12. Create GUI panels and transaction dialog.
13. Connect GUI actions to BankController.
14. Display customers, accounts, and transaction history in tables.
15. Add the alert monitor thread.
16. Add transaction queue and worker.
17. Add logging.
18. Test the full application.
19. Prepare the report and final demo.

## 11. Task Split

The work can be divided between two members.

### Member 1: Backend

Responsible for:

- model
- service
- repository
- database
- exceptions
- threads
- JUnit tests

Main tasks:

- Create model classes.
- Create database tables.
- Implement repository classes.
- Implement banking logic.
- Implement exceptions.
- Implement transaction synchronization.
- Write unit tests.

### Member 2: Frontend

Responsible for:

- gui
- controller
- user interaction
- tables
- forms
- dialogs

Main tasks:

- Create Swing windows and panels.
- Create transaction dialog.
- Connect buttons to controller.
- Display customers and accounts.
- Display transaction history.
- Show error messages.
- Refresh GUI after operations.

Both members should work together on testing, debugging, GitHub, documentation, and the final presentation.

## 12. Conclusion

This project is a complete Java desktop application for managing bank customers, accounts, and transactions.

It uses Java Swing for the interface, SQLite for data storage, JDBC for database access, JUnit for testing, and Java threads for concurrent operations.

The project is useful because it combines many important Java concepts in one real application: object-oriented design, GUI, collections, exceptions, database, threads, tests, and logging.