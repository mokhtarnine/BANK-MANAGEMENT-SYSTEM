## Project Structure

The project will follow the standard Maven structure. This makes the project easy to build, test, and submit.

```text
BankManagementSystem/
│
├── pom.xml
├── README.md
├── bank.db
│
├── doc/
│   ├── recherche.md
│   └── report.md
│
├── logs/
│   └── bank.log
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           │
│   │   │           ├── Main.java
│   │   │           │
│   │   │           ├── controller/
│   │   │           │   └── BankController.java
│   │   │           │
│   │   │           ├── exception/
│   │   │           │   ├── BankException.java
│   │   │           │   ├── InsufficientFundsException.java
│   │   │           │   ├── AccountClosedException.java
│   │   │           │   ├── InvalidAmountException.java
│   │   │           │   └── OverdraftAlertException.java
│   │   │           │
│   │   │           ├── gui/
│   │   │           │   ├── MainFrame.java
│   │   │           │   ├── DashboardPanel.java
│   │   │           │   ├── CustomerListPanel.java
│   │   │           │   ├── AccountDetailPanel.java
│   │   │           │   ├── TransactionDialog.java
│   │   │           │   └── AlertLogPanel.java
│   │   │           │
│   │   │           ├── model/
|   |   |           |   ├── User.java
│   │   │           │   ├── Customer.java
│   │   │           │   ├── Employee.java
│   │   │           │   ├── Account.java
│   │   │           │   ├── CheckingAccount.java
│   │   │           │   ├── SavingsAccount.java
│   │   │           │   ├── Transaction.java
│   │   │           │   └── TransactionType.java
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   ├── DatabaseManager.java
│   │   │           │   ├── BankRepository.java
│   │   │           │   └── JdbcBankRepository.java
│   │   │           │
│   │   │           ├── service/
│   │   │           │   ├── BankService.java
│   │   │           │   ├── AuthService.java
│   │   │           │   └── AuditService.java
│   │   │           │
│   │   │           └── thread/
│   │   │               ├── AlertMonitor.java
│   │   │               ├── TransactionQueue.java
│   │   │               ├── TransactionWorker.java
│   │   │               └── TransactionRequest.java
│   │   │
│   │   └── resources/
│   │       ├── logging.properties
│   │       └── database/
│   │           └── schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── bank/
│                   ├── AccountTest.java
│                   ├── BankServiceTest.java
│                   ├── TransferTest.java
│                   └── RepositoryTest.java
