package com.Abstraction.Util.Interfaces;

/**
 * Uses for classes that can start a new thread
 */

public interface Starting {


    /**
     * Starts a new thread
     *
     * @param name with given name
     * @return true if a new Thread started
     */
    boolean start(String name);

    /**
     * Stop the thread
     */

    void close();

}
