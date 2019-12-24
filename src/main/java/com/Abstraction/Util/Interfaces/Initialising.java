package com.Abstraction.Util.Interfaces;

/**
 * Use with classes that at some point need to be lazy initialised
 * Call this function only 1 time for the whole application live
 */

public interface Initialising {

    void init();
}
