package com.bank;

import com.bank.exception.AccountNotEmptyException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Employee;
import com.bank.model.SavingsAccount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for account model behavior.
 *
 * These tests check the rules inside Account, SavingsAccount, and
 * CheckingAccount without using the controller, GUI, or database.
 */
class AccountTest {

    /**
     * Helper method used by many tests. It avoids repeating employee creation
     * in every test case.
     */
    private Employee createEmployee() {
        return new Employee(
                "E001",
                "Sara",
                "sara",
                "123",
                "ADMIN"
        );
    }

    @Test
    void deposit_shouldIncreaseBalance() throws BankException {
        // Arrange: create the objects needed for the test.
        Employee employee = createEmployee();

        Account account = new SavingsAccount(
                "ACC001",
                1000.0,
                0.05
        );

        // Act: call the method being tested.
        account.deposit(
                500.0,
                employee
        );

        // Assert: verify the result is what we expected.
        assertEquals(
                1500.0,
                account.getBalance(),
                0.001
        );
    }

    @Test
    void deposit_invalidAmount_shouldThrowException() {
        Employee employee = createEmployee();

        Account account = new SavingsAccount(
                "ACC006",
                1000.0,
                0.05
        );

        /*
         * assertThrows means this operation is expected to fail with
         * InvalidAmountException. If it does not fail, the test fails.
         */
        assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(
                        0.0,
                        employee
                )
        );
    }

    @Test
    void savingsWithdraw_shouldDecreaseBalance() throws BankException {
        Employee employee = createEmployee();

        Account account = new SavingsAccount(
                "ACC002",
                1000.0,
                0.05
        );

        account.withdraw(
                300.0,
                employee
        );

        assertEquals(
                700.0,
                account.getBalance(),
                0.001
        );
    }

    @Test
    void checkingWithdraw_shouldAllowOverdraftWithinLimit() throws BankException {
        Employee employee = createEmployee();

        Account account = new CheckingAccount(
                "ACC003",
                1000.0,
                500.0
        );

        // Checking accounts can go below zero if they stay inside overdraft.
        account.withdraw(
                1200.0,
                employee
        );

        assertEquals(
                -200.0,
                account.getBalance(),
                0.001
        );
    }

    @Test
    void savingsWithdraw_moreThanBalance_shouldThrowException() {
        Employee employee = createEmployee();

        Account account = new SavingsAccount(
                "ACC004",
                1000.0,
                0.05
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(
                        1200.0,
                        employee
                )
        );
    }

    @Test
    void checkingWithdraw_moreThanOverdraftLimit_shouldThrowException() {
        Employee employee = createEmployee();

        Account account = new CheckingAccount(
                "ACC005",
                1000.0,
                500.0
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(
                        1600.0,
                        employee
                )
        );
    }

    @Test
    void closeAccount_nonZeroBalance_shouldThrowException() {
        Account account = new SavingsAccount(
                "ACC007",
                1000.0,
                0.05
        );

        assertThrows(
                AccountNotEmptyException.class,
                account::closeAccount
        );
    }
}
