package Com.Util.History;

/**
 * String implementation of History
 * Can be accessed from factory method only
 */

public class StringHistory implements History<String> {

    private String[] history;

    /**
     * Current index of get
     */

    private int index;

    /**
     * Current index of push
     */

    private int pushIndex;

    /**
     * For traversal purposes
     */

    private int initialIndex;

    StringHistory(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Size can't be negative or zero! " + size);
        history = new String[size];
        index = 0;
        pushIndex = 0;
        initialIndex = 0;
    }

    @Override
    public String getNext() {
        initialIndex = index;
        return getNextThroughLoop();
    }

    /**
     * Recursive search until initialIndex != index
     * index is changing with each iteration
     *
     * @return stored or empty string
     */

    private String getNextThroughLoop() {
        index = checkIndex(index);
        String result = history[index];
        index--;
        if (initialIndex == index)
            return "";
        if (result == null)
            return getNextThroughLoop();
        return result;
    }

    @Override
    public void push(String data) {
        index = pushIndex;
        history[pushIndex] = data;
        pushIndex++;
        if (pushIndex >= history.length)
            pushIndex = 0;
    }

    /**
     * check index on appropriate state
     *
     * @param index to check
     * @return index or max or min value possible
     */

    private int checkIndex(int index) {
        if (index >= history.length) {
            return 0;
        } else if (index < 0) {
            return history.length - 1;
        }
        return index;
    }
}
