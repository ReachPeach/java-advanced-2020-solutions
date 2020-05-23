package ru.ifmo.rain.busyuk.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.rain.busyuk.hello.HelloUDPUtils.*;

public class HelloUDPClient implements info.kgeorgiy.java.advanced.hello.HelloClient {
    private final static int TIMEOUT_DELAY = 100;

    private void send(final SocketAddress socketAddress, final String prefix, final int requests, final int threadIndex) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_DELAY);
            for (int requestIndex = 0; requestIndex < requests; requestIndex++) {
                final String message = String.format("%s%d_%d", prefix, threadIndex, requestIndex);
                final DatagramPacket request = makePacket(message.getBytes(StandardCharsets.UTF_8), socketAddress);
                final DatagramPacket response = makePacket(socket.getReceiveBufferSize());
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        socket.send(request);
                        System.out.println(String.format("Send to %s: %s%n", socketAddress.toString(), message));
                        socket.receive(response);
                        final String responseMessage = parsePacket(response);
                        if (responseMatches(requestIndex, threadIndex, responseMessage)) {
                            System.out.println(String.format("Text of response: %s%n", responseMessage));
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println(String.format("Failed sending in thread %d: %s", threadIndex, e.getMessage()));
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Failed creating the socket: " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed taking host by name: '" + host + "'", e);
        }

        final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        final ExecutorService senders = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int finalIndex = i;
            senders.execute(() -> send(inetSocketAddress, prefix, requests, finalIndex));
        }
        senders.shutdown();
        try {
            senders.awaitTermination(threads * requests * AWAIT_TERMINATION_COEFFICIENT, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Incorrect arguments provided");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("All arguments must be not null");
                return;
            }
        }
        String host = args[0], prefix = args[2];
        int port, threads, requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Not a correct number provided" + e.getMessage());
            return;
        }
        new HelloUDPClient().run(host, port, prefix, threads, requests);
    }
}
