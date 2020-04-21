package ru.ifmo.rain.busyuk.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultCollector<R> {
    private int done;
    private final int count;
    private final List<R> results;

    ResultCollector(int size) {
        done = 0;
        count = size;
        results = new ArrayList<>(Collections.nCopies(size, null));
    }

    protected synchronized void set(int pos, R result) {
        results.set(pos, result);
        done++;
        if (done >= count) {
            notify();
        }
    }

    protected synchronized List<R> getResults() throws InterruptedException {
        while (done < count) {
            wait();
        }
        return results;
    }
}

