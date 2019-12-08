package com.Abstraction.Audio.Output;

import com.Abstraction.Audio.Misc.AbstractLine;

/**
 * Represents output line -> speaker headset etc.
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
     * @param data   to speak
     * @param offset from 0 to length
     * @param length amount of bytes to read
     * @return amount of bytes actually read
     */

    int write(byte[] data, int offset, int length);

    /**
     * Short cut for writing data
     *
     * @param data to speak
     * @return amount ob bytes actually read
     */

    default int write(byte[] data) {
        return write(data, 0, data.length);
    }

}
