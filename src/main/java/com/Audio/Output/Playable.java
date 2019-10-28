package com.Audio.Output;

import com.Model.Updater;
import com.Util.Interfaces.Initialising;

public interface Playable extends Initialising, Updater {

    void playSound(int from, byte[] data);

    void playMessage();

    void playMessage(int track);

    void playMessage(int track, int delay);

    void playCall();

    void stopCall();

    void close();
}
