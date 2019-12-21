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

    int readNonBlocking(byte[] buffer, int offset, int length);

    /**
     * Short cut for read
     *
     * @param buffer to fill with data
     * @return actual amount of reade bytes
     */

    default int readNonBlocking(byte[] buffer) {
        return readNonBlocking(buffer, 0, buffer.length);
    }

    /**
     * Blocking read
     *
     * @param buffer to fill
     * @param offset buffer start position
     * @param length amount of bytes to read
     * @return actual number of read bytes from underlying device buffer
     */

    int readBlocking(byte[] buffer, int offset, int length);

    /**
     * Blocking read for full buffer
     *
     * @param buffer to fill
     * @return actual number of read bytes from underlying device buffer
     */

    default int readBlocking(byte[] buffer) {
        return readBlocking(buffer, 0, buffer.length);
    }
}
