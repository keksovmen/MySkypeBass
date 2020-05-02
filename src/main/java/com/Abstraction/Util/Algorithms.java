package com.Abstraction.Util;

import com.Abstraction.Util.Resources.Resources;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return result;
    }

    /**
     * Find value corresponding to percentage
     *
     * @param min        possible value
     * @param max        possible value
     * @param percentage between 0 - 100
     * @return value between min and max equal to the percentage
     */

    public static int findPercentage(int min, int max, int percentage) {
        if (percentage < 0 || 100 < percentage)
            throw new IllegalArgumentException("Percentage can't be more than 100 or less than 0! " + percentage);
        int difference = max - min;
        float percent = difference / 100f;
        float value = percent * percentage;
        return (int) (min + value);
    }

    /**
     * Convert each byte into unsigned int {@code ->} to string {@code ->} append in line with spaces between
     *
     * @param data to convert
     * @return string such as "(\\d )*"
     */

    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder(data.length * 3);
        for (byte b : data) {
            stringBuilder.append(String.valueOf(Byte.toUnsignedInt(b)));
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * Convert String from {@link #byteArrayToString(byte[])} to original byte array
     *
     * @param data to convert
     * @return original byte array
     */

    public static byte[] stringToByteArray(String data) {
        Pattern pattern = Pattern.compile("(\\d{1,3}) ");
        Matcher matcher = pattern.matcher(data);

        List<Byte> byteList = new ArrayList<>();
        while (matcher.find()) {
            byteList.add(Integer.valueOf(matcher.group(1)).byteValue());
        }

        return listOfBytesToPrimitiveByte(byteList);
    }

    private static byte[] listOfBytesToPrimitiveByte(List<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static void closeSocketThatCouldBeClosed(Closeable socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            //already closed
        }
    }

    public static long minToMillis(int minutes){
        return minutes * 60 * 1000;
    }

    /**
     *
     * @return duration of audio unit in MICRO seconds
     */

    public static int calculatePartOfAudioUnitDuration(){
        double durationInMillis = 1000d / Resources.getInstance().getMiCaptureSizeDivider();
        return (int) ((durationInMillis * 1000) / Resources.getInstance().getUnitFrameDivider());
    }
}
