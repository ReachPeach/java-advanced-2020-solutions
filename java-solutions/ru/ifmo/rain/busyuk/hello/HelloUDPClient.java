package ru.ifmo.rain.busyuk.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static ru.ifmo.rain.busyuk.hello.HelloUDPUtils.*;

public class HelloUDPClient implements info.kgeorgiy.java.advanced.hello.HelloClient {
    private boolean matches(final int requestNumber, final int threadNumber, final String response) {
        String regex = String.format("^\\D*%d\\D+%d\\D*$", threadNumber, requestNumber);
        return Pattern.matches(regex, response);
    }

    private void send(final SocketAddress to, final String prefix, final int requests, final int threadNumber) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            for (int requestNumber = 0; requestNumber < requests; requestNumber++) {
                final String message = String.format("%s%d_%d", prefix, threadNumber, requestNumber);
                final DatagramPacket request = makePacket(message.getBytes(StandardCharsets.UTF_8), to);
                final DatagramPacket response = makePacket(socket.getReceiveBufferSize());
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        socket.send(request);
                        System.out.println(String.format("Send to %s: %s%n", to.toString(), message));
                        socket.receive(response);
                        final String respondMessage = parsePacket(response);
                        if (matches(requestNumber, threadNumber, respondMessage)) {
                            System.out.println(String.format("Text of response: %s%n", respondMessage));
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println(String.format("Failed sending in thread %d: %s", threadNumber, e.getMessage()));
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
            throw new IllegalArgumentException("Failed taking name by host: " + host + e.getMessage(), e);
        }

        final InetSocketAddress to = new InetSocketAddress(address, port);
        final ExecutorService senders = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int finalIndex = i;
            senders.execute(() -> send(to, prefix, requests, finalIndex));
        }
        senders.shutdown();
        try {
            senders.awaitTermination(threads * requests * 10, TimeUnit.MINUTES);
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
