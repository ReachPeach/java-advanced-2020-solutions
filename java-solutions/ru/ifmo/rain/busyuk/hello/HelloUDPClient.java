package ru.ifmo.rain.busyuk.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HelloUDPClient implements info.kgeorgiy.java.advanced.hello.HelloClient {
    private String mask = "Hello, ";

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

    private boolean check(final int threadInd, final int threadId, final String response) {
        String regex = String.format("^\\D*%d\\D+%d\\D*$", threadId, threadInd);
        return Pattern.matches(regex, response);
    }

    private void send(final SocketAddress to, final String prefix, final int requests, final int threadId) {
        try (var socket = new DatagramSocket()) {
            socket.setSoTimeout(1000);
            for (int i = 0; i < requests; i++) {
                final var message = String.format("%s%d_%d", prefix, threadId, i);
                final var request = makePacket(message.getBytes(StandardCharsets.UTF_8), to);
                final var response = makePacket(socket.getReceiveBufferSize());
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        socket.send(request);
                        System.out.println(String.format("Send to %s: %s%n", to.toString(), message));
                        socket.receive(response);
                        final String respondMessage = parsePacket(response);
                        if (check(i, threadId, respondMessage)) {
                            System.out.println(String.format("Text of response: %s%n", respondMessage));
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println(String.format("Failed send in thread %d: %s", threadId, e.getMessage()));
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Failed creation of socket: " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int countOfThreads, int countOfRequests) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Failed take a name by host: " + host);
            return;
        }

        final var to = new InetSocketAddress(address, port);
        final var senders = Executors.newFixedThreadPool(countOfThreads);
        for (int i = 0; i < countOfThreads; i++) {
            int finalI = i;
            senders.execute(() -> send(to, prefix, countOfRequests, finalI));
        }
        senders.shutdown();
        try {
            senders.awaitTermination(countOfThreads * countOfRequests * 10, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Incorrect Arguments!");
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (args[i] == null) {
                System.out.println("Arguments mustn't be a null");
                return;
            }
        }
        String host = args[0];
        int port;
        String prefix = args[2];
        int threads;
        int requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Format of numbers is incorrect!");
            return;
        }
        new HelloUDPClient().run(host, port, prefix, threads, requests);
    }
}
