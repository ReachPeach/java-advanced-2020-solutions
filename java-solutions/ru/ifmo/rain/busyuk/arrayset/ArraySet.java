package ru.ifmo.rain.busyuk.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this.comparator = null;
        data = Collections.emptyList();
    }

    public ArraySet(Comparator<? super T> comparator) {
        this.comparator = comparator;
        data = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> other) {
        this(other, null);
    }

    public ArraySet(Collection<? extends T> other, Comparator<? super T> comparator) {
        this.comparator = comparator;
        if (!isSorted(other)) {
            NavigableSet<T> navigableSet = new TreeSet<>(this.comparator);
            navigableSet.addAll(other);
            this.data = new ArrayList<>(navigableSet);
        } else {
            this.data = new ArrayList<>(other);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return this.comparator;
    }


    @Override
    public int size() {
        return this.data.size();
    }


    @Override
    public T lower(T t) {
        return getElement(t, false, false);
    }

    @Override
    public T floor(T t) {
        return getElement(t, true, false);
    }

    @Override
    public T ceiling(T t) {
        return getElement(t, true, true);
    }

    @Override
    public T higher(T t) {
        return getElement(t, false, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int startIndex = getIndex(fromElement, fromInclusive, true);
        int endIndex = getIndex(toElement, toInclusive, false);

        if (startIndex > endIndex || endIndex == -1) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(startIndex, endIndex + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(fromElement, inclusive, last(), true);
    }


    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (compare(fromElement, toElement) > 0) throw new IllegalArgumentException();
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
    public T first() {
        checkCapacity();
        return data.get(0);
    }

    @Override
    public T last() {
        checkCapacity();
        return data.get(data.size() - 1);
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    private ArraySet(List<T> other, Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.data = other;
        if (other instanceof ReversedList) {
            ((ReversedList) data).reverse();
        }
    }


    private void checkCapacity() {
        if (data.isEmpty())
            throw new NoSuchElementException();
    }

    private boolean checkIndex(int index) {
        return 0 <= index && index < data.size();
    }


    private int getIndex(T t, boolean inclusive, boolean up) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = -index - 1;
            if (!up) index--;
        } else {
            if (!inclusive) {
                if (up) {
                    index++;
                } else {
                    index--;
                }
            }
        }
        return index;
    }

    private T getElement(T t, boolean inclusive, boolean missing) {
        int index = getIndex(t, inclusive, missing);
        return checkIndex(index) ? data.get(index) : null;
    }

    private boolean isSorted(Collection<? extends T> other) {
        T previous = null;
        for (T current : other) {
            if (previous != null && compare(current, previous) <= 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    private int compare(T firstElement, T secondElement) {
        if (comparator != null) return comparator.compare(firstElement, secondElement);
        return (((Comparable<? super T>) (firstElement)).compareTo(secondElement));
    }

}
