package com.bank.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.exception.AccountClosedException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.repository.DatabaseManager;
import com.bank.repository.JdbcBankRepository;

/**
 * Service layer of the application.
 *
 * This class contains the main banking rules. The GUI and controller ask this
 * class to create customers, open accounts, deposit, withdraw, transfer, and
 * search data. The service updates the model objects first, then asks the
 * repository to save the new state when persistence is enabled.
 */
public class BankService {

    private final ArrayList<Account> accounts;
    private final HashMap<String, Customer> customers;
    private final ReentrantLock transferLock;
    private final BankRepository repository;

    public BankService() {
        this(true);
    }

    public BankService(boolean persistenceEnabled) {
        this.accounts = new ArrayList<>();
        this.customers = new HashMap<>();
        this.transferLock = new ReentrantLock();
        this.repository = persistenceEnabled
                ? new JdbcBankRepository(new DatabaseManager())
                : null;

        loadDataSafely();
    }

    /**
     * Creates a customer and stores it in the customer map using the customer id
     * as the key. After the customer is added, the full bank state is saved.
     */
    public Customer createCustomer(String id,String fullName,String username,String password,String email) {
        Customer customer = new Customer( id,fullName,username,password,email);
        customers.put(id, customer);
        saveDataSafely();
        return customer;
    }

    /**
     * Returns all customers currently loaded in memory.
     */
    public Collection<Customer> getAllCustomers(){
        return customers.values();
    }

