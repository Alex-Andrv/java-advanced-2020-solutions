package info.kgeorgiy.ja.Andreev.crawler;

import info.kgeorgiy.java.advanced.base.BaseTester;
import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.EasyCrawlerTest;
import info.kgeorgiy.java.advanced.crawler.HardCrawlerTest;
import info.kgeorgiy.java.advanced.crawler.Tester;

import java.io.IOException;

public class TesterWebCrawler extends BaseTester {
    public static void main(final String... args) throws IOException {
        new Tester()
                .add("easy", EasyCrawlerTest.class)
                .add("hard", HardCrawlerTest.class)
                .run(args);
//        System.out.println(new WebCrawler(new CachingDownloader(), 5, 5, 5).download("https://www.kgeorgiy.info:4443/courses/java-advanced/homeworks.html#homework-crawler", 3));

    }
}
