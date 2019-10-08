package Com.Util.Interfaces;

/**
 * Uses as container
 * First register some listeners
 * than iterate when action occurs
 * @param <T> type of listener
 */

public interface Registration<T> {

    boolean registerListener(T listener);

    boolean removeListener(T listener);
}
