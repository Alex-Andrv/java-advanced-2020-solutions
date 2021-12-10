package info.kgeorgiy.ja.Andreev.walk;

import info.kgeorgiy.java.advanced.walk.RecursiveWalkTest;
import info.kgeorgiy.java.advanced.walk.Tester;
import info.kgeorgiy.java.advanced.walk.WalkTest;

public class TesterWalk extends Tester {

    public static void main(String... args) {
        new Tester()
                .add("Walk", WalkTest.class)
                .add("RecursiveWalk", RecursiveWalkTest.class)
                .add("AdvancedWalk", (tester, cut) -> {
                    tester.test("Walk", cut.replace(".RecursiveWalk", ".Walk"));
                    return tester.test("RecursiveWalk", cut);
                })
                .run(args);
    }
}