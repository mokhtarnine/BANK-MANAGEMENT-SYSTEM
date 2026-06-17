package com.bank.repository;

import com.bank.exception.BankException;
import com.bank.exception.RepositoryException;
import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class JdbcBankRepository implements BankRepository {

    private final DatabaseManager databaseManager;

    public JdbcBankRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void saveCustomers(HashMap<String, Customer> customers) throws BankException {
        String deleteSql = "DELETE FROM customers";

        String insertSql = """
                INSERT INTO customers (
                    id,
                    full_name,
                    username,
                    password,
                    email
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = databaseManager.getConnection();
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                PreparedStatement insertStatement = connection.prepareStatement(insertSql)
        ) {
            deleteStatement.executeUpdate();

            for (Customer customer : customers.values()) {
                insertStatement.setString(1, customer.getId());
                insertStatement.setString(2, customer.getFullName());
                insertStatement.setString(3, customer.getUsername());
                insertStatement.setString(4, customer.getPassword());
                insertStatement.setString(5, customer.getEmail());
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to save customers", e);
        }
    }

    @Override
    public HashMap<String, Customer> loadCustomers() throws BankException {
        String sql = """
                SELECT
                    id,
                    full_name,
                    username,
                    password,
                    email
                FROM customers
                """;

        HashMap<String, Customer> customers = new HashMap<>();

        try (
                Connection connection = databaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Customer customer = new Customer(
                        resultSet.getString("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );

                customers.put(customer.getId(), customer);
            }

            return customers;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to load customers", e);
        }
    }

    @Override
    public void saveAccounts(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException {
        try (Connection connection = databaseManager.getConnection()) {
            saveAccounts(connection, customers, accounts);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to save accounts", e);
        }
    }

    @Override
    public ArrayList<Account> loadAccounts(HashMap<String, Customer> customers) throws BankException {
        try (Connection connection = databaseManager.getConnection()) {
            return loadAccounts(connection, customers);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to load accounts", e);
        }
    }

    @Override
    public void saveAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                saveTransactions(connection, new ArrayList<>());
                saveAccounts(connection, customers, new ArrayList<>());
                saveCustomers(connection, customers);
                saveAccounts(connection, customers, accounts);
                saveTransactions(connection, accounts);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Failed to save bank data", e);
        }
    }

    @Override
    public void loadAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException {
        customers.clear();
        accounts.clear();

        try (Connection connection = databaseManager.getConnection()) {
            HashMap<String, Customer> loadedCustomers = loadCustomers(connection);
            customers.putAll(loadedCustomers);

            ArrayList<Account> loadedAccounts = loadAccounts(connection, customers);
            accounts.addAll(loadedAccounts);

            loadTransactions(connection, accounts);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to load bank data", e);
        }
    }

    private void saveCustomers(
            Connection connection,
            HashMap<String, Customer> customers
    ) throws SQLException {
        String deleteSql = "DELETE FROM customers";

        String insertSql = """
                INSERT INTO customers (
                    id,
                    full_name,
                    username,
                    password,
                    email
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                PreparedStatement insertStatement = connection.prepareStatement(insertSql)
        ) {
            deleteStatement.executeUpdate();

            for (Customer customer : customers.values()) {
                insertStatement.setString(1, customer.getId());
                insertStatement.setString(2, customer.getFullName());
                insertStatement.setString(3, customer.getUsername());
                insertStatement.setString(4, customer.getPassword());
                insertStatement.setString(5, customer.getEmail());
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
        }
    }

    private HashMap<String, Customer> loadCustomers(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    id,
                    full_name,
                    username,
                    password,
                    email
                FROM customers
                """;

        HashMap<String, Customer> customers = new HashMap<>();

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Customer customer = new Customer(
                        resultSet.getString("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );

                customers.put(customer.getId(), customer);
            }
        }

        return customers;
    }

    private void saveAccounts(
            Connection connection,
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws SQLException {
        String deleteSql = "DELETE FROM accounts";

        String insertSql = """
                INSERT INTO accounts (
                    account_number,
                    customer_id,
                    account_type,
                    balance,
                    closed,
                    overdraft_limit,
                    interest_rate
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                PreparedStatement insertStatement = connection.prepareStatement(insertSql)
        ) {
            deleteStatement.executeUpdate();

            for (Account account : accounts) {
                String customerId = findCustomerIdForAccount(customers, account);

                if (customerId == null) {
                    continue;
                }

                insertStatement.setString(1, account.getAccountNumber());
                insertStatement.setString(2, customerId);
                insertStatement.setString(3, getAccountType(account));
                insertStatement.setDouble(4, account.getBalance());
                insertStatement.setInt(5, account.isClosed() ? 1 : 0);

                if (account instanceof CheckingAccount checkingAccount) {
                    insertStatement.setDouble(6, checkingAccount.getOverdraftLimit());
                    insertStatement.setNull(7, Types.REAL);
                } else if (account instanceof SavingsAccount savingsAccount) {
                    insertStatement.setNull(6, Types.REAL);
                    insertStatement.setDouble(7, savingsAccount.getInterestRate());
                } else {
                    insertStatement.setNull(6, Types.REAL);
                    insertStatement.setNull(7, Types.REAL);
                }

                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
        }
    }

    private ArrayList<Account> loadAccounts(
            Connection connection,
            HashMap<String, Customer> customers
    ) throws SQLException {
        String sql = """
                SELECT
                    account_number,
                    customer_id,
                    account_type,
                    balance,
                    closed,
                    overdraft_limit,
                    interest_rate
                FROM accounts
                """;

        ArrayList<Account> accounts = new ArrayList<>();

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Account account = createAccountFromResultSet(resultSet);
                account.setClosed(resultSet.getInt("closed") == 1);

                accounts.add(account);

                Customer customer = customers.get(resultSet.getString("customer_id"));

                if (customer != null) {
                    customer.addAccount(account);
                }
            }
        }

        return accounts;
    }

    private Account createAccountFromResultSet(ResultSet resultSet) throws SQLException {
        String accountNumber = resultSet.getString("account_number");
        String accountType = resultSet.getString("account_type");
        double balance = resultSet.getDouble("balance");

        if ("CHECKING".equalsIgnoreCase(accountType)) {
            return new CheckingAccount(
                    accountNumber,
                    balance,
                    resultSet.getDouble("overdraft_limit")
            );
        }

        if ("SAVINGS".equalsIgnoreCase(accountType)) {
            return new SavingsAccount(
                    accountNumber,
                    balance,
                    resultSet.getDouble("interest_rate")
            );
        }

        throw new SQLException("Unknown account type: " + accountType);
    }

    private void saveTransactions(
            Connection connection,
            ArrayList<Account> accounts
    ) throws SQLException {
        String deleteSql = "DELETE FROM transactions";

        String insertSql = """
                INSERT INTO transactions (
                    transaction_id,
                    account_number,
                    transaction_type,
                    amount,
                    timestamp,
                    balance_after,
                    employee_id,
                    employee_name,
                    employee_username,
                    employee_password,
                    employee_role
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                PreparedStatement insertStatement = connection.prepareStatement(insertSql)
        ) {
            deleteStatement.executeUpdate();

            for (Account account : accounts) {
                for (Transaction transaction : account.getTransaction()) {
                    insertStatement.setString(1, transaction.getTransactionId());
                    insertStatement.setString(2, account.getAccountNumber());
                    insertStatement.setString(3, transaction.getType().name());
                    insertStatement.setDouble(4, transaction.getAmount());
                    insertStatement.setString(5, transaction.getTimestamp().toString());
                    insertStatement.setDouble(6, transaction.getBalanceAfter());
                    setEmployeeParameters(insertStatement, transaction.getPerformedBy());
                    insertStatement.addBatch();
                }
            }

            insertStatement.executeBatch();
        }
    }

    private void setEmployeeParameters(
            PreparedStatement statement,
            Employee employee
    ) throws SQLException {
        if (employee == null) {
            statement.setString(7, null);
            statement.setString(8, null);
            statement.setString(9, null);
            statement.setString(10, null);
            statement.setString(11, null);
            return;
        }

        statement.setString(7, employee.getId());
        statement.setString(8, employee.getFullName());
        statement.setString(9, employee.getUsername());
        statement.setString(10, employee.getPassword());
        statement.setString(11, employee.getRole());
    }

    private void loadTransactions(
            Connection connection,
            ArrayList<Account> accounts
    ) throws SQLException {
        String sql = """
                SELECT
                    transaction_id,
                    account_number,
                    transaction_type,
                    amount,
                    timestamp,
                    balance_after,
                    employee_id,
                    employee_name,
                    employee_username,
                    employee_password,
                    employee_role
                FROM transactions
                ORDER BY timestamp
                """;

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Account account = findAccountByNumber(
                        accounts,
                        resultSet.getString("account_number")
                );

                if (account == null) {
                    continue;
                }

                Transaction transaction = new Transaction(
                        resultSet.getString("transaction_id"),
                        TransactionType.valueOf(resultSet.getString("transaction_type")),
                        resultSet.getDouble("amount"),
                        LocalDateTime.parse(resultSet.getString("timestamp")),
                        resultSet.getDouble("balance_after"),
                        createEmployeeFromResultSet(resultSet)
                );

                account.addTransaction(transaction);
            }
        }
    }

    private Employee createEmployeeFromResultSet(ResultSet resultSet) throws SQLException {
        String employeeId = resultSet.getString("employee_id");

        if (employeeId == null) {
            return null;
        }

        return new Employee(
                employeeId,
                resultSet.getString("employee_name"),
                resultSet.getString("employee_username"),
                resultSet.getString("employee_password"),
                resultSet.getString("employee_role")
        );
    }

    private Account findAccountByNumber(
            ArrayList<Account> accounts,
            String accountNumber
    ) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }

        return null;
    }

    private String findCustomerIdForAccount(
            HashMap<String, Customer> customers,
            Account account
    ) {
        for (Customer customer : customers.values()) {
            if (customer.getAccounts().contains(account)) {
                return customer.getId();
            }
        }

        return null;
    }

    private String getAccountType(Account account) {
        if (account instanceof CheckingAccount) {
            return "CHECKING";
        }

        if (account instanceof SavingsAccount) {
            return "SAVINGS";
        }

        return "UNKNOWN";
    }
}
