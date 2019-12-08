package com.Abstraction.Audio.Input;

import com.Abstraction.Audio.Misc.AbstractLine;

/**
 * Represents input audio line
 */

public interface AudioInputLine extends AbstractLine {

    /**
     * Fill given buffer with mic captured data
     *
     * @param buffer where to put data
     * @param offset start position
     * @param length amount of byte to put
     * @return actual amount of reade bytes
     */

    int read(byte[] buffer, int offset, int length);

    /**
     * Short cut for read
     *
     * @param buffer to fill with data
     * @return actual amount of reade bytes
     */

    default int read(byte[] buffer) {
        return read(buffer, 0, buffer.length);
    }
}
