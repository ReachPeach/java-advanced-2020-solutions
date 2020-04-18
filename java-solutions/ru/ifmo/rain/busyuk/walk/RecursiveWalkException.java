package ru.ifmo.rain.busyuk.walk;

import java.io.IOException;

public class RecursiveWalkException extends IOException {
    RecursiveWalkException(String message, Exception e) {
        super(message, e);
    }

    RecursiveWalkException(String message) {
        super(message);
    }
}
