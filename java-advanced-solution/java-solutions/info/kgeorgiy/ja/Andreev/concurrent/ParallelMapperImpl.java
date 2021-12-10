package info.kgeorgiy.ja.Andreev.concurrent;

import java.util.*;
import java.util.function.Function;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class ParallelMapperImpl implements ParallelMapper {

    private static final int DEFAULT_PARTITION_SIZE = 1;

    private final TaskQueue tasks = new TaskQueue();

    private final List<Thread> listThreads;


    /**
     * Allocates a new ParallelMapperImpl object, and starts {@code threads} threads
     * @param threads count starts thread
     */
    public ParallelMapperImpl(int threads) {

        final Runnable runner = () -> {
            try {
                while (!Thread.interrupted()) {
                    tasks.nextSubTask().run();
                }
            } catch (final InterruptedException e) {
               //toDO
            } finally {
                Thread.currentThread().interrupt();
            }
        };

        listThreads = new ArrayList<>();

        for (int numberThread = 0; numberThread < threads; numberThread++) {
            listThreads.add(new Thread(runner));
        }

        for (Thread thread : listThreads) {
            thread.start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param f maps function
     * @param args elements
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Task<T, R> newTask = new Task<>(f, args, DEFAULT_PARTITION_SIZE);
        tasks.addTask(newTask);
        return newTask.waitResult();
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        listThreads.forEach(Thread::interrupt);

        for (int i = 0; i < listThreads.size(); i++) {
            try {
                listThreads.get(i).join();
            } catch (InterruptedException e) {
                i--; // Что делать?
            }
        }
    }

    /**
     * Thread-safe class wrapper over the Queue
     */
    private static class TaskQueue {

        private final Queue<Task<?, ?>> queue;

        /**
         * Allocates a new TaskQueue object
         */
        TaskQueue() {
            queue = new ArrayDeque<>();
        }


        synchronized void addTask(Task<?, ?> task) {
            queue.add(task);
            notifyAll();
        }

        synchronized Runnable nextSubTask() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            Task<?, ?> head = queue.element();
            Runnable subTask = head.nextSubTask();
            if (head.isDistributed()) {
                queue.remove();
            }
            return subTask;
        }
    }

    /**
     * Task class
     * @param <T> type of array
     * @param <R> type or result
     */
    private static class Task<T, R> {

        final private Function<? super T, ? extends R> f;
        final private List<? extends T> args;
        final private List<R> result;
        final private int partitionSize;
        final private int size;
        private volatile boolean distributed = false;
        private volatile int countThread;
        private int start = 0;

        /**
         * Allocates a new Task
         * @param f maps function
         * @param args elements
         * @param partitionSize size one part
         */
        Task(Function<? super T, ? extends R> f, List<? extends T> args, int partitionSize) {
            this.f = f;
            this.args = args;
            this.partitionSize = partitionSize;
            size = args.size();
            countThread = size / partitionSize + (size % partitionSize != 0 ? 1 : 0);
            result = new ArrayList<>(Collections.nCopies(size, null));
        }

        synchronized Runnable nextSubTask() {
            int l = start;
            int r = start + partitionSize;
            if (r >= size) {
                distributed = true;
                r = args.size();
            }
            start = r;
            return new SubTask(l, r);
        }

        boolean isDistributed() {
            return distributed;
        }

        synchronized List<R> waitResult() throws InterruptedException {
            while (!distributed || countThread != 0) {
                wait();
            }
            return result;
        }


        synchronized void subTaskFinish() {
            countThread--;
            if (countThread == 0) {
                notify();
            }
        }

        /**
         * Inner class that characterizes one part of a task
         */
        class SubTask implements Runnable {
            final private int l, r;

            SubTask(int l, int r) {
                this.l = l;
                this.r = r;
            }

            /**
             * Run part task
             */
            public void run() {
                for (int i = l; i < r; i++) {
                    result.set(i, f.apply(args.get(i)));
                }
                subTaskFinish();
            }

        }
    }

}
