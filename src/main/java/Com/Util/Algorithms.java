package Com.Util;

import java.util.Comparator;
import java.util.function.BiFunction;

public class Algorithms {

    private Algorithms() {
    }

    /**
     * Simple linear search for a given value
     *
     * @param array where to search
     * @param item  the value
     * @param <T>   template class
     * @return true if found
     */

    public static <T> boolean search(T[] array, T item) {
        for (T t : array) {
            if (t.equals(item))
                return true;
        }
        return false;
    }

    /**
     * Simple linear search for a given value
     *
     * @param array      where to search
     * @param item       the value
     * @param comparator used to find identical but different classes thing
     * @param <T>        template class for array
     * @param <Y>        template class for finding thing
     * @return T or null
     */

    public static <T, Y> T search(T[] array, Y item, BiFunction<T, Y, Boolean> comparator) {
        for (T t : array) {
            if (comparator.apply(t, item))
                return t;
        }
        return null;
    }

    /**
     * Simple geometric search for identities
     *
     * @param array where to search
     * @param <T>   template class
     * @return true if find identities
     */

    public static <T> boolean searchForIdentities(T[] array) {
        for (int i = 0; i < array.length; i++) {
            T tmp = array[i];
            for (int k = 0; k < array.length; k++) {
                if (i == k)
                    continue;
                if (array[k].equals(tmp))
                    return true;
            }
        }
        return false;
    }

    /**
     * Simple geometric search for identities
     *
     * @param comparator used instead of equals()
     * @param array      where to search
     * @param <T>        template class
     * @return true if find identities
     */

    public static <T> boolean searchForIdentities(T[] array, Comparator<T> comparator) {
        for (int i = 0; i < array.length; i++) {
            T tmp = array[i];
            for (int k = 0; k < array.length; k++) {
                if (i == k)
                    continue;

                if (comparator.compare(array[k], tmp) == 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Combine two bytes in to an int
     *
     * @param highByte  will be high
     * @param lowerByte will be low
     * @return unsigned int
     */

    public static int combineTwoBytes(byte highByte, byte lowerByte) {
        int result = (highByte & 0xff) << 8;
        result |= lowerByte & 0xff;
        result &= 0xffff;
        return result;
    }
}
