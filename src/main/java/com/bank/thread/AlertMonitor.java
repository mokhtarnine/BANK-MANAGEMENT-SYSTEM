package com.bank.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.bank.exception.OverdraftAlertException;
import com.bank.model.Account;
import com.bank.service.AuditService;
import com.bank.service.BankService;

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
                alertedAccounts.add(accountNumber);

                throw new OverdraftAlertException(
                        accountNumber,
                        account.getBalance()
                );
            }
        } else {
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
