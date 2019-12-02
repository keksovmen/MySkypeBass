package com.Audio.Helpers;

import com.Util.FormatWorker;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleHelper extends AudioHelper {

    private final List<Mixer.Info> sourceLines;
    private final List<Mixer.Info> targetLines;

    private AudioFormat audioFormat;
    private int micCaptureSize;

    public SimpleHelper() {
        sourceLines = new ArrayList<>();
        targetLines = new ArrayList<>();
        audioFormat = null;
        micCaptureSize = -1;
    }

    @Override
    public SourceDataLine getOutput(Mixer.Info info, AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat, info);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        return sourceDataLine;
    }

    @Override
    public SourceDataLine getOutput(Mixer.Info info) throws LineUnavailableException {
        return getOutput(info, audioFormat);
    }

    @Override
    public SourceDataLine getOutputForFile(Mixer.Info info, BufferedInputStream file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
        return getOutput(info, audioFileFormat.getFormat());
    }

    @Override
    public Mixer.Info getDefaultForOutput() {
        return sourceLines.get(0);
    }

    @Override
    public Mixer.Info getDefaultForInput() {
        return targetLines.get(0);
    }

    @Override
    public TargetDataLine getInput(Mixer.Info mixer, AudioFormat audioFormat) throws LineUnavailableException {
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat, mixer);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        return targetDataLine;
    }

    @Override
    public TargetDataLine getInput(Mixer.Info mixer) throws LineUnavailableException {
        return getInput(mixer, audioFormat);
    }

    @Override
    public int getMicCaptureSize() {
        return micCaptureSize;
    }

//    @Override
//    public boolean setAudioFormat(AudioFormat format, int micSize) {
//        return false;
//    }

    @Override
    public List<Mixer.Info> getSourceLines() {
        return sourceLines;
    }

    @Override
    public List<Mixer.Info> getTargetLines() {
        return targetLines;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public boolean isFormatSupported(String formatAndCaptureSize) {
        AudioFormat format = FormatWorker.parseAudioFormat(formatAndCaptureSize);
        int micSize = FormatWorker.parseMicCaptureSize(formatAndCaptureSize);
        sourceLines.clear();
        targetLines.clear();
        boolean result = isLineExist(format, SourceDataLine.class) &&
                isLineExist(format, TargetDataLine.class);
        if (result) {
            this.audioFormat = format;
            micCaptureSize = micSize;
        }
        return result;
    }

    private boolean isLineExist(AudioFormat format, Class<? extends DataLine> dataLineClass) {
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
}
