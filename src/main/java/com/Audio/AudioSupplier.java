package com.Audio;

import com.Networking.Protocol.ProtocolBitMap;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioSupplier {

    private static final List<Mixer.Info> sourceLines = new ArrayList<>();
    private static final List<Mixer.Info> targetLines = new ArrayList<>();

    private static AudioFormat audioFormat = null;
    private static int MIC_CAPTURE_SIZE = -1; // will be initialised after equal to half a second


    private AudioSupplier() {

    }

    public static SourceDataLine getOutput(Mixer.Info info, AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat, info);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        return sourceDataLine;
    }

    public static SourceDataLine getOutput(Mixer.Info info) throws LineUnavailableException {
        return getOutput(info, audioFormat);
    }

    public static SourceDataLine getOutputForFile(Mixer.Info info, BufferedInputStream file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
        return getOutput(info, audioFileFormat.getFormat());
    }

    public static Mixer.Info getDefaultForOutput() {
        return sourceLines.get(0);
    }

    public static Mixer.Info getDefaultForInput() {
        return targetLines.get(0);
    }

    public static TargetDataLine getInput(Mixer.Info mixer, AudioFormat audioFormat) throws LineUnavailableException {
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat, mixer);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        return targetDataLine;
    }

    public static TargetDataLine getInput(Mixer.Info mixer) throws LineUnavailableException {
        return getInput(mixer, audioFormat);
    }

    public static int getMicCaptureSize() {
        return MIC_CAPTURE_SIZE;
    }

    public static boolean setAudioFormat(AudioFormat format) {
        sourceLines.clear();
        targetLines.clear();
        boolean result = isLineExist(format, SourceDataLine.class) &&
                isLineExist(format, TargetDataLine.class);
        if (result) {
            audioFormat = format;
            MIC_CAPTURE_SIZE = calculateMicCaptureSize(audioFormat);
        }
        return result;
    }

    public static List<Mixer.Info> getSourceLines() {
        return sourceLines;
    }

    public static List<Mixer.Info> getTargetLines() {
        return targetLines;
    }

    public static AudioFormat getAudioFormat() {
        return audioFormat;
    }

    private static boolean isLineExist(AudioFormat format, Class<? extends DataLine> dataLineClass) {
        boolean result = false;
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(new DataLine.Info(dataLineClass, format))) {
                if (dataLineClass.equals(SourceDataLine.class)) {
                    sourceLines.add(info);
                } else if (dataLineClass.equals(TargetDataLine.class)) {
                    targetLines.add(info);
                }
                result = true;
            }
        }
        return result;
    }

    private static int calculateMicCaptureSize(AudioFormat format) {
        int i = (int) ((format.getSampleRate() / 8) *
                (format.getSampleSizeInBits() / 8));
        i = i - i % (format.getSampleSizeInBits() / 8);
        if (i > ProtocolBitMap.MAX_VALUE) {
            System.exit(2);
            throw new IllegalArgumentException("Capture size can't be more than protocol can handle!\n" +
                    i + " <= " + ProtocolBitMap.MAX_VALUE);
        }
        return i;
    }
}
