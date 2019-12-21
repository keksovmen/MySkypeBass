package com.Abstraction.Audio.Helper;

import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AudioOutputLine;

import java.util.Map;

public abstract class AudioHelper {

    public abstract AudioOutputLine getOutput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException;

    public abstract AudioOutputLine getOutput(int idOfParticularMixer) throws AudioLineException;

    public abstract void playResourceFile(int idOfParticularMixer, int trackId);

    public abstract int getDefaultForOutput();

    public abstract int getDefaultForInput();

    public abstract AudioInputLine getInput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException;

    public abstract AudioInputLine getInput(int idOfParticularMixer) throws AudioLineException;

    public abstract int getMicCaptureSize();

    public abstract Map<Integer, String> getOutputLines();

    public abstract Map<Integer, String> getInputLines();

    public abstract AbstractAudioFormat getAudioFormat();

    public abstract boolean isFormatSupported(String formatAndCaptureSize);
}
