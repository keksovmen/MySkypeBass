package Bin;

public interface Expendable {

    /*
    * Used by AudioClient and Part of GUI
    * Processor thread is subject others observers
     */

    boolean add(int id);
    void remove(int id);
    void close();
}
