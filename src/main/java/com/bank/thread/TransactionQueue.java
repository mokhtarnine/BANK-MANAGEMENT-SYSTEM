package com.bank.thread;

import java.util.LinkedList;
import java.util.Queue;

public class TransactionQueue {

    private final Queue<TransactionRequest> queue;

    public TransactionQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized void enqueue(TransactionRequest request) {
        queue.add(request);
        notifyAll();
    }

    public synchronized TransactionRequest dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
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