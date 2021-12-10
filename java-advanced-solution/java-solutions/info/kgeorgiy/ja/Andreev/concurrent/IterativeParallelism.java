package info.kgeorgiy.ja.Andreev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementation of the {@code AdvancedIP} interface, providing methods for parallel data processing.
 *
 * @author Alex_Andrv
 */
public class IterativeParallelism implements ListIP {

    //java -cp . -p . -m info.kgeorgiy.java.advanced.concurrent info.kgeorgiy.ja.Andreev.concurrent.IterativeParallelism

    private final ParallelMapper mapper;

    /**
     * Mapper constructor.
     *
     * @param mapper {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Default constructor.
     */
    public IterativeParallelism() {
        this.mapper = null;
    }

    /**
     *  Apply function {@code process} to all {@code value} elements split into {@code threads} threads.
     *  And then apply function {@code reduce}.
     * @param threads count of concurred thread
     * @param values value to process and then reduce
     * @param process a {@code Function}, capable of reducing a {@code Stream} of values of type {@code T}
     *          to a single value of type {@code M}
     * @param reduce a {@code Function}, capable of reducing a {@code Stream} of values of type {@code M}
     *          to a single value of type {@code R}
     * @param <T> type of element in {@code values}
     * @param <M> type of middle element
     * @param <R> result type
     * @return the result of reducing
     * @throws InterruptedException when thread was interrupted
     */
    private <T, M, R> R abstractTask(int threads, List<? extends T> values,
                               Function<Stream<? extends T>, M> process,
                               Function<Stream<? extends M>, R> reduce) throws InterruptedException {
        if (threads <= 0 ) {
            throw new IllegalArgumentException("Impossible to split into " + threads);
        }
        List<Stream<? extends T>> workingParts = splitValues(threads, values);
        List<M> result;
        if (mapper == null) {
            result = new ArrayList<>(Collections.nCopies(workingParts.size(), null));
            List<Thread> listThreads = new ArrayList<>();
            for (int i = 0; i < workingParts.size(); i++) {
                final int index = i;
                final Stream<? extends T> workingPart = workingParts.get(i);
                Thread newThread = new Thread(() -> result.set(index, process.apply(workingPart)));
                newThread.start();
                listThreads.add(newThread);
            }
            for (Thread thread : listThreads) {
                thread.join();
            }
        } else {
            result = mapper.map(process, workingParts);
        }
        return reduce.apply(result.stream());
    }


    private <T> List<Stream<? extends T>> splitValues (int threads, List<? extends T> values) {
        List<Stream<? extends T>> workingParts = new ArrayList<>();
        int countBlocks = values.size() / threads;
        int remainder = values.size() % threads;
        int start = 0;
        for (int i = 0; i < threads && start < values.size(); i++) {
            int end = start + countBlocks + (remainder > i ? 1 : 0);
            assert(start != end);
            Stream<? extends T> workingPart = values.subList(start, end).stream();
            workingParts.add(workingPart);
            start = end;
        }
        return workingParts;
    }



    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return list of joined result of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicated. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.filter(predicate),
                stream -> stream.flatMap(Function.identity()).collect(Collectors.toList()));
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values  values to filter.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.map(f),
                stream -> stream.flatMap(Function.identity()).collect(Collectors.toList()));
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfies predicate or {@code true}, if no values are given
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return abstractTask(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
