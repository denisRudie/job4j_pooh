package ru.job4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses requests from client and create tasks for {@link ThreadPool}.
 */
public class Handler {

    private static final Pattern postQueue = Pattern.compile("^(POST /queue/)(.+)");
    private static final Pattern postTopic = Pattern.compile("^(POST /topic/)(.+)");
    private final ServerSocket server;
    private final Socket client;
    private final ThreadPool pool;

    public Handler(ServerSocket server, Socket client, ThreadPool pool) {
        this.server = server;
        this.client = client;
        this.pool = pool;
    }

    /**
     *Creates {@link ConnectionIO} object for parse requests and transfer them to executing to
     *  {@link Handler#pool}.
     *  Request "-1" - closes server.
     *  Request "exit" - closes current connection.
     */
    public void run() {
        try (ConnectionIO conn = new ConnectionIO(client)) {
            pool.addConn(conn);
            String request;

            while (!client.isClosed()
                    && (request = conn.readLine()) != null
                    && !request.equals("exit")) {

                Matcher m1 = postQueue.matcher(request);
                Matcher m2 = postTopic.matcher(request);

                if (m1.matches()) {
                    pool.postQueue(m1.group(2));
                } else if (request.equals("GET /queue")) {
                    pool.getQueue(conn);
                } else if (m2.matches()) {
                    pool.postTopic(m2.group(2));
                } else if (request.equals("GET /topic")) {
                    pool.getTopic(conn);
                } else if (request.equals("-1")) {
                    client.close();
                    pool.shutdown();
                    server.close();
                }
            }
            pool.removeConn(conn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
