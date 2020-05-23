package ru.ifmo.rain.busyuk.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

class HelloUDPUtils {
    protected final static int AWAIT_TERMINATION_COEFFICIENT = 10;

    protected static DatagramPacket makePacket(final byte[] buffer, final SocketAddress socketAddress) {
        return new DatagramPacket(buffer, 0, buffer.length, socketAddress);
    }

    protected static DatagramPacket makePacket(int bufferSize) {
        final byte[] buffer = new byte[bufferSize];
        return new DatagramPacket(buffer, bufferSize);
    }

    protected static String parsePacket(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    protected static boolean responseMatches(final int requestIndex, final int threadIndex, final String responseMessage) {
        String regex = String.format("^\\D*%d\\D+%d\\D*$", threadIndex, requestIndex);
        return Pattern.matches(regex, responseMessage);
    }

}