    /**
     * Generates the next account number by reading the biggest existing ACC
     * number, then adding 1.
     *
     * This avoids duplicate numbers after loading old data from SQLite. For
     * example, if the database contains ACC2 only, accounts.size() would create
     * ACC2 again, but this method correctly creates ACC3.
     */
    private String generateNextAccount(){
        int maxNumber = 0;

        for (Account account: accounts){
            String number = account.getAccountNumber();

            if(number.startsWith("ACC")){
                try{
                    int value = Integer.parseInt(number.substring(3));
                    if(value > maxNumber ){
                        maxNumber = value;
                    }
                } catch (NumberFormatException e){
                    // ingonre account number that do not follow ACC format
                }
            }
        }
        return "ACC" + (maxNumber + 1);
    }
    /**
     * Opens a checking or savings account for an existing customer.
     *
     * The account is stored in two places:
     * - the global accounts list, so the bank can search all accounts
     * - the customer's own account list, so the GUI can show that customer's
     *   accounts
     */
    public Account openAccount(String customerId,String type,double initialBalance) {
        Customer customer = customers.get(customerId);

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        if (initialBalance < 0) {
            throw new InvalidAmountException(initialBalance);
        }

        String accountNumber = generateNextAccount();

        Account account;

        if (type.equalsIgnoreCase("CHECKING")) {
            account = new CheckingAccount(accountNumber,initialBalance,500.0);
        } else if (type.equalsIgnoreCase("SAVINGS")) {
            account = new SavingsAccount(accountNumber,initialBalance,0.05);
        } else {
            throw new IllegalArgumentException("Invalid account type: " + type);
        }

        accounts.add(account);
        customer.addAccount(account);

        saveDataSafely();

        return account;
    }
    /**
     * Searches the loaded accounts list and returns the account with the given
     * account number. Returns null when no account matches.
     */
    public Account findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }

        return null;
    }
    /**
     * Deposits money into an account, records a DEPOSIT transaction, then saves
     * the updated bank state.
     */
    public void deposit(String accountNumber,double amount,Employee employee) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        account.deposit(amount, employee);
        account.addTransaction(createTransaction(
                TransactionType.DEPOSIT,
                amount,
                account,
                employee
        ));
        saveDataSafely();
    }

    /**
     * Withdraws money from an account, records a WITHDRAW transaction, then
     * saves the updated bank state.
     */
    public void withdraw(String accountNumber,double amount,Employee employee) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.withdraw(amount, employee);
        account.addTransaction(createTransaction(
                TransactionType.WITHDRAW,
                amount,
                account,
                employee
        ));
        saveDataSafely();
    }
    /**
     * Transfers money between two accounts as one atomic operation.
     *
     * Both accounts remain locked during validation, balance changes, and
     * transaction-history creation. This prevents another thread from changing
     * either account in the middle of the transfer.
     */
    public void transfer(
            String fromAccountNumber,
            String toAccountNumber,
            double amount,
            Employee employee
    ) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and target accounts must be different."
            );
        }

        Account fromAccount = findAccount(fromAccountNumber);
        Account toAccount = findAccount(toAccountNumber);

        if (fromAccount == null) {
            throw new IllegalArgumentException("Source account not found: " + fromAccountNumber);
        }

        if (toAccount == null) {
            throw new IllegalArgumentException("Target account not found: " + toAccountNumber);
        }

        /*
         * transferLock allows only one transfer to acquire account locks at a
         * time. Deposits and withdrawals are still protected by each account's
         * own lock.
         */
        transferLock.lock();
        fromAccount.getLock().lock();
        toAccount.getLock().lock();

        try {
            /*
             * Validate the target before withdrawing from the source. Because
             * the target is locked, it cannot become closed after this check.
             */
            if (toAccount.isClosed()) {
                throw new AccountClosedException(
                        toAccount.getAccountNumber()
                );
            }

            fromAccount.withdraw(amount, employee);
            toAccount.deposit(amount, employee);

            fromAccount.addTransaction(createTransaction(
                    TransactionType.TRANSFER_OUT,
                    amount,
                    fromAccount,
                    employee
            ));
            toAccount.addTransaction(createTransaction(
                    TransactionType.TRANSFER_IN,
                    amount,
                    toAccount,
                    employee
            ));
            saveDataSafely();
        } finally {
            // Unlock in reverse order so every lock is released after success or failure.
            toAccount.getLock().unlock();
            fromAccount.getLock().unlock();
            transferLock.unlock();
        }
    }
    /**
     * Searches customers by full name, id, or email. The GUI uses this for the
     * customer search box.
     */
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> result = new ArrayList<>();
            //If customer name contains keyword
            //OR customer id contains keyword
            //OR customer email contains keyword
            //then add this customer to the search result.

        for (Customer customer : customers.values()) {
            if (customer.getFullName().toLowerCase().contains(keyword.toLowerCase())
                    || customer.getId().toLowerCase().contains(keyword.toLowerCase())
                    || customer.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(customer);
            }
        }

        return result;
    }
    /**
     * Returns the loaded accounts list. The controller uses this for dashboard
     * totals and account table data.
     */
    public List<Account> getAllAccounts() {
        return accounts;
    }

    /**
     * Closes an account only when the account exists and has zero balance.
     */
    public void closeAccount(String accountNumber) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        account.closeAccount();
        saveDataSafely();
    }
    /**
     * Returns only accounts that match the requested account type.
     */
    public List<Account> filterAccountsByType(String type) {
        List<Account> result = new ArrayList<>();

        for (Account account : accounts) {
            if (type.equalsIgnoreCase("CHECKING") && account instanceof CheckingAccount) {
                result.add(account);
            } else if (type.equalsIgnoreCase("SAVINGS") && account instanceof SavingsAccount) {
                result.add(account);
            }
        }

        return result;
    }
    /**
     * Returns the transaction history for one account.
     */
    public List<Transaction> getTransactionHistory(String accountNumber) {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        return account.getTransaction();
    }

    /**
     * Builds a transaction record after a successful operation. The balanceAfter
     * value is captured from the account after the balance has changed.
     */
    private Transaction createTransaction(
            TransactionType type,
            double amount,
            Account account,
            Employee employee
    ) {
        return new Transaction(
                UUID.randomUUID().toString(),
                type,
                amount,
                LocalDateTime.now(),
                account.getBalance(),
                employee
        );
    }

    /**
     * Loads saved customers, accounts, and transactions from the repository when
     * persistence is enabled.
     */
    private void loadDataSafely() {
        if (repository == null) {
            return;
        }

        try {
            repository.loadAll(customers, accounts);
        } catch (BankException e) {
            throw new IllegalStateException(
                    "Failed to load bank data",
                    e
            );
        }
    }

    /**
     * Saves all current data through the repository. If the repository reports a
     * problem, the service converts it to an IllegalStateException so the
     * controller and GUI can show a clear failure message.
     */
    private void saveDataSafely() {
        if (repository == null) {
            return;
        }

        try {
            repository.saveAll(customers, accounts);
        } catch (BankException e) {
            throw new IllegalStateException(
                    "Failed to save bank data",
                    e
            );
        }
    }

}
