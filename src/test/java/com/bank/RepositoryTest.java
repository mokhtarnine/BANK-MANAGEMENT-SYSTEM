package com.bank;

import com.bank.exception.BankException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SQLite repository persistence.
 *
 * This test saves customers, accounts, and transactions into a temporary SQLite
 * database, then loads them again to prove the repository preserves the full
 * object state.
 */
class RepositoryTest {

    @TempDir
    private Path tempDir;

    @Test
    void saveAllAndLoadAll_shouldPreserveCustomersAccountsAndTransactions() throws BankException {
        // Arrange: create a repository connected to a temporary test database.
        BankRepository repository = createRepository();

        Customer customer = new Customer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        CheckingAccount checkingAccount = new CheckingAccount(
                "ACC100",
                1000.0,
                500.0
        );

        SavingsAccount savingsAccount = new SavingsAccount(
                "ACC200",
                200.0,
                0.05
        );
        savingsAccount.setClosed(true);

        Employee employee = new Employee(
                "E001",
                "Sara",
                "sara",
                "1234",
                "ADMIN"
        );

        checkingAccount.addTransaction(new Transaction(
                "T001",
                TransactionType.DEPOSIT,
                300.0,
                LocalDateTime.of(2026, 6, 17, 10, 0),
                1000.0,
                employee
        ));

        savingsAccount.addTransaction(new Transaction(
                "T002",
                TransactionType.TRANSFER_IN,
                100.0,
                LocalDateTime.of(2026, 6, 17, 10, 5),
                200.0,
                employee
        ));

        customer.addAccount(checkingAccount);
        customer.addAccount(savingsAccount);

        HashMap<String, Customer> customers = new HashMap<>();
        customers.put(customer.getId(), customer);

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(checkingAccount);
        accounts.add(savingsAccount);

        // Act 1: save the prepared in-memory objects to SQLite.
        repository.saveAll(customers, accounts);

        HashMap<String, Customer> loadedCustomers = new HashMap<>();
        ArrayList<Account> loadedAccounts = new ArrayList<>();

        // Act 2: load the data into new empty collections.
        repository.loadAll(loadedCustomers, loadedAccounts);

        // Assert: verify customers and account ownership were restored.
        assertEquals(1, loadedCustomers.size());
        assertEquals(2, loadedAccounts.size());

        Customer loadedCustomer = loadedCustomers.get("C001");
        assertNotNull(loadedCustomer);
        assertEquals("Ahmed", loadedCustomer.getFullName());
        assertEquals("ahmed", loadedCustomer.getUsername());
        assertEquals("ahmed@gmail.com", loadedCustomer.getEmail());
        assertEquals(2, loadedCustomer.getAccounts().size());

        // Assert: verify account type-specific fields were preserved.
        Account loadedChecking = findAccount(loadedAccounts, "ACC100");
        assertInstanceOf(CheckingAccount.class, loadedChecking);
        assertEquals(1000.0, loadedChecking.getBalance(), 0.001);
        assertFalse(loadedChecking.isClosed());
        assertEquals(
                500.0,
                ((CheckingAccount) loadedChecking).getOverdraftLimit(),
                0.001
        );

        Account loadedSavings = findAccount(loadedAccounts, "ACC200");
        assertInstanceOf(SavingsAccount.class, loadedSavings);
        assertEquals(200.0, loadedSavings.getBalance(), 0.001);
        assertTrue(loadedSavings.isClosed());
        assertEquals(
                0.05,
                ((SavingsAccount) loadedSavings).getInterestRate(),
                0.001
        );

        // Assert: verify transaction history was saved and loaded correctly.
        assertEquals(1, loadedChecking.getTransaction().size());
        Transaction loadedDeposit = loadedChecking.getTransaction().get(0);
        assertEquals("T001", loadedDeposit.getTransactionId());
        assertEquals(TransactionType.DEPOSIT, loadedDeposit.getType());
        assertEquals(300.0, loadedDeposit.getAmount(), 0.001);
        assertEquals(1000.0, loadedDeposit.getBalanceAfter(), 0.001);
        assertNotNull(loadedDeposit.getPerformedBy());
        assertEquals("Sara", loadedDeposit.getPerformedBy().getFullName());

        assertEquals(1, loadedSavings.getTransaction().size());
        Transaction loadedTransfer = loadedSavings.getTransaction().get(0);
        assertEquals("T002", loadedTransfer.getTransactionId());
        assertEquals(TransactionType.TRANSFER_IN, loadedTransfer.getType());
        assertEquals(100.0, loadedTransfer.getAmount(), 0.001);
        assertEquals(200.0, loadedTransfer.getBalanceAfter(), 0.001);
    }

    private BankRepository createRepository() {
        /*
         * @TempDir creates a clean folder for this test. The database is placed
         * there so tests never write to the real application bank.db.
         */
        Path databasePath = tempDir.resolve("repository-test.db").toAbsolutePath();
        String databaseUrl = "jdbc:sqlite:" + databasePath.toString().replace("\\", "/");

        return new JdbcBankRepository(new DatabaseManager(databaseUrl));
    }

    private Account findAccount(ArrayList<Account> accounts, String accountNumber) {
        // Helper used by assertions to find a loaded account by number.
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }

        return null;
    }
}
