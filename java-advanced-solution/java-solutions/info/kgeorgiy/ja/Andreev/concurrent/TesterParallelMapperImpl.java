package info.kgeorgiy.ja.Andreev.concurrent;

import info.kgeorgiy.java.advanced.mapper.AdvancedMapperTest;
import info.kgeorgiy.java.advanced.mapper.ListMapperTest;
import info.kgeorgiy.java.advanced.mapper.ScalarMapperTest;
import info.kgeorgiy.java.advanced.mapper.Tester;

public class TesterParallelMapperImpl  extends Tester {

    public static void main(String[] args) {
        new Tester()
                .add("scalar", ScalarMapperTest.class)
                .add("list", ListMapperTest.class)
                .add("advanced", AdvancedMapperTest.class)
                .run(args);
    }
}