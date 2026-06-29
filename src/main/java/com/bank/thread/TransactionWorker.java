package com.bank.thread;

import com.bank.exception.BankException;
import com.bank.model.TransactionType;
import com.bank.service.AuditService;
import com.bank.service.BankService;

/**
 * Background worker responsible for processing queued transaction requests.
 *
 * The worker continuously takes requests from TransactionQueue and executes
 * them using BankService. Each successful or failed operation is recorded
 * using AuditService.
 */
public class TransactionWorker implements Runnable {
    private final TransactionQueue queue;
    private final BankService bankService;
    private final AuditService auditService;

    private volatile boolean running;

    public TransactionWorker(
            TransactionQueue queue,
            BankService bankService,
            AuditService auditService
    ) {
        this.queue = queue;
        this.bankService = bankService;
        this.auditService = auditService;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                /*
                 * dequeue() blocks when the queue is empty. That means this
                 * worker does not waste CPU while waiting for work.
                 */
                TransactionRequest request = queue.dequeue();
                processRequest(request);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop();

            } catch (BankException e) {
                auditService.recordWarning(
                        "Queued transaction failed: " + e.getMessage()
                );
            } catch (RuntimeException e) {
                /*
                 * An unexpected runtime error is logged, but the worker keeps
                 * running so later requests can still be processed.
                 */
                auditService.recordError(
                        "Unexpected queued transaction error",
                        e
                );
            }
        }
    }

    private void processRequest(TransactionRequest request) throws BankException {
        TransactionType type = request.getType();

        /*
         * The request contains the operation type and the required account
         * numbers. The worker translates that request into a BankService call.
         */
        if (type == TransactionType.DEPOSIT) {
            bankService.deposit(
                    request.getFromAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );
            auditService.recordAction(
                    request.getEmployee(),
                    "Queued deposit of " + request.getAmount()
                            + " into " + request.getFromAccount()
            );

        } else if (type == TransactionType.WITHDRAW) {
            bankService.withdraw(
                    request.getFromAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );
            auditService.recordAction(
                    request.getEmployee(),
                    "Queued withdrawal of " + request.getAmount()
                            + " from " + request.getFromAccount()
            );

        } else if (type == TransactionType.TRANSFER_OUT) {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.getEmployee()
            );
            auditService.recordAction(
                    request.getEmployee(),
                    "Queued transfer of " + request.getAmount()
                            + " from " + request.getFromAccount()
                            + " to " + request.getToAccount()
            );

        } else {
            auditService.recordWarning(
                    "Unsupported transaction type: " + type
            );
        }
    }

    public void stop() {
        running = false;
    }
}
