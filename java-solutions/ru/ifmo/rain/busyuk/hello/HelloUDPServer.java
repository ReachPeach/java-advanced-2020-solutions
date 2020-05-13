package ru.ifmo.rain.busyuk.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class HelloUDPServer implements info.kgeorgiy.java.advanced.hello.HelloServer {
    private DatagramSocket socket;
    private int requestBufferSize;
    private ExecutorService listener;
    private ExecutorService requestsSender;
    private boolean closed = true;


    private DatagramPacket makePacket(final byte[] buffer, final SocketAddress to) {
        return new DatagramPacket(buffer, 0, buffer.length, to);
    }

    private DatagramPacket makePacket(int bufferSize) {
        final var buffer = new byte[bufferSize];
        return new DatagramPacket(buffer, bufferSize);
    }

    private String parsePacket(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    private void sendResponse(DatagramPacket packet) {
        final var requestMessage = parsePacket(packet);
        final var responseMessage = "Hello, " + requestMessage;
        final var responsePacket = makePacket(responseMessage.getBytes(StandardCharsets.UTF_8), packet.getSocketAddress());

        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            System.out.println(String.format("ERROR. Can't send packet to %s%nLog:%s", packet.getSocketAddress(), e.getMessage()));
        }
    }

    private void listen() {
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
            final var packet = makePacket(requestBufferSize);
            try {
                socket.receive(packet);
                requestsSender.submit(() -> sendResponse(packet));
            } catch (IOException e) {
                if (!closed) {
                    System.out.println("Failed to receive a packet: " + e.getMessage());
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
            System.out.println(" " + port);
            return;
        }

        listener = Executors.newSingleThreadExecutor();
        listener.submit(this::listen);
        requestsSender = Executors.newFixedThreadPool(threads);
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
        listener.shutdownNow();
        requestsSender.shutdownNow();
        try {
            requestsSender.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        socket.close();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid arguments!");
            return;
        }

        int port;
        int threads;

        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Format of arguments is incorrect " + e.getMessage());
            return;
        }

        new HelloUDPServer().start(port, threads);
    }
}
