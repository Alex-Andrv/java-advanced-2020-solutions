package info.kgeorgiy.ja.Andreev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIPTest;
import info.kgeorgiy.java.advanced.concurrent.ListIPTest;
import info.kgeorgiy.java.advanced.concurrent.ScalarIPTest;
import info.kgeorgiy.java.advanced.concurrent.Tester;

public class TesterIterativeParallelism extends Tester {

    public static void main(String[] args) {
        new TesterIterativeParallelism()
                .add("scalar", ScalarIPTest.class)
                .add("list", ListIPTest.class)
                .add("advanced", AdvancedIPTest.class)
                .run(args);
    }
}