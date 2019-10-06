package Com.Util;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all necessary representations of data
 */

public class FormatWorker {

    private FormatWorker() {
    }

    /**
     * Parse string like this Sample rate = 01...n\nSample size = 01....n
     * retrieve from them digits
     * <p>
     * MUST MATCH getAudioFormat() !
     *
     * @param data got from the server
     * @return default audio format
     */

    public static AudioFormat parseAudioFormat(String data) {
        String[] strings = data.split("\n");
        Pattern pattern = Pattern.compile("\\d+?\\b");
        Matcher matcher = pattern.matcher(strings[0]);
        matcher.find();
        int sampleRate = Integer.valueOf(matcher.group());
        matcher = pattern.matcher(strings[1]);
        matcher.find();
        int sampleSize = Integer.valueOf(matcher.group());
        return new AudioFormat(sampleRate, sampleSize, 1, true, true);
    }

    /**
     * Format for transferring audio data
     *
     * @return data enough to represent audio format for client
     */

    public static String getAudioFormat(AudioFormat audioFormat) {
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    public static boolean isHostNameCorrect(String hostName) {
        Pattern compile = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
        return hostName.matches(compile.pattern());
    }

    public static boolean verifyPortFormat(String port) {
        return port.matches("\\d+?");
    }

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

    public static boolean verifyOnlyDigits(String string) {
        return string.matches("\\d+");
    }

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

    public static String getTime(){
        Calendar calendar = Calendar.getInstance();
        return Resources.dateFormat.format(calendar.getTime());
    }
    //    /**
//     * Regular expression is a POWER
//     * mean ((1-3 digits).) 3 times and then just (1-3 digits)
//     *
//     * @param ip to verify
//     * @return true if has an appropriate format
//     */
//
//    public static boolean verifyIp(String ip) {
//        String digitAndDotThenDigits = "((\\d){1,3}\\.){3}(\\d{1,3})";
//        return Pattern.compile(digitAndDotThenDigits).matcher(ip.trim()).matches();
//    }
}
