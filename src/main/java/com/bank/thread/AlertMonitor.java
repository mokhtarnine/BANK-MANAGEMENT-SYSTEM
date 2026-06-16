package com.bank.thread;

import com.bank.exception.OverdraftAlertException;
import com.bank.model.Account;
import com.bank.service.BankService;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class AlertMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AlertMonitor.class.getName());

    private final BankService bankService;
    private final double threshold;
    private final Set<String> alertedAccounts;

    private volatile boolean running;

    public AlertMonitor(BankService bankService, double threshold) {
        this.bankService = bankService;
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
                LOGGER.warning(e.getMessage());
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

    public void stop() {
        running = false;
    }
}