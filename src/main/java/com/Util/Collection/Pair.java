package com.Util.Collection;

public class Pair <T, Y> {

    private final T t;
    private  final Y y;

    public Pair(T t, Y y) {
        this.t = t;
        this.y = y;
    }

    public T getFirst() {
        return t;
    }

    public Y getSecond() {
        return y;
    }
}
