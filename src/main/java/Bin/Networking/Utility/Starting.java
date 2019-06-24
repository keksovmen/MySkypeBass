package Bin.Networking.Utility;

/**
 * Uses for classes that can start a new thread
 */
public interface Starting {


    /**
     * Starts a new thread
     *
     * @param name with given name
     */
    void start(String name);

    /**
     * Stop the thread
     */

    void close();
}
