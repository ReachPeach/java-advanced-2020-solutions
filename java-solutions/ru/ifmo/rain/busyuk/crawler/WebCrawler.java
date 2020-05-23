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

    public WebCrawler(Downloader downloader, int downloads, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloads);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.hostManagerMap = new ConcurrentHashMap<>();
        this.perHost = perHost;
    }

    private Optional<String> getHostName(String url, Map<String, IOException> urlsWithExceptions) {
        Optional<String> result = Optional.empty();
        try {
            result = Optional.of(URLUtils.getHost(url));
        } catch (MalformedURLException e) {
            urlsWithExceptions.put(url, e);
        }
        return result;
    }

    private Runnable newExtractorsTask(Document downloaded, int remainingDepth, Phaser phaser, String url,
                                       Set<String> visitedUrls, Map<String, IOException> urlsWithExceptions) {
        return () -> {
            try {
                for (String link : downloaded.extractLinks()) {
                    downloadRecursively(link, remainingDepth - 1,
                            phaser, visitedUrls, urlsWithExceptions);
                }
            } catch (IOException e) {
                urlsWithExceptions.put(url, e);
            } finally {
                phaser.arrive();
            }
        };
    }

    private Runnable newDownloadersTask(String url, int remainingDepth, Phaser phaser, String hostName,
                                        Set<String> visitedUrls, Map<String, IOException> urlsWithExceptions) {
        return () -> {
            try {
                Document downloaded = downloader.download(url);
                if (remainingDepth > 0) {
                    Runnable extractorsTask = newExtractorsTask(downloaded, remainingDepth, phaser, url,
                            visitedUrls, urlsWithExceptions);
                    phaser.register();
                    extractors.submit(extractorsTask);
                }
            } catch (IOException e) {
                urlsWithExceptions.put(url, e);
            } finally {
                hostManagerMap.computeIfPresent(hostName, ((someUrl, hostManager) -> {
                    Runnable task = hostManager.pollTask();
                    if (task != null) {
                        downloaders.submit(task);
                    }
                    return hostManager;
                }));
                phaser.arrive();
            }
        };
    }

    private void downloadRecursively(String url, int remainingDepth, Phaser phaser,
                                     Set<String> visitedUrls, Map<String, IOException> urlsWithExceptions) {
        if (!visitedUrls.add(url)) {
            return;
        }

        getHostName(url, urlsWithExceptions).ifPresent(hostName -> {
            Runnable downloadersTask = newDownloadersTask(url, remainingDepth, phaser, hostName,
                    visitedUrls, urlsWithExceptions);
            phaser.register();

            hostManagerMap.compute(hostName, ((someUrl, hostManager) -> {
                if (hostManager == null) {
                    hostManager = new HostManager();
                }
                if (!hostManager.addTask(downloadersTask, perHost)) {
                    downloaders.submit(downloadersTask);
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

    private static class HostManager {
        private final AtomicInteger count = new AtomicInteger();
        private final Queue<Runnable> tasks = new LinkedList<>();

        synchronized private Runnable pollTask() {
            if (!tasks.isEmpty()) {
                return tasks.poll();
            } else {
                count.decrementAndGet();
                return null;
            }
        }

        synchronized private boolean addTask(Runnable downloadersTask, int perHost) {
            if (count.get() >= perHost) {
                tasks.add(downloadersTask);
                return true;
            } else {
                count.incrementAndGet();
                return false;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 5) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        } else {
            int[] parsedArgs = new int[4];
            for (int i = 1; i <= 5; i++) {
                try {
                    parsedArgs[i - 1] = (i < args.length ? Integer.parseInt(args[i]) : 1);
                } catch (NumberFormatException e) {
                    System.err.println("Not a correct number: " + args[i]);
                    return;
                }
            }
            String url = args[0];
            int depth = parsedArgs[0], downloads = parsedArgs[1], extractors = parsedArgs[2], perHost = parsedArgs[3];
            Downloader downloader;
            try {
                downloader = new CachingDownloader();
            } catch (IOException e) {
                throw new IOException("Error while initialising CachingDownloader" + e.getMessage(), e);
            }
            try (WebCrawler webCrawler = new WebCrawler(downloader, downloads, extractors, perHost)) {
                Result result = webCrawler.download(url, depth);
                System.out.print("Successfully downloaded " + result.getDownloaded().size() + " pages, " +
                        result.getErrors().size() + " errors occurred");
            }
        }
    }
}