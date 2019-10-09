package com.Util.Interfaces;

/**
 * Uses for classes that can start a new thread
 */

public interface Starting {


    /**
     * Starts a new thread
     *
     * @param name with given name
     */
    boolean start(String name);

    /**
     * Stop the thread
     */

    void close();

}
