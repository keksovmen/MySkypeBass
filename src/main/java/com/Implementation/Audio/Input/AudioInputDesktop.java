package com.Implementation.Audio.Input;

import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;

import javax.sound.sampled.TargetDataLine;

public class AudioInputDesktop implements AudioInputLine {

    private final TargetDataLine line;
    private final AbstractAudioFormat format;

    public AudioInputDesktop(TargetDataLine line, AbstractAudioFormat format) {
        this.line = line;
        this.format = format;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        return line.read(buffer, offset, length);
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
