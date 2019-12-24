package com.Abstraction.Audio.Helper;

import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AudioOutputLine;

import java.util.Map;

/**
 * Provides platform dependent resources and methods
 */

public abstract class AudioHelper {

    /**
     * Default format received  from server on successful connection
     */

    private AbstractAudioFormat defaultFormat;

    /**
     * Obtains and opens output line for specific audio format
     *
     * @param idOfParticularMixer if your platform provide more than default device, id of particular
     * @param audioFormat         contains format specific data
     * @return already opened output line, ready to be written in
     * @throws AudioLineException convert platform specific exception to it
     */

    public abstract AudioOutputLine getOutput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException;

    /**
     * Obtains and opens output line for default server format
     *
     * @param idOfParticularMixer if your platform provide more than default device, id of particular
     * @return already opened output line, ready to be written in
     * @throws AudioLineException convert platform specific exception to it
     */

    public abstract AudioOutputLine getOutput(int idOfParticularMixer) throws AudioLineException;

    /**
     * Plays a sound resource in current thread
     *
     * @param idOfParticularMixer output device id
     * @param trackId             {@link com.Abstraction.Util.Resources.AbstractResources getNotificationsTracks()}
     */

    public abstract void playResourceFile(int idOfParticularMixer, int trackId);

    /**
     * @return id of default output device
     */

    public abstract int getDefaultForOutput();

    /**
     * @return id of default input device
     */

    public abstract int getDefaultForInput();

    /**
     * Obtains and opens platform specific input line
     *
     * @param idOfParticularMixer if your platform provide more than default device, id of particular
     * @param audioFormat         contains format specific data
     * @return opened and ready to read input device
     * @throws AudioLineException convert platform specific exception to it
     */

    public abstract AudioInputLine getInput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException;

    /**
     * Obtains and opens platform default input line
     *
     * @param idOfParticularMixer if your platform provide more than default device, id of particular
     * @return opened and ready to read input device
     * @throws AudioLineException convert platform specific exception to it
     */

    public abstract AudioInputLine getInput(int idOfParticularMixer) throws AudioLineException;

    /**
     * Should receive from a server
     *
     * @return amount of bytes to capture for 1 time from mic
     */

    public abstract int getMicCaptureSize();

    /**
     * @return map with integer = idOfParticularMixer, String description of given line
     */

    public abstract Map<Integer, String> getOutputLines();

    /**
     * @return map with integer = idOfParticularMixer, String description of given line
     */

    public abstract Map<Integer, String> getInputLines();

    /**
     * Must check if format received from a server is appropriate, and if so to {@link #setDefaultFormat(AbstractAudioFormat)}
     *
     * @param formatAndCaptureSize see {@link com.Abstraction.Util.FormatWorker#getFullAudioPackage(AbstractAudioFormat, int)}
     * @return true if can work with given format false otherwise
     */

    public abstract boolean isFormatSupported(String formatAndCaptureSize);

    /**
     * @return audio format received from a server
     */

    public AbstractAudioFormat getDefaultAudioFormat() {
        return defaultFormat;
    }

    /**
     * @param format to set as default {@link #isFormatSupported(String)}
     */

    protected void setDefaultFormat(AbstractAudioFormat format) {
        defaultFormat = format;
    }
}
