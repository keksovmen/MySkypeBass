package com.Abstraction.Audio.Output;

import com.Abstraction.Util.Interfaces.Starting;

/**
 * Handles call notifications
 */

public abstract class AbstractCallNotificator implements Starting, ChangeOutputDevice {

    /**
     * Use for getting particular audio output
     */

    protected volatile int indexOfOutput;

    /**
     * State for thread play call message sound
     */

    protected volatile boolean isWorking;

    /**
     * @param indexOfParticularDevice key in {@link com.Abstraction.Audio.Helper.AudioHelper#getOutputLines()}
     */

    @Override
    public void changeOutputDevice(int indexOfParticularDevice) {
        indexOfOutput = indexOfParticularDevice;
    }

    /**
     * Tries to start new thread that will play call sound
     *
     * @param name with given name
     * @return true if new thread started
     */

    @Override
    public boolean start(String name) {
        if (isWorking)
            return false;
        isWorking = true;

        new Thread(() -> {
            while (isWorking) {
                playCallSound();
            }
        }, name).start();
        return true;
    }

    @Override
    public void close() {
        isWorking = false;
    }

    /**
     * Factory method
     * Override like play call sound until close() is called
     */

    protected abstract void playCallSound();
}
