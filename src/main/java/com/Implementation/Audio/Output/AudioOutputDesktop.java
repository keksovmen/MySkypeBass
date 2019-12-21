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
    public int writeNonBlocking(byte[] buffer, int offset, int length) {
        if (line.available() < buffer.length)
            line.flush();

        return line.write(buffer, 0, buffer.length);
    }

    @Override
    public int writeBlocking(byte[] buffer, int offset, int length) {
        return line.write(buffer, offset, length);
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
