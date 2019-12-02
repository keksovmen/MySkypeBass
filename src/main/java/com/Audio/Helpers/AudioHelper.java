package com.Audio.Helpers;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

public abstract class AudioHelper {

    public abstract SourceDataLine getOutput(Mixer.Info info, AudioFormat audioFormat) throws LineUnavailableException;

    public abstract SourceDataLine getOutput(Mixer.Info info) throws LineUnavailableException;

    public abstract SourceDataLine getOutputForFile(Mixer.Info info, BufferedInputStream file) throws IOException, UnsupportedAudioFileException, LineUnavailableException;

    public abstract Mixer.Info getDefaultForOutput();

    public abstract Mixer.Info getDefaultForInput();

    public abstract TargetDataLine getInput(Mixer.Info mixer, AudioFormat audioFormat) throws LineUnavailableException;

    public abstract TargetDataLine getInput(Mixer.Info mixer) throws LineUnavailableException;

    public abstract int getMicCaptureSize();

//    public abstract boolean setAudioFormat(AudioFormat format, int micSize);

    public abstract List<Mixer.Info> getSourceLines();

    public abstract List<Mixer.Info> getTargetLines();

    public abstract AudioFormat getAudioFormat();

    public abstract boolean isFormatSupported(String formatAndCaptureSize);
}
