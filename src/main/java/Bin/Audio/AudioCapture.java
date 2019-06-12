package Bin.Audio;

import java.util.function.Consumer;

/**
 * Uses as wrapper for microphone actions
 * Package private uses
 */

class AudioCapture {

    /**
     * Indicate is a thread already started or not
     */

    private volatile boolean started;

    /**
     * For mute purposes
     */

    private volatile boolean mute;

    /**
     * Define while loop for capturing sound
     */

    private volatile boolean work;

    /**
     * Need for bass boost purposes
     */

    private volatile double multiplier = 1d;

    /**
     * Changes mute state to an opposite
     * notify thread in case of it was stopped before
     *
     * @return actual value
     */

    synchronized boolean mute() {
        mute = !mute;
        notify();
        return mute;
    }

    /**
     * All data processing with data must be here
     *
     * @param sendSound action for what to do with the sound
     *                  just send to a server
     * @return false if can't capture audio, true otherwise
     */

    private boolean process(final Consumer<byte[]> sendSound) {
        byte[] audio = AudioClient.getInstance().captureAudio();
        if (audio == null) {
            return false;
        }
        // really shit bass boost
        if (multiplier != 1f) {
            for (int i = 0; i < audio.length; i++) {
                audio[i] = (byte) (audio[i] * -multiplier);
            }
        }
        sendSound.accept(audio);
        return true;
    }

    /**
     * STARTS a NEW THREAD
     * If mute is false the thread will wait
     * turns all parameters before start of the new thread
     *
     * @param sendSound actions for delivering data
     */

    void start(final Consumer<byte[]> sendSound) {
        if (started) {
            return;
        }
        started = true;
        work = true;
        mute = false;
        new Thread(() -> {
            while (work) {
                synchronized (this) {
                    if (mute) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!process(sendSound)) {
                    close();
                }
            }
            started = false;
        }, "Client capture").start();
    }

    /**
     * Shut down the thread
     * must be sync because of notify
     */

    synchronized void close() {
        work = false;
        notify();
    }

    /**
     * Creates setter for the multiplier field
     *
     * @return ready to use action
     */

    Consumer<Double> changeMultiplier() {
        return aDouble -> {
            if (aDouble < 1d) {
                multiplier = 1d;
            } else {
                multiplier = aDouble;
            }
        };
    }

}
