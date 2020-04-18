package ru.ifmo.rain.busyuk.concurrent;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements info.kgeorgiy.java.advanced.mapper.ParallelMapper {
    private List<Thread> threads;
    private final Queue<Runnable> consumers;

    public ParallelMapperImpl(int threadCount) {
        consumers = new LinkedList<>();
        threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (consumers) {
                            while (consumers.isEmpty()) {
                                consumers.wait();
                            }
                            task = consumers.poll();
                            consumers.notifyAll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                }
            }));
            threads.get(i).start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultCollector<R> threadResults = new ResultCollector<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            synchronized (consumers) {
                while (!consumers.isEmpty()) {
                    consumers.wait();
                }
                consumers.add(() -> threadResults.set(index, f.apply(args.get(index))));
                consumers.notify();
            }
        }
        return threadResults.getResults();
    }

    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}