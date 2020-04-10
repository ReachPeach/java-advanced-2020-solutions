package ru.ifmo.rain.busyuk.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements info.kgeorgiy.java.advanced.concurrent.AdvancedIP {
    private int threadCount, blockCapacity, remaining;
    private List<Thread> threads;

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return perform(threads, values, stream -> stream.min(comparator).orElseThrow(),
                stream -> stream.min(comparator).orElseThrow());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return perform(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(item -> item.equals(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return perform(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return perform(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return perform(threads, values, stream -> stream.map(f).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return perform(threads, values, stream -> stream.reduce(monoid.getIdentity(), (left, right) ->
                        monoid.getOperator().apply(left, right), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), (left, right) ->
                        monoid.getOperator().apply(left, right), monoid.getOperator()));
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return reduce(threads, map(threads, values, lift), monoid);
    }

    private <T, R> R perform(int threadProvidedCount, List<? extends T> values, Function<Stream<? extends T>, R> sourceApplier,
                             Function<Stream<R>, R> resultsApplier) throws InterruptedException {
        threadCount = Math.min(threadProvidedCount, values.size());
        threads = new ArrayList<>();
        blockCapacity = values.size() / threadCount;
        remaining = values.size() - threadCount * blockCapacity;
        List<R> threadsResults = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) threadsResults.add(null);
        fillThreads(threadsResults, values, sourceApplier);
        joinThreads();
        return resultsApplier.apply(threadsResults.stream());
    }

    private <T, R> void fillThreads(List<R> threadsResults, List<? extends T> values,
                                    Function<Stream<? extends T>, R> sourceApplier) {
        for (int i = 0, l, r = 0; i < threadCount; i++) {
            l = r;
            r += blockCapacity;
            if (remaining != 0) {
                r++;
                remaining--;
            }
            if (l == r) break;
            fillThread(threadsResults, values.subList(l, r).stream(), sourceApplier, i);
        }
    }

    private <T, R> void fillThread(List<R> threadsResult, Stream<? extends T> values,
                                   Function<Stream<? extends T>, R> sourceApplier, int index) {
        Thread thread = new Thread(() -> threadsResult.set(index, sourceApplier.apply(values)));
        thread.start();
        threads.add(thread);
    }

    private void joinThreads() throws InterruptedException {
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
