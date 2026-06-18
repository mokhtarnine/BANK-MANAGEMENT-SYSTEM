package com.bank;

import com.bank.exception.AccountNotEmptyException;
import com.bank.exception.AccountClosedException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.TransactionType;
import com.bank.service.BankService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankServiceTest {

    private BankService bankService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        bankService = new BankService(false);

        employee = new Employee(
                "E001",
                "Sara",
                "sara",
                "1234",
                "ADMIN"
        );
    }

    private Customer createDefaultCustomer() {
        return bankService.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );
    }

    @Test
    void createCustomer_shouldStoreCustomer() {
        Customer customer = createDefaultCustomer();

        assertEquals(1, bankService.getAllCustomers().size());
        assertTrue(bankService.getAllCustomers().contains(customer));
    }

    @Test
    void openCheckingAccount_shouldAddAccountToBankAndCustomer() {
        Customer customer = createDefaultCustomer();

        Account account = bankService.openAccount(
                customer.getId(),
                "CHECKING",
                1000.0
        );

        assertInstanceOf(CheckingAccount.class, account);
        assertEquals(1, bankService.getAllAccounts().size());
        assertTrue(customer.getAccounts().contains(account));
    }

    @Test
    void deposit_shouldIncreaseBalance() throws BankException {
        Customer customer = createDefaultCustomer();

        Account account = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        bankService.deposit(
                account.getAccountNumber(),
                500.0,
                employee
        );

        assertEquals(1500.0, account.getBalance(), 0.001);
        assertEquals(1, account.getTransaction().size());
        assertEquals(TransactionType.DEPOSIT, account.getTransaction().get(0).getType());
        assertEquals(1500.0, account.getTransaction().get(0).getBalanceAfter(), 0.001);
    }

    @Test
    void withdraw_shouldDecreaseBalance() throws BankException {
        Customer customer = createDefaultCustomer();

        Account account = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        bankService.withdraw(
                account.getAccountNumber(),
                300.0,
                employee
        );

        assertEquals(700.0, account.getBalance(), 0.001);
        assertEquals(1, account.getTransaction().size());
        assertEquals(TransactionType.WITHDRAW, account.getTransaction().get(0).getType());
        assertEquals(700.0, account.getTransaction().get(0).getBalanceAfter(), 0.001);
    }

    @Test
    void withdraw_moreThanBalance_shouldThrowException() {
        Customer customer = createDefaultCustomer();

        Account account = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> bankService.withdraw(
                        account.getAccountNumber(),
                        1200.0,
                        employee
                )
        );
    }

    @Test
    void transfer_shouldMoveMoneyBetweenAccounts() throws BankException {
        Customer customer = createDefaultCustomer();

        Account fromAccount = bankService.openAccount(
                customer.getId(),
                "CHECKING",
                1000.0
        );

        Account toAccount = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                200.0
        );

        bankService.transfer(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                300.0,
                employee
        );

        assertEquals(700.0, fromAccount.getBalance(), 0.001);
        assertEquals(500.0, toAccount.getBalance(), 0.001);
        assertEquals(1, fromAccount.getTransaction().size());
        assertEquals(1, toAccount.getTransaction().size());
        assertEquals(TransactionType.TRANSFER_OUT, fromAccount.getTransaction().get(0).getType());
        assertEquals(TransactionType.TRANSFER_IN, toAccount.getTransaction().get(0).getType());
        assertEquals(700.0, fromAccount.getTransaction().get(0).getBalanceAfter(), 0.001);
        assertEquals(500.0, toAccount.getTransaction().get(0).getBalanceAfter(), 0.001);
    }

    @Test
    void transfer_toClosedAccount_shouldNotRemoveMoneyFromSource() throws BankException {
        Customer customer = createDefaultCustomer();

        Account fromAccount = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        Account closedTargetAccount = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                0.0
        );

        bankService.closeAccount(closedTargetAccount.getAccountNumber());

        assertThrows(
                AccountClosedException.class,
                () -> bankService.transfer(
                        fromAccount.getAccountNumber(),
                        closedTargetAccount.getAccountNumber(),
                        300.0,
                        employee
                )
        );

        // A failed transfer must leave both balances and histories unchanged.
        assertEquals(1000.0, fromAccount.getBalance(), 0.001);
        assertEquals(0.0, closedTargetAccount.getBalance(), 0.001);
        assertTrue(fromAccount.getTransaction().isEmpty());
        assertTrue(closedTargetAccount.getTransaction().isEmpty());
    }

    @Test
    void closeAccount_withNonZeroBalance_shouldThrowException() {
        Customer customer = createDefaultCustomer();

        Account account = bankService.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        assertThrows(
                AccountNotEmptyException.class,
                () -> bankService.closeAccount(account.getAccountNumber())
        );
    }
}
