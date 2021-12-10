package info.kgeorgiy.ja.Andreev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final Map<String, Semaphore> hostCnt;
    private final int perHost;


    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        this.hostCnt = new ConcurrentHashMap<>();
    }


    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> successfullyUsed = ConcurrentHashMap.newKeySet();
        final Set<String> used = ConcurrentHashMap.newKeySet();
        Set<String> currentLevel = ConcurrentHashMap.newKeySet();
        currentLevel.add(url);
        for (int level = 0; level < depth; level++) {
            final int cntRemainingLevel = depth - level;
            final Set<String> nextLevel = ConcurrentHashMap.newKeySet();
            Phaser phaser = new Phaser(1);
            for (String link : currentLevel) {
                if (used.add(link)) {

                    String host;
                    try {
                        host = URLUtils.getHost(link);
                    } catch (MalformedURLException e) {
                        errors.put(link, e);
                        continue;
                    }

                    final Semaphore semaphore = hostCnt.computeIfAbsent(host, s -> new Semaphore(perHost));

                    phaser.register();

                    downloaders.submit(() -> {
                        try {
                            semaphore.acquire();
                            Document document = downloader.download(link);
                            successfullyUsed.add(link);
                            if (cntRemainingLevel > 1) {
                                phaser.register();
                                extractors.submit(() -> {
                                    try {
                                        List<String> links = document.extractLinks();
                                        links.removeAll(used);
                                        nextLevel.addAll(links);
                                    } catch (IOException e) {
                                        // Ignore
                                    } finally {
                                        phaser.arrive();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            errors.put(link, e);
                        } catch (InterruptedException e) {
                            //Ignore
                        } finally {
                            semaphore.release();
                            phaser.arrive();
                        }
                    });
                }
            }
            phaser.arriveAndAwaitAdvance();
            currentLevel = nextLevel;
        }
        return new Result(new ArrayList<>(successfullyUsed), errors);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
        try {
            awaitTermination(downloaders);
            awaitTermination(extractors);
        } catch (InterruptedException e) {
            downloaders.shutdownNow();
            extractors.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void awaitTermination(ExecutorService executorService) throws InterruptedException {
        final long MAX_WAIT_SECONDS = 60;
        final long MAX_WAIT_MICROSECONDS = 1000;
        if (!executorService.awaitTermination(MAX_WAIT_MICROSECONDS, TimeUnit.MICROSECONDS)) {
            executorService.shutdownNow();
            if (!executorService.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS))
                System.err.println("ExecutorService did not terminate");
        }
    }

    private static int getArgumentOrOne(String[] args, int ind) throws NumberFormatException {
        if (ind >= args.length)
            return 1;
        return Integer.parseInt(args[ind]);
    }

    public static void main(String[] args) {
        Objects.requireNonNull(args);
        if (args.length == 0 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Expected parameters: url [depth [downloads [extractors [perHost]]]]");
        }

        try {
            final String url = args[0];
            final int depth = getArgumentOrOne(args, 1);
            final int downloaders = getArgumentOrOne(args, 2);
            final int extractors = getArgumentOrOne(args, 3);
            final int perHost = getArgumentOrOne(args, 4);
            try (final Crawler wc = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
                wc.download(url, depth);
            } catch (IOException e) {
                System.err.println("Failed to initialize downloader: " + e.getMessage());
            }
        } catch (final NumberFormatException e) {
            System.out.println("Invalid argument");
        }

    }
}
