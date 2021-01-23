package ru.job4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DataQueue {

    private final ConcurrentLinkedQueue<Item> queue = new ConcurrentLinkedQueue<>();
    private final ThreadPool pool;

    public DataQueue(ThreadPool pool) {
        this.pool = pool;
    }

    protected void getQueue(ConnectionIO conn) {
        Future<Item> future =
                pool.submit(queue::poll);

        try {
            if (future.get() != null) {
                conn.writeLine(future.get().getName());
            } else {
                conn.writeLine("queue is empty");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void postQueue(String text) {
        pool.execute(() -> queue.offer(new Item(text)));
    }

}
