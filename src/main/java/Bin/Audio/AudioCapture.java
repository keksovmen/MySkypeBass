package Bin.Audio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
     * Action for what to do with the sound
     * just sendSound to a server
     */

    private Consumer<byte[]> sendSound;

    /**
     * How you get audio to be send
     */

    private Supplier<byte[]> getAudioFromMic;

    /**
     * For running a sendSound action in different thread
     */

    private ExecutorService service;

    /**
     * Defines how much audio portion you can put in
     * sendSound action.
     * If networking is slow it will affect like teared audio
     * Needs for one purpose if networking work slow
     * sound is captured from the mic will not grow
     * more than QUEUE_SIZE * AudioClient.CAPTURE_SIZE_MAIN bytes
     */

    private static final int QUEUE_SIZE = 15;

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
     * Send sound in different thread for removing glitches in audio
     *
     * @return false if can't capture audio, true otherwise
     */

    private boolean process() {
        byte[] audio = getAudioFromMic.get();
        if (audio == null) {
            return false;
        }
        // really shit bass boost
        if (multiplier != 1f) {
            for (int i = 0; i < audio.length; i++) {
                audio[i] = (byte) (audio[i] * -multiplier);
            }
        }
        service.execute(() -> sendSound.accept(audio));
        return true;
    }

    /**
     * STARTS a NEW THREAD
     * If mute is false the thread will wait
     * turns all parameters before start of the new thread
     *
     * @param sendSound actions for delivering data
     */

    void start(final Consumer<byte[]> sendSound, final Supplier<byte[]> getSound) {
        if (started) {
            return;
        }
        this.sendSound = sendSound;
        getAudioFromMic = getSound;
        onStartCapturing();
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
                if (!process()) {
                    close();
                }
            }
            onStopCapturing();
        }, "Client capture").start();
    }

    /**
     * Sets all required fields for proper audio capturing
     */

    private void onStartCapturing() {
        started = true;
        work = true;
        mute = false;
        service = new ThreadPoolExecutor(0, 1, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE));
    }

    /**
     * Sets all necessary fields for stopping the capturing
     */

    private void onStopCapturing() {
        started = false;
        service.shutdownNow();
        service = null;
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
