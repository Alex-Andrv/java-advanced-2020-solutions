package info.kgeorgiy.ja.Andreev.arrayset;

import java.util.*;

@SuppressWarnings("unchecked")
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    //java -cp . -p . -m info.kgeorgiy.java.advanced.arrayset SortedSet info.kgeorgiy.ja.Andreev.arrayset.ArraySet

    private final List<T> list;
    private final Comparator<? super T> comparator;
    private final boolean reverse;

    // <Constructors
    public ArraySet() {
        this(new ArrayList<>(), null);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(new ArrayList<>(), comparator);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.reverse = false;
        final TreeSet<T> tmp = new TreeSet<>(comparator); // Does not allow null
        tmp.addAll(collection);
        this.list = new ArrayList<>(tmp);
    }

    private ArraySet(List<T> collection, Comparator<? super T> comparator, boolean reverse) {
        this.list = collection;
        this.comparator = comparator;
        this.reverse = reverse;
    }

    // />


    @Override
    public Iterator<T> iterator() {
        return new Itr(Collections.unmodifiableList(list), reverse);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T floor(T t) {
        return IndexOrNull(indexFloor(t, true));
    }

    @Override
    public T lower(T t) {
        return IndexOrNull(indexFloor(t, false));
    }

    private int indexFloor(T t, boolean include) {
        return reverse ? findIndexCeiling(t, include) : findIndexFloor(t, include);
    }

    @Override
    public T ceiling(T t) {
        return IndexOrNull(indexCeiling(t, true));
    }

    @Override
    public T higher(T t) {
        return IndexOrNull(indexCeiling(t, false));
    }

    private int indexCeiling(T t, boolean include) {
        return reverse ? findIndexFloor(t, include) : findIndexCeiling(t, include);
    }

    private int findIndexFloor(T element, boolean include) {
        int ind = findIndexElement(element);
        return ind >= 0 ? ind - (include ? 0 : 1) : getInsertionPoint(ind) - 1;
    }

    private int findIndexCeiling(T element, boolean include) {
        int ind = findIndexElement(element);
        return ind >= 0 ? ind + (include ? 0 : 1) : getInsertionPoint(ind);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("We can not modify the collection");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("We can not modify the collection");
    }

    private int getInsertionPoint(int ind ) {
        return - ind - 1;
    }

    private T IndexOrNull(int ind) {
        return (ind >= 0 && ind < size()) ? list.get(ind) : null;
    }

    private int findIndexElement(T t) {
        checkNotNull(t);
        return Collections.binarySearch(list, t, comparator);
    }

    private void checkNotNull(Object obj) {
        if (obj == null)
            throw new NullPointerException("This set does not permit null elements");
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(list, comparator, !reverse);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @SuppressWarnings("unchecked")
    private int compare(final T fromElement, final T toElement) {
        return (comparator() == null) ? ((Comparable<T>) fromElement).compareTo(toElement) : comparator().compare(fromElement, toElement);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
//        checkNotNull(fromElement); findIndexElement throw this exception
//        checkNotNull(toElement);
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("The left border should not be bigger than the right");
        }
        int from = findIndexCeiling(fromElement, fromInclusive);
        int to = findIndexFloor(toElement, toInclusive);
        if (reverse) {
            int tmp = from;
            from = to;
            to = tmp;
        }
        if (from > to) {
            return new ArraySet<>(comparator);
        }
        return subSet(from, to + 1);
    }

    private NavigableSet<T> subSet(int from,  int to) {
        try {
            return new ArraySet<>(list.subList(from, to), comparator, reverse);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("fromElement is greater than toElement; or if this set itself has a restricted range, and fromElement or toElement lies outside the bounds of the range.\n" + e.getMessage());
        }
    }

    @Override
    public boolean contains(final Object obj) {
//        if (obj == null || !(obj instanceof T)) {
//            return false;
//        }
        return IndexOrNull(findIndexElement((T) obj)) != null;
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return reverse ? tail(toElement, inclusive) : head(toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return reverse ? head(fromElement, inclusive) : tail(fromElement, inclusive);
    }

    private NavigableSet<T> head(T toElement, boolean inclusive) {
        int to = findIndexFloor(toElement, inclusive);
        return subSet(0, to + 1);
    }

    private NavigableSet<T> tail(T fromElement, boolean inclusive) {
        int from = findIndexCeiling(fromElement, inclusive);
        return subSet(from, size());
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return reverse ?  Collections.reverseOrder(comparator) : comparator;
    }

    @Override
    public T first() {
        if (list.isEmpty())
            throw new NoSuchElementException("Set is empty");
        return get(0);
    }

    @Override
    public T last() {
        if (list.isEmpty())
            throw new NoSuchElementException("Set is empty");
        return get(size() - 1);
    }

    private T get(int index) {
        return list.get(reverse ? size() - index - 1 : index);
    }

    private class Itr implements Iterator<T> {

        private final List<T> list;
        private final boolean reverse;
        private int cursor;

        Itr(List<T> list, boolean reverse) {
            this.list = list;
            this.reverse = reverse;
            cursor = reverse ? list.size() : 0;
        }

        @Override
        public boolean hasNext() {
            return reverse ? cursor != 0 : cursor != list.size();
        }


        @Override
        public T next() {
            int i = cursor;
            if (i >= size() || i < 0)
                throw new NoSuchElementException();
            cursor = reverse ? i - 1 : i + 1;
            return list.get(i);
        }
    }
}
