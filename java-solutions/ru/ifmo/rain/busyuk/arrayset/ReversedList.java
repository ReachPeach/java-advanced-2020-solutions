package ru.ifmo.rain.busyuk.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversedList<T> extends AbstractList {
    private List<T> data;
    private boolean reversed;

    ReversedList(List<T> other) {
        this.data = other;
        this.reversed = false;
    }

    public void reverse() {
        reversed = !reversed;
    }

    @Override
    public T get(int index) {
        return data.get(reversed ? size() - 1 - index : index);
    }

    @Override
    public int size() {
        return data.size();
    }
}
