package com.Abstraction.Audio.Output;

import com.Abstraction.Model.Updater;
import com.Abstraction.Util.Interfaces.Initialising;

public interface Playable extends Initialising, Updater {

    /**
     * Play audio for given dude
     *
     * @param who  the dude
     * @param data audio
     */

    void playSound(int who, byte[] data);

    /**
     * Play random notification
     */

    void playMessage();

    /**
     * Play particular notification
     *
     * @param track id
     */

    void playMessage(int track);

    /**
     * Play particular track with delay in millis
     *
     * @param track id
     * @param delay time in millis
     */

    void playMessage(int track, int delay);

    /**
     * Start playing call sound
     */

    void playCall();

    /**
     * Stop playing call sound
     */

    void stopCall();

    /**
     * release all underlying resources
     */

    void close();
}
