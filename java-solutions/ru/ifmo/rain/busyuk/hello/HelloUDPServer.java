package ru.ifmo.rain.busyuk.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static ru.ifmo.rain.busyuk.hello.HelloUDPUtils.*;

public class HelloUDPServer implements info.kgeorgiy.java.advanced.hello.HelloServer {
    private DatagramSocket socket;
    private int requestBufferSize;
    private ExecutorService requestsReceiver;
    private ExecutorService responsesSender;
    private boolean closed;

    private void sendResponse(DatagramPacket packet) {
        final String requestMessage = parsePacket(packet);
        final String responseMessage = "Hello, " + requestMessage;
        final DatagramPacket responsePacket = makePacket(responseMessage.getBytes(StandardCharsets.UTF_8),
                packet.getSocketAddress());

        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            System.err.println(String.format("Can't send packet to %s%nLog: %s", packet.getSocketAddress(),
                    e.getMessage()));
        }
    }

    private void receiveRequests() {
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
            final DatagramPacket packet = makePacket(requestBufferSize);
            try {
                socket.receive(packet);
                responsesSender.submit(() -> sendResponse(packet));
            } catch (IOException e) {
                if (!closed) {
                    System.out.println("Failed receiving packet: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            requestBufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            throw new IllegalArgumentException("Problems with port: " + port + e.getMessage(), e);
        }

        requestsReceiver = Executors.newSingleThreadExecutor();
        requestsReceiver.submit(this::receiveRequests);
        responsesSender = Executors.newFixedThreadPool(threads);
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
        requestsReceiver.shutdownNow();
        responsesSender.shutdownNow();
        try {
            responsesSender.awaitTermination(AWAIT_TERMINATION_COEFFICIENT, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        socket.close();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments provided");
            return;
        }

        int port, threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Format of arguments is incorrect " + e.getMessage());
            return;
        }
        try (HelloUDPServer helloUDPServer = new HelloUDPServer()) {
            helloUDPServer.start(port, threads);
        }
    }
}
