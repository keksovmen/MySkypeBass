package com.Abstraction.Audio.Output;

import com.Abstraction.Util.Interfaces.Starting;

public abstract class AbstractCallNotificator implements Starting, ChangeOutput {

    /**
     * Use for getting particular audio output
     */

    protected volatile int indexOfOutput;
    protected volatile boolean isWorking;

    @Override
    public void changeOutput(int indexOfParticularMixer) {
        indexOfOutput = indexOfParticularMixer;
    }

    @Override
    public boolean start(String name) {
        if (isWorking)
            return false;
        isWorking = true;

        new Thread(() -> {
            while (isWorking){
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
