package com.Abstraction.Audio.Misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractAudioFormatWithMic extends AbstractAudioFormat {

    private final int micCaptureSize;

    public AbstractAudioFormatWithMic(int sampleRate, int sampleSizeInBits, int micCaptureSize) {
        super(sampleRate, sampleSizeInBits);
        this.micCaptureSize = micCaptureSize;
    }

    public AbstractAudioFormatWithMic(int sampleRate, int sampleSizeInBits, int channelsAmount, boolean isSigned, boolean isBigEndian, int micCaptureSize) {
        super(sampleRate, sampleSizeInBits, channelsAmount, isSigned, isBigEndian);
        this.micCaptureSize = micCaptureSize;
    }

    public int getMicCaptureSize() {
        return micCaptureSize;
    }

    @Override
    public String toString() {
        return "Sample rate = " + getSampleRate() + "\n" +
                "Sample size = " + getSampleSizeInBits() +
                "Mic capture size = " + micCaptureSize;
    }

    public static String intoString(AbstractAudioFormatWithMic format) {
        return "Sample rate = " + format.getSampleRate() + "\n" +
                "Sample size = " + format.getSampleSizeInBits() + "\n" +
                "Mic capture size = " + format.micCaptureSize;
    }

    public static AbstractAudioFormatWithMic fromString(String s) {
        Pattern compile = Pattern.compile("Sample rate = (\\d+)\nSample size = (\\d+)\nMic capture size = (\\d+)");
        Matcher matcher = compile.matcher(s);
        if (!matcher.matches())
            return null;
        return new AbstractAudioFormatWithMic(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }
}
