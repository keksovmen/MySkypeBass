package com.Abstraction.Util;

import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Networking.Protocol.ProtocolBitMap;
import com.Abstraction.Util.Collection.Pair;
import com.Abstraction.Util.Cryptographics.Crypto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all necessary representations of data
 */

public class FormatWorker {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private FormatWorker() {
    }

    /**
     * Parse string like this:
     * Sample rate = 01...n
     * Sample size = 01...n
     * retrieve from them digits
     * <p>
     *
     * @param data contain audio format
     * @return parsed audio format
     */

    public static AbstractAudioFormat parseAudioFormat(String data) {
        Pattern pattern = Pattern.compile("Sample rate = (\\d+)\n" +
                "Sample size = (\\d+)");
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find())
            throw new IllegalArgumentException("Given string doesn't contain proper audio format! " + data);
        int sampleRate = Integer.parseInt(matcher.group(1));
        int sampleSize = Integer.parseInt(matcher.group(2));
        return new AbstractAudioFormat(sampleRate, sampleSize);
    }

    public static int parseMicCaptureSize(String data) {
        Pattern compile = Pattern.compile("Mic capture size = (\\d+)");
        Matcher matcher = compile.matcher(data);
        if (!matcher.find())
            throw new IllegalArgumentException("Given string doesn't contain proper mic capture size! " + data);
        return Integer.parseInt(matcher.group(1));
    }

    /**
     * Format for transferring audio data
     *
     * @param audioFormat contain audio format data
     * @return data enough to represent audio format for client
     */

    public static String getAudioFormatAsString(AbstractAudioFormat audioFormat) {
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    public static String getMicCaptureSizeAsString(int micCaptureSize) {
        return "Mic capture size = " + micCaptureSize;
    }

    public static String getFullAudioPackage(AbstractAudioFormat format, int micCaptureSize) {
        return getAudioFormatAsString(format) + "\n" + getMicCaptureSizeAsString(micCaptureSize);
    }

    public static boolean isHostNameCorrect(String hostName) {
        Pattern compile = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
        return hostName.matches(compile.pattern());
    }

    /**
     * Checks if a port in the range (2 unsigned bytes)
     *
     * @param port to check
     * @return true if in the range
     */

    public static boolean portInRange(int port) {
        return 0 <= port && port <= 0xFFFF;
    }

    public static boolean checkZeroLength(String data) {
        return data.length() == 0;
    }

    /**
     * Checks if the string consist only from digits
     *
     * @param string data
     * @return true if correct
     */

    public static boolean verifyOnlyDigits(String string) {
        return string.matches("\\d+");
    }

    /**
     * Give List with numbers from a string
     * To retrieve numbers they should be packed as
     * {@code <$DIGITS>}
     *
     * @param message that contain digits or not
     * @return zero or not length list
     */

    public static List<Pair<Integer, Integer>> retrieveMessageMeta(String message) {
        Pattern pattern = Pattern.compile("<\\$(\\d+)(-(\\d+))?>");
        Matcher matcher = pattern.matcher(message);
        List<Pair<Integer, Integer>> results = new ArrayList<>();

        while (matcher.find()) {
            String trackIndex = matcher.group(1);
            String delayString = matcher.group(3);
            int delay = 0;
            if (delayString != null &&
                    delayString.length() > 0) {
                delay = Integer.valueOf(delayString);
            }
            results.add(new Pair<>(Integer.valueOf(trackIndex), delay));
        }
        return results;
    }

    public static String asMessageMeta(int id) {
        return "<$" + id + ">";
    }

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    public static int getPackageSizeUDP(boolean isCipher, int audioSize) {
        return ProtocolBitMap.PACKET_SIZE + audioSize + (isCipher ? Crypto.STANDARD_PADDING : 0);
    }

}
