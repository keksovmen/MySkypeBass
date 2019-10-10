package com.Util;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
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
     *      Sample rate = 01...n
     *      Sample size = 01...n
     * retrieve from them digits
     * <p>
     *
     * @param data contain audio format
     * @return parsed audio format
     */

    public static AudioFormat parseAudioFormat(String data) {
        Pattern pattern = Pattern.compile("Sample rate = (\\d+)\n" +
                "Sample size = (\\d+)");
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find())
            throw new IllegalArgumentException("Given string doesn't contain proper audio format! " + data);
        int sampleRate = Integer.parseInt(matcher.group(1));
        int sampleSize = Integer.parseInt(matcher.group(2));
        return new AudioFormat(sampleRate, sampleSize, 1, true, true);
    }

    public static int parseMicCaptureSize(String data){
        Pattern compile = Pattern.compile("Mic capture size = (\\d+)");
        Matcher matcher = compile.matcher(data);
        if (!matcher.find())
            throw new IllegalArgumentException("Given string doesn't contain proper mic capture size! " + data);
        return Integer.parseInt(matcher.group(1));
    }

    /**
     * Format for transferring audio data
     *
     * @return data enough to represent audio format for client
     */

    public static String getAudioFormatAsString(AudioFormat audioFormat) {
        return "Sample rate = " + (int) audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    public static String getMicCaptureSizeAsString(int micCaptureSize){
        return "Mic capture size = " + micCaptureSize;
    }

    public static String getFullAudioPackage(AudioFormat format, int micCaptureSize){
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
        return 0 < port && port < 0xFFFF;
    }

    public static boolean checkZeroLength(String data) {
        return data.length() == 0;
    }

    /**
     * Needs for JFormattedTextField NumberFormatter.getFormat()
     * not appropriate because of @see AudioFormatStats at the end
     *
     * @return formatter for only digits
     */

    public static JFormattedTextField.AbstractFormatterFactory getFormatter() {
        return new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                return new JFormattedTextField.AbstractFormatter() {
                    private final int MAX_LENGTH_AS_STRING =
                            String.valueOf(Integer.MAX_VALUE).length() - 1; //-1 just in case ov largest value

                    @Override
                    public Object stringToValue(String text) {
                        String result = text.trim();
                        result = result.replaceAll("\\D", "");
                        int difference = result.length() - MAX_LENGTH_AS_STRING;
                        if (0 < difference)
                            result = result.substring(0, result.length() - difference);
                        return result.equals("") ? "" : Integer.parseInt(result);
                    }

                    @Override
                    public String valueToString(Object value) {
                        return String.valueOf(value);
                    }
                };
            }
        };
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
     * <$DIGITS>
     *
     * @param message that contain digits or not
     * @return zero or not length list
     */

    public static List<Integer> retrieveMessageMeta(String message) {
        Pattern pattern = Pattern.compile("<\\$(\\d+)?>");
        Matcher matcher = pattern.matcher(message);
        List<Integer> results = new ArrayList<>();

        while (matcher.find()) {
            String rawData = matcher.group(1);
            results.add(Integer.valueOf(rawData));
        }
        return results;
    }

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

}
