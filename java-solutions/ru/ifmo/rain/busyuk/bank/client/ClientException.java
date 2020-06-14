package ru.ifmo.rain.busyuk.bank.client;

public class ClientException extends Exception {
    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(String message) {
        super(message);
    }
}
