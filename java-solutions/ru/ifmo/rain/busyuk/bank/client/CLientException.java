package ru.ifmo.rain.busyuk.bank.client;

public class CLientException extends Exception {
    public CLientException() {
    }

    public CLientException(String message) {
        super(message);
    }

    public CLientException(String message, Throwable cause) {
        super(message, cause);
    }
}
