package com.Abstraction.Util.Interfaces;

/**
 * Uses as container
 * First register some listeners
 * than iterate when action occurs
 * @param <T> type of listener
 */

public interface Registration<T> {

    void attach(T listener);

    void detach(T listener);
}
