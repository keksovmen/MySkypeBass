package com.Abstraction.Audio.Misc;

import java.io.Closeable;

/**
 * Represent abstract Line for android and desktop application
 * Some sort of Adapter
 * <p>
 * Invariants:
 * 1 - Must be open until close() called
 * 2 - After close you must not call write or read,
 * Better solution to make null reference after close()
 * 3 - Must guarantee blocking and non blocking read and write
 */

public interface AbstractLine extends Closeable {


    /**
     * @return format under which this line is open
     */

    AbstractAudioFormat getFormat();

    /**
     * Release underlying resources
     */

    @Override
    void close();
}
