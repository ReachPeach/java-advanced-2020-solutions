package ru.ifmo.rain.busyuk.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements info.kgeorgiy.java.advanced.crawler.Crawler {
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final Downloader downloader;
    private final Map<String, HostManager> hostManagerMap;
    private final int perHost;

    private static class HostManager {
        private AtomicInteger count = new AtomicInteger();
        private Queue<Runnable> tasks = new LinkedList<>();

        private int getCount() {
            return count.get();
        }

        private void addTask(Runnable task) {
            tasks.add(task);
        }

        private void decrementCount() {
            count.decrementAndGet();
        }

        private void incrementCount() {
            count.incrementAndGet();
        }

        private boolean isEmpty() {
            return tasks.isEmpty();
        }

        private Runnable getTask() {
            return tasks.poll();
        }
    }

    public WebCrawler(Downloader downloader, int downloads, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloads);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.hostManagerMap = new ConcurrentHashMap<>();
        this.perHost = perHost;
    }

    private Optional<String> getHost(String url, Map<String, IOException> pagesWithExceptions) {
        Optional<String> result = Optional.empty();
        try {
            result = Optional.of(URLUtils.getHost(url));
        } catch (MalformedURLException e) {
            pagesWithExceptions.put(url, e);
        }
        return result;
    }

    private Runnable getDownloaderTask(String url, int remainingDepth, Phaser phaser, String hostName,
                                       Set<String> visitedPages, Map<String, IOException> pagesWithExceptions) {
        return () -> {
            try {
                Document downloaded = downloader.download(url);

                Runnable extractorsTask = () -> {
                    try {
                        if (remainingDepth > 0) {
                            for (String link : downloaded.extractLinks()) {
                                downloadRecursively(link, remainingDepth - 1,
                                        phaser, visitedPages, pagesWithExceptions);
                            }
                        }
                    } catch (IOException e) {
                        pagesWithExceptions.put(url, e);
                    } finally {
                        phaser.arrive();
                    }
                };

                phaser.register();
                extractors.submit(extractorsTask);
            } catch (IOException e) {
                pagesWithExceptions.put(url, e);
            }

            hostManagerMap.computeIfPresent(hostName, ((s, hostManager) -> {
                if (!hostManager.isEmpty()) {
                    downloaders.submit(hostManager.getTask());
                } else {
                    hostManager.decrementCount();
                }
                return hostManager;
            }));
            phaser.arrive();
        };
    }

    private void downloadRecursively(String url, int remainingDepth, Phaser phaser,
                                     final Set<String> visitedUrls, final Map<String, IOException> urlsWithExceptions) {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);

        getHost(url, urlsWithExceptions).ifPresent(hostName -> {
            Runnable downloaderTask = getDownloaderTask(url, remainingDepth, phaser, hostName,
                    visitedUrls, urlsWithExceptions);
            phaser.register();

            hostManagerMap.compute(hostName, ((someUrl, hostManager) -> {
                if (hostManager == null) {
                    hostManager = new HostManager();
                }

                if (hostManager.getCount() >= perHost) {
                    hostManager.addTask(downloaderTask);
                } else {
                    hostManager.incrementCount();
                    downloaders.submit(downloaderTask);
                }
                return hostManager;
            }));
        });
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> visitedUrls = new ConcurrentSkipListSet<>();
        Map<String, IOException> urlsWithExceptions = new ConcurrentHashMap<>();

        Phaser phaser = new Phaser(1);
        downloadRecursively(url, depth - 1, phaser, visitedUrls, urlsWithExceptions);
        phaser.arriveAndAwaitAdvance();

        visitedUrls.removeAll(urlsWithExceptions.keySet());
        return new Result(new ArrayList<>(visitedUrls), urlsWithExceptions);
    }

    @Override
    public void close() {
        extractors.shutdownNow();
        downloaders.shutdownNow();
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 5) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        } else {
            int[] newArgs = new int[4];
            for (int i = 1; i <= 5; i++) {
                try {
                    newArgs[i - 1] = (i < args.length ? Integer.parseInt(args[i]) : 1);
                } catch (NumberFormatException e) {
                    System.err.println("Not a correct number: " + args[i]);
                    newArgs[i - 1] = 1;
                }
            }
            Downloader downloader;
            try {
                downloader = new CachingDownloader();
            } catch (IOException e) {
                System.err.println("Error while initialising CachingDownloader");
                return;
            }
            WebCrawler webCrawler = new WebCrawler(downloader, newArgs[1], newArgs[2], newArgs[3]);
            Result result = webCrawler.download(args[1], newArgs[0]);
            System.out.print("Successfully downloaded " + result.getDownloaded().size() + " pages, " +
                    result.getErrors().size() + " errors occurred");
            webCrawler.close();
            //debug change//
        }
    }
}