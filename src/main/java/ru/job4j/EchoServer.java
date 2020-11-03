package ru.job4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    private final ServerSocket server;
    private final ThreadPool pool;

    public EchoServer(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.pool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void start() {
        while (!server.isClosed()) {
            try {
                Socket clientSocket = server.accept();
                pool.execute(() -> new Handler(server, clientSocket, pool)
                        .run());
            } catch (IOException ignore) { }
        }
    }

    public static void main(String[] args) {
        EchoServer server;
        try {
            server = new EchoServer(9000);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}