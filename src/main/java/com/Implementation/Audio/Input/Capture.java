package com.Implementation.Audio.Input;

import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Util.Algorithms;

/**
 * Represents a microphone
 * Can be muted
 * And bass boosted
 */

public class Capture extends AbstractMicrophone {

    private static final float MIN_BASS_LVL = 1f;
    private static final float MAX_BASS_LVL = 20f;

    private volatile float bassLvl = 1f;


    public Capture(ButtonsHandler helpHandlerPredecessor) {
        super(helpHandlerPredecessor);
    }


    @Override
    public void IncreaseBass(int percentage) {
        bassLvl = Algorithms.findPercentage((int) MIN_BASS_LVL, (int) MAX_BASS_LVL, percentage);
    }


    @Override
    public synchronized void close() {
        super.close();
        bassLvl = 1f;
    }

    @Override
    protected byte[] bassBoost(byte[] data) {
        if (bassLvl == 1f)
            return data;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] * bassLvl);
        }
        return data;
    }
}
