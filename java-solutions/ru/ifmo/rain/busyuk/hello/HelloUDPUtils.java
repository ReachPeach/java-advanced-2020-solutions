package ru.ifmo.rain.busyuk.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class HelloUDPUtils {
    protected static DatagramPacket makePacket(final byte[] buffer, final SocketAddress to) {
        return new DatagramPacket(buffer, 0, buffer.length, to);
    }

    protected static DatagramPacket makePacket(int bufferSize) {
        final byte[] buffer = new byte[bufferSize];
        return new DatagramPacket(buffer, bufferSize);
    }

    protected static String parsePacket(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    protected static boolean responseMatches(final int requestNumber, final int threadNumber, final String response) {
        String regex = String.format("^\\D*%d\\D+%d\\D*$", threadNumber, requestNumber);
        return Pattern.matches(regex, response);
    }

}
