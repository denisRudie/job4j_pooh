package ru.job4j;

import java.util.concurrent.*;

/**
 * Pool of threads, which working with tasks, which sent from {@link Handler}.
 * {@link ThreadPool#executor} for creating threads and allocating tasks between threads.
 * {@link ThreadPool#queue} queue for storing items.
 * {@link ThreadPool#connections} store for connections to {@link EchoServer}.
 */
public class ThreadPool {
    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<Item> queue;
    private final ConcurrentHashMap<ConnectionIO, String> connections;

    public ThreadPool(int nOThreads) {
        this.executor = Executors.newFixedThreadPool(nOThreads);
        this.queue = new ConcurrentLinkedQueue<>();
        this.connections = new ConcurrentHashMap<>();
    }

    /**
     * Return to client the first element of {@link ThreadPool#queue}.
     *
     * @param conn {@link ConnectionIO} object, which created while client connected to
     * {@link EchoServer}.
     */
    protected void getQueue(ConnectionIO conn) {
        Future<Item> future = executor.submit(queue::poll);

        try {
            if (future.get() != null) {
                conn.writeLine(future.get().getName());
            } else {
                conn.writeLine("queue is empty");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates object by request and add it to {@link ThreadPool#queue}.
     *
     * @param text request.
     */
    protected void postQueue(String text) {
        executor.execute(() -> queue.offer(new Item(text)));
    }

    /**
     * Executes {@link Runnable} tasks.
     *
     * @param r Runnable object.
     */
    protected void execute(Runnable r) {
        executor.execute(r);
    }

    /**
     * Return to client the last element from his local topic.
     * If topic is empty return message.
     *
     * @param conn {@link ConnectionIO} object, which created while client connected to
     * {@link EchoServer}.
     */
    protected void getTopic(ConnectionIO conn) {
        Future<Item> future = executor.submit(conn::poll);
        try {
            if (future.get() != null) {
                conn.writeLine(future.get().getName());
            } else {
                conn.writeLine("topic is empty");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param text create object by request and add to each of {@link ThreadPool#connections}.
     */
    protected void postTopic(String text) {
        executor.submit(() -> {
            for (ConnectionIO conn : connections.keySet()) {
                conn.offer(new Item(text));
            }
        });
    }

    /**
     * Add connection to {@link ThreadPool#connections}.
     */
    protected void addConn(ConnectionIO conn) {
        connections.put(conn, "");
    }

    /**
     * Removes connection from {@link ThreadPool#connections}.
     *
     * @param conn ConnectionIO object, which created while client connected to server.
     */
    protected void removeConn(ConnectionIO conn) {
        connections.remove(conn);
    }

    /**
     * Clear connections list and shutdowns {@link ThreadPool#executor}.
     */
    protected void shutdown() {
        connections.clear();
        executor.shutdownNow();
    }
}
