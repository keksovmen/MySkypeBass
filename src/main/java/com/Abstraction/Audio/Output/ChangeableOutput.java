package com.Abstraction.Audio.Output;

/**
 * Changeable interface for output lines
 */

public interface ChangeableOutput extends ChangeOutputDevice {

    /**
     * @param who        id of underlying output line
     * @param percentage from 0 - 100
     */

    void changeVolume(int who, int percentage);

}
