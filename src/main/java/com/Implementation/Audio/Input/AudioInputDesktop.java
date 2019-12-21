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
    public int readNonBlocking(byte[] buffer, int offset, int length) {
        int available = line.available();
        return line.read(buffer, offset, available < length ? available : length);
    }

    @Override
    public int readBlocking(byte[] buffer, int offset, int length) {
        return line.read(buffer, offset, length);
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
