package com.Abstraction.Audio.Misc;

import com.Abstraction.Audio.Misc.AbstractAudioFormat;

import java.io.Closeable;

/**
 * Represent abstract Line for android and desktop application
 * Some sort of Adapter
 */

public interface AbstractLine extends Closeable {

    /**
     * @return currently loaded buffer
     */

    int available();

    /**
     * To clear buffer, and send data to a receiver
     * Not blocking current thread
     */

    void flush();

    /**
     * Check if line is open -> ready to produce results through read or write
     *
     * @return ready state
     */

    boolean isOpen();

    /**
     * If line is running it means it's in process to produce or consume data
     *
     * @return line current state
     */

    boolean isRunning();

    /**
     * Current thread will wait until underlying buffer empties
     */

    void drain();


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
