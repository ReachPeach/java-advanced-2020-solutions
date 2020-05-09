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
    private final Map<String, HostData> hostDataMap;
    private final int hostCount;

    private static class HostData {
        private AtomicInteger count = new AtomicInteger();
        private Queue<Runnable> tasks = new LinkedList<>();
    }

    public WebCrawler(Downloader downloader, int downloads, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloads);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.hostDataMap = new ConcurrentHashMap<>();
        this.hostCount = perHost;
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

    private void downloadRec(String url, int remainingDepth, Phaser phaser,
                             final Set<String> visitedPages, final Map<String, IOException> pagesWithExceptions) {
        if (url == null || url.isEmpty() || visitedPages.contains(url)) {
            return;
        }
        visitedPages.add(url);

        getHost(url, pagesWithExceptions).ifPresent(hostName -> {
            Runnable downloaderTask = () -> {
                try {
                    Document downloaded = downloader.download(url);

                    Runnable extractorsTask = () -> {
                        try {
                            if (remainingDepth > 0) {
                                for (String link : downloaded.extractLinks()) {
                                    downloadRec(link, remainingDepth - 1,
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

                hostDataMap.computeIfPresent(hostName, ((s, hostInfo) -> {
                    if (!hostInfo.tasks.isEmpty()) {
                        downloaders.submit(hostInfo.tasks.poll());
                    } else {
                        hostInfo.count.decrementAndGet();
                    }
                    return hostInfo;
                }));
                phaser.arrive();
            };

            phaser.register();

            hostDataMap.compute(hostName, ((someUrl, hostData) -> {
                if (hostData == null) {
                    hostData = new HostData();
                }
                if (hostData.count.get() >= hostCount) {
                    hostData.tasks.add(downloaderTask);
                } else {
                    hostData.count.incrementAndGet();
                    downloaders.submit(downloaderTask);
                }
                return hostData;
            }));
        });
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> visitedPages = new ConcurrentSkipListSet<>();
        Map<String, IOException> pagesWithExceptions = new ConcurrentHashMap<>();

        Phaser phaser = new Phaser(1);
        downloadRec(url, depth - 1, phaser, visitedPages, pagesWithExceptions);
        phaser.arriveAndAwaitAdvance();

        visitedPages.removeAll(pagesWithExceptions.keySet());
        return new Result(new ArrayList<>(visitedPages), pagesWithExceptions);
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
        }
    }
}