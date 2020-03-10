package com.Abstraction.Audio.Output;

import com.Abstraction.Audio.Misc.AbstractLine;

/**
 * Represents output line {@code ->} speaker headset etc.
 */

public interface AudioOutputLine extends AbstractLine {

    /**
     * Check if can change volume
     *
     * @return true if can
     */

    boolean isVolumeChangeSupport();

    /**
     * Changes volume lvl to given @percentage
     *
     * @param percentage from 0 to 100% desired volume lvl
     */

    void setVolume(int percentage);

    /**
     * Make underlying speaker produce given sound
     *
     * @param buffer   to speak
     * @param offset from 0 to length
     * @param length amount of bytes to read
     * @return amount of bytes actually read
     */

    int writeNonBlocking(byte[] buffer, int offset, int length);

    /**
     * Short cut for writing buffer
     *
     * @param buffer to speak
     * @return amount ob bytes actually read
     */

    default int writeNonBlocking(byte[] buffer) {
        return writeNonBlocking(buffer, 0, buffer.length);
    }

    /**
     * Blocking write to output device
     *
     * @param buffer   to write
     * @param offset in given buffer
     * @param length of buffer to put in
     * @return actual amount of bytes whiten
     */

    int writeBlocking(byte[] buffer, int offset, int length);

    /**
     * Short cut for blocking write
     *
     * @param buffer to write
     * @return actual amount of bytes whiten
     */

    default int writeBlocking(byte[] buffer) {
        return writeBlocking(buffer, 0, buffer.length);
    }

}
