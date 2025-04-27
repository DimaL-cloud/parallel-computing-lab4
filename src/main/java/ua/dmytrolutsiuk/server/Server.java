package ua.dmytrolutsiuk.server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

import java.util.concurrent.*;

@Slf4j
public class Server {

    private static final int PORT = 8080;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 50;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final ExecutorService executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(PORT)) {
            log.info("Server started on port {}", PORT);
            while (true) {
                var socket = serverSocket.accept();
                log.info("Client connected: {}", socket.getInetAddress());
                try {
                    executor.submit(new ClientSession(socket));
                } catch (RejectedExecutionException e) {
                    log.warn("Server is overloaded. Rejecting client: {}", socket.getInetAddress());
                    socket.close();
                }
            }
        } catch (IOException e) {
            log.error("Error starting server", e);
        }
    }
}
