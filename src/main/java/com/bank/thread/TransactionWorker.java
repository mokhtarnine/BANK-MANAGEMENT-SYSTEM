package com.bank.thread;

import com.bank.exception.BankException;
import com.bank.model.TransactionType;
import com.bank.service.BankService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionWorker implements Runnable {

    private static final Logger LOGGER =  Logger.getLogger(TransactionWorker.class.getName());

    private final TransactionQueue queue;
    private final BankService bankService;

    private volatile boolean running;

    public TransactionWorker(
            TransactionQueue queue,
            BankService bankService
    ) {
        this.queue = queue;
        this.bankService = bankService;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                TransactionRequest request = queue.dequeue();
                processRequest(request);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop();

            } catch (BankException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Transaction failed",
                        e
                );
            }
        }
    }

    private void processRequest(TransactionRequest request) throws BankException {
        TransactionType type = request.getType();

        if (type == TransactionType.DEPOSIT) {
            bankService.deposit(
                    request.getFromAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );

        } else if (type == TransactionType.WITHDRAW) {
            bankService.withdraw(
                    request.getFromAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );

        } else if (type == TransactionType.TRANSFER_OUT) {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );

        } else {
            LOGGER.warning("Unsupported transaction type: " + type);
        }
    }

    public void stop() {
        running = false;
    }
}