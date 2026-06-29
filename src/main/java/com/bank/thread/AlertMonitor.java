package com.bank.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.bank.exception.OverdraftAlertException;
import com.bank.model.Account;
import com.bank.service.AuditService;
import com.bank.service.BankService;

/**
 * Background monitoring thread responsible for detecting low-balance alerts.
 *
 * The monitor runs independently from the GUI and checks all accounts every few
 * seconds. When an account balance goes below the threshold, it logs a warning
 * and optionally sends the message to the GUI through alertHandler.
 */
public class AlertMonitor implements Runnable {
    private final BankService bankService;
    private final AuditService auditService;
    private final double threshold;
    private final Set<String> alertedAccounts;
    private Consumer<String> alertHandler;

    private volatile boolean running;

    public AlertMonitor(
            BankService bankService,
            AuditService auditService,
            double threshold
    ) {
        this.bankService = bankService;
        this.auditService = auditService;
        this.threshold = threshold;
        this.alertedAccounts = new HashSet<>();
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            checkAccounts();

            try {
                // Wait before checking again so the monitor does not run nonstop.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop();
            }
        }
    }

    private void checkAccounts() {
        for (Account account : bankService.getAllAccounts()) {
            try {
                checkAccount(account);
            } catch (OverdraftAlertException e) {
                auditService.recordWarning(e.getMessage());

                if (alertHandler != null) {
                    alertHandler.accept(e.getMessage());
                }
            }
        }
    }

    private void checkAccount(Account account) throws OverdraftAlertException {
        String accountNumber = account.getAccountNumber();

        if (account.getBalance() < threshold) {
            if (!alertedAccounts.contains(accountNumber)) {
                /*
                 * Remember this account after the first alert. This prevents
                 * the GUI from receiving the same alert every 3 seconds.
                 */
                alertedAccounts.add(accountNumber);

                throw new OverdraftAlertException(
                        accountNumber,
                        account.getBalance()
                );
            }
        } else {
            // If the balance becomes healthy again, allow a future alert.
            alertedAccounts.remove(accountNumber);
        }
    }
    public void setAlertHandler(Consumer<String> alertHandler) {
        this.alertHandler = alertHandler;
    }

    public void stop() {
        running = false;
    }
}
