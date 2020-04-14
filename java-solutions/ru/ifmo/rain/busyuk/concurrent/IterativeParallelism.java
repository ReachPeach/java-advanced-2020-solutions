package ru.ifmo.rain.busyuk.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements info.kgeorgiy.java.advanced.concurrent.AdvancedIP {
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.min(comparator).orElseThrow(),
                stream -> stream.min(comparator).orElseThrow());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(item -> item.equals(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.map(f).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, stream -> stream.reduce(monoid.getIdentity(), (left, right) ->
                        monoid.getOperator().apply(left, right), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), (left, right) ->
                        monoid.getOperator().apply(left, right), monoid.getOperator()));
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return reduce(threads, map(threads, values, lift), monoid);
    }

    private <T, R> R mapReduce(int threadCount, List<? extends T> values, Function<Stream<? extends T>, R> mapper,
                               Function<Stream<R>, R> reducer) throws InterruptedException {
        if (threadCount <= 0 || values == null) {
            throw new IllegalArgumentException("provided 0 threads or empty values");
        }

        threadCount = Math.min(threadCount, values.size());
        List<Thread> threads = new ArrayList<>();
        int blockCapacity = values.size() / threadCount;
        int remaining = values.size() % threadCount;
        List<R> threadResults = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            threadResults.add(null);
        }
        for (int i = 0, l, r = 0; i < threadCount; i++) {
            l = r;
            r += blockCapacity;
            if (remaining != 0) {
                r++;
                remaining--;
            }
            if (l == r) {
                break;
            }
            final int index = i, left = l, right = r;
            Thread thread = new Thread(() -> threadResults.set(index, mapper.apply(values.subList(left, right).stream())));
            thread.start();
            threads.add(thread);
        }
        joinThreads(threads);
        return reducer.apply(threadResults.stream());
    }

    private void joinThreads(List<Thread> threads) throws InterruptedException {
        List<InterruptedException> exceptions = new ArrayList<>();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            InterruptedException exception = exceptions.get(0);
            exceptions.stream().skip(1).forEach(exception::addSuppressed);
            throw exception;
        }
    }
}
