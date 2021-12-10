package info.kgeorgiy.ja.Andreev.arrayset;

import info.kgeorgiy.java.advanced.arrayset.AdvancedSetTest;
import info.kgeorgiy.java.advanced.arrayset.NavigableSetTest;
import info.kgeorgiy.java.advanced.arrayset.SortedSetTest;
import info.kgeorgiy.java.advanced.walk.Tester;


public class TesterArraySet extends Tester {
    public static void main(String[]  args) {
        new TesterArraySet()
                .add("SortedSet", SortedSetTest.class)
                .add("NavigableSet", NavigableSetTest.class)
                .add("AdvancedSet", AdvancedSetTest.class)
                .run(args);
    }
}
