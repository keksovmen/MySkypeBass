package com.Audio.Output;

import javax.sound.sampled.SourceDataLine;

public abstract class Magnitafon {

    /**
     * Plays data without blocking the thread
     *
     * @param output where to play
     * @param sound data to play
     */

    void playData(SourceDataLine output, byte[] sound){
        if (!checkLine(output))
            throw new IllegalStateException("Output line is not open or running");

        if (output.available() < sound.length)
            output.flush();

        output.write(sound, 0, sound.length);
    }

    /**
     * Plays data with blocking
     * Useful for message notification and Calls
     *
     * @param output where to play
     * @param sound data to play
     */

    void playFullData(SourceDataLine output, byte[] sound){
        if (!checkLine(output))
            throw new IllegalStateException("Output line is not open or running");

        output.write(sound, 0 , sound.length);
    }

    /**
     * Check line for open and running state
     *
     * @param output line to check
     * @return true if ready to use
     */

    private static boolean checkLine(SourceDataLine output){
        return output.isOpen() || output.isRunning();
    }
}
