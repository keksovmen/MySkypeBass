package com.Util.History;

/**
 * Simple loop structure that
 * store than retrieve data
 *
 * @param <T> type of the data
 */

public interface History<T> {

    T getNext();

    void push(T data);
}
