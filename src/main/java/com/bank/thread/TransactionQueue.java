package com.bank.thread;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Shared queue used by the controller and TransactionWorker.
 *
 * The controller adds TransactionRequest objects with enqueue(). The worker
 * waits inside dequeue() until a request is available. synchronized, wait(), and
 * notifyAll() make this communication thread-safe.
 */
public class TransactionQueue {

    private final Queue<TransactionRequest> queue;

    public TransactionQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized void enqueue(TransactionRequest request) {
        queue.add(request);
        /*
         * Wake the worker thread if it is waiting in dequeue(). Without this,
         * the request could stay in the queue while the worker keeps sleeping.
         */
        notifyAll();
    }

    public synchronized TransactionRequest dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            // Release the lock and wait until enqueue() adds a request.
            wait();
        }

        return queue.remove();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
