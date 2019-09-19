package Com.Util;

import javax.sound.sampled.AudioFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all necessary representations of data
 */

public class FormatWorker {

    private FormatWorker(){}

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

    public static boolean isHostNameCorrect(String hostName){
        Pattern compile = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
        return hostName.matches(compile.pattern());
    }
}
