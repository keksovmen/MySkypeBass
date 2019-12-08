package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Abstraction.Util.Algorithms;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class AudioOutputDesktop implements AudioOutputLine {

    private final SourceDataLine line;
    private final AbstractAudioFormat format;

    public AudioOutputDesktop(SourceDataLine line, AbstractAudioFormat format) {
        this.line = line;
        this.format = format;
    }

    @Override
    public boolean isVolumeChangeSupport() {
        return line.isControlSupported(FloatControl.Type.MASTER_GAIN);
    }

    @Override
    public void setVolume(int percentage) {
        FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        int minimum = (int) Math.ceil(control.getMinimum());
        int maximum = (int) Math.floor(control.getMaximum());
        int mean = Algorithms.findPercentage(minimum, maximum, percentage);
        control.setValue(mean);
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        return line.write(data, offset, length);
    }

    @Override
    public int available() {
        return line.available();
    }

    @Override
    public void flush() {
        line.flush();
    }

    @Override
    public boolean isOpen() {
        return line.isOpen();
    }

    @Override
    public boolean isRunning() {
        return line.isRunning();
    }

    @Override
    public void drain() {
        line.drain();
    }

    @Override
    public AbstractAudioFormat getFormat() {
        return format;
    }

    @Override
    public void close() {
        line.close();
    }
}
