package info.kgeorgiy.ja.Andreev.concurrent;

import java.util.ArrayList;
import java.util.List;

public class main {

    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(7);
        list.add(8);
        list.add(4);
        IterativeParallelism iterativeParallelism = new IterativeParallelism();
        System.out.println(iterativeParallelism.maximum(10, list, Integer::compare));
    }
}
