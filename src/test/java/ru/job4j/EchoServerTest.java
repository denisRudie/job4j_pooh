package ru.job4j;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EchoServerTest {

    private void shutdownServer() {
        try (ConnectionIO connection = new ConnectionIO("127.0.0.1", 9000)) {
            connection.writeLine("-1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Concurrent adding and getting values from queue.
     * Checking sending data equals to receiving data.
     */
    @Test
    public void whenAddItemsToQueueThenGet() throws IOException, InterruptedException {
        EchoServer echoServer = new EchoServer(9000);

        Thread starter = new Thread(echoServer::start);
        starter.start();

        Thread.sleep(100);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        ConcurrentHashMap<String, String> in = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> out = new ConcurrentHashMap<>();

        for (int i = 0; i < 300; i++) {
            executor.execute(() -> {
                try (ConnectionIO connection = new ConnectionIO("127.0.0.1", 9000)) {
                    String random = String.valueOf(new Random().nextInt());
                    connection.writeLine("POST /queue/" + random);
                    in.put(random, "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(500);

        for (int i = 0; i < 300; i++) {
            executor.execute(() -> {
                try (ConnectionIO connection = new ConnectionIO("127.0.0.1", 9000)) {
                    connection.writeLine("GET /queue");
                    out.put(connection.readLine(), "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(500);
        shutdownServer();
        assertEquals(in.keySet(), out.keySet());
    }

    /**
     * Consumers connected and automatically subscribe to topic. Now they will see all data added
     * to topic after their connection.
     * Producers add data to topic concurrently.
     * Consumers sending requests for getting data.
     * Checking consumers data equals to adding data by producers.
     */
    @Test
    public void whenAddAndReadFromTopic() throws IOException, InterruptedException {
        EchoServer echoServer = new EchoServer(9000);

        Thread starter = new Thread(echoServer::start);
        starter.start();

        Thread.sleep(100);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        ConcurrentHashMap<String, String> producedItems = new ConcurrentHashMap<>();


//        consumers connected
        ConnectionIO c1 = new ConnectionIO("127.0.0.1", 9000);
        ConnectionIO c2 = new ConnectionIO("127.0.0.1", 9000);
//        stores for reading by consumers data from topic
        Set<String> consumer1Data = new HashSet<>();
        Set<String> consumer2Data = new HashSet<>();

//        adding data by producers
        for (int i = 0; i < 30; i++) {
            executor.execute(() -> {
                try (ConnectionIO connection = new ConnectionIO("127.0.0.1", 9000)) {
                    String random = String.valueOf(new Random().nextInt());
                    connection.writeLine("POST /topic/" + random);
                    producedItems.put(random, "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(500);

//        reading data by consumer1
        executor.execute(() -> {
            for (int i = 0; i < 30; i++) {
                try {
                    c1.writeLine("GET /topic");
                    consumer1Data.add(c1.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        reading data by consumer2
        executor.execute(() -> {
            for (int i = 0; i < 30; i++) {
                try {
                    c2.writeLine("GET /topic");
                    consumer2Data.add(c2.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.sleep(500);
        shutdownServer();
        Thread.sleep(100);

        assertEquals(consumer1Data, consumer2Data);
        assertEquals(consumer1Data, producedItems.keySet());
    }

    @Test
    public void test() throws IOException {
        EchoServer server = mock(EchoServer.class);
        ThreadPool pool = mock(ThreadPool.class);

        Socket client = mock(Socket.class);

        ByteArrayInputStream in = new ByteArrayInputStream("POST /queue/hello".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(client.getInputStream()).thenReturn(in);
        when(client.getOutputStream()).thenReturn(out);

        server.start();


        ConnectionIO conn = mock(ConnectionIO.class);
        when(conn.readLine()).thenReturn("Hello");
        System.out.println(conn.readLine());
    }
}