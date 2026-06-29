package com.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank.controller.BankController;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Employee;

/**
 * Tests for BankController.
 *
 * These tests verify that the controller correctly delegates work to
 * BankService, manages login state, prepares data for the GUI, and can process
 * queued transactions through background threads.
 */
class BankControllerTest {

    private BankController controller;

    @BeforeEach
    void setUp() {
        /*
         * false disables real database persistence. This keeps controller tests
         * isolated from bank.db and makes them repeatable.
         */
        controller = new BankController(false);
    }

    @AfterEach
    void tearDown() {
        /*
         * BankController starts worker threads. @AfterEach stops them after each
         * test so the test process can finish cleanly.
         */
        controller.shutdownThreads();
    }

    @Test
    void login_validCredentials_shouldReturnTrueAndSetCurrentEmployee() {
        boolean result = controller.login(
                "admin",
                "admin123"
        );

        // Successful login should store the employee inside the controller.
        assertTrue(result);

        Employee employee = controller.getCurrentEmployee();

        assertNotNull(employee);
        assertEquals("Admin User", employee.getFullName());
        assertEquals("ADMIN", employee.getRole());
    }

    @Test
    void login_invalidCredentials_shouldReturnFalse() {
        boolean result = controller.login(
                "admin",
                "wrong-password"
        );

        assertFalse(result);
        assertNull(controller.getCurrentEmployee());
    }

    @Test
    void createCustomer_shouldDelegateToBankService() {
        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        /*
         * The controller should return the created customer and expose it through
         * getAllCustomers(), proving it delegated to BankService.
         */
        assertNotNull(customer);
        assertEquals("C001", customer.getId());
        assertEquals("Ahmed", customer.getFullName());
        assertEquals(1, controller.getAllCustomers().size());
    }

    @Test
    void openAccount_shouldDelegateToBankService() {
        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        assertNotNull(account);
        assertEquals(1000.0, account.getBalance(), 0.001);
        assertEquals(1, controller.getAllAccounts().size());
    }

    @Test
    void deposit_afterLogin_shouldIncreaseBalance() throws BankException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        controller.deposit(
                account.getAccountNumber(),
                500.0
        );

        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    void withdraw_afterLogin_shouldDecreaseBalance() throws BankException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        controller.withdraw(
                account.getAccountNumber(),
                300.0
        );

        assertEquals(700.0, account.getBalance(), 0.001);
    }

    @Test
    void withdraw_moreThanBalance_shouldThrowException() {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> controller.withdraw(
                        account.getAccountNumber(),
                        1200.0
                )
        );
    }

    @Test
    void transfer_afterLogin_shouldMoveMoneyBetweenAccounts() throws BankException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account fromAccount = controller.openAccount(
                customer.getId(),
                "CHECKING",
                1000.0
        );

        Account toAccount = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                200.0
        );

        controller.transfer(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                300.0
        );

        assertEquals(700.0, fromAccount.getBalance(), 0.001);
        assertEquals(500.0, toAccount.getBalance(), 0.001);
    }

    @Test
    void requestDeposit_shouldProcessDepositInBackground() throws InterruptedException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        controller.requestDeposit(
                account.getAccountNumber(),
                500.0
        );

        /*
         * requestDeposit uses TransactionQueue and TransactionWorker. The worker
         * runs on another thread, so the test waits until the balance changes.
         */
        waitForBalance(account, 1500.0);

        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    void requestWithdraw_shouldProcessWithdrawInBackground() throws InterruptedException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account account = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                1000.0
        );

        controller.requestWithdraw(
                account.getAccountNumber(),
                300.0
        );

        waitForBalance(account, 700.0);

        assertEquals(700.0, account.getBalance(), 0.001);
    }

    @Test
    void requestTransfer_shouldProcessTransferInBackground() throws InterruptedException {
        controller.login(
                "admin",
                "admin123"
        );

        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account fromAccount = controller.openAccount(
                customer.getId(),
                "CHECKING",
                1000.0
        );

        Account toAccount = controller.openAccount(
                customer.getId(),
                "SAVINGS",
                200.0
        );

        controller.requestTransfer(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                300.0
        );

        waitForBalance(fromAccount, 700.0);

        assertEquals(700.0, fromAccount.getBalance(), 0.001);
        assertEquals(500.0, toAccount.getBalance(), 0.001);
    }

    @Test
    void getCustomerAccountTableData_shouldFilterByNumberAndType() {
        Customer customer = controller.createCustomer(
                "C001",
                "Ahmed",
                "ahmed",
                "1234",
                "ahmed@gmail.com"
        );

        Account checkingAccount = controller.openAccount(
                customer.getId(),
                "CHECKING",
                1000.0
        );

        controller.openAccount(
                customer.getId(),
                "SAVINGS",
                200.0
        );

        /*
         * This method returns Object[][] because Swing tables can display this
         * simple row/column format without knowing the Account class.
         */
        Object[][] rows = controller.getCustomerAccountTableData(
                customer.getId(),
                checkingAccount.getAccountNumber(),
                "CHECKING"
        );

        assertEquals(1, rows.length);
        assertEquals(checkingAccount.getAccountNumber(), rows[0][0]);
        assertEquals("CHECKING", rows[0][1]);
    }

    private void waitForBalance(Account account, double expectedBalance) throws InterruptedException {
        /*
         * Small polling helper for asynchronous tests. It checks the balance a
         * few times because the worker thread may not process the request
         * immediately.
         */
        int attempts = 0;

        while (attempts < 10
                && Math.abs(account.getBalance() - expectedBalance) > 0.001) {
            Thread.sleep(100);
            attempts++;
        }
    }
}
