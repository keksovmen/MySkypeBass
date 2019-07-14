package Bin.Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Uses as wrapper for microphone actions
 * Package private uses
 */

class AudioCapture {

    /**
     * Line from you take audio
     */

    private TargetDataLine mic;

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
     * For running a sendSound action in different thread
     */

    private ExecutorService executor;

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
     */

    private void readAndProcess() {
        byte[] audio = new byte[AudioClient.getInstance().getMicCaptureSize()];
        mic.read(audio, 0, audio.length);
        // really shit bass boost
        if (multiplier != 1d) {
            for (int i = 0; i < audio.length; i++) {
                audio[i] = (byte) (audio[i] * multiplier);
            }
        }
        executor.execute(() -> sendSound.accept(audio));
    }

    /**
     * STARTS a NEW THREAD
     * If mute is false the thread will wait
     * turns all parameters before start of the new thread
     *
     * @param sendSound actions for delivering data
     */

    void start(final Consumer<byte[]> sendSound, AudioFormat audioFormat) {//think about sync
        synchronized (this) {
            if (started) {  //cancel if already started
                return;
            }
            started = true;
        }
        this.sendSound = sendSound;
        try {
            onStartCapturing(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
        new Thread(() -> {
            while (work) {
                synchronized (this) {
                    if (mute) {
                        try {
                            wait();//could make it with semaphore
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                readAndProcess();
            }
            onStopCapturing();
        }, "Client capture").start();

    }

    /**
     * Sets all required fields for proper audio capturing
     */

    private void onStartCapturing(AudioFormat audioFormat) throws LineUnavailableException {
        mic = AudioLineProvider.obtainAndOpenTarget(audioFormat);
        executor = new ThreadPoolExecutor(0, 1, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE));
        work = true;
        mute = false;
    }

    /**
     * Sets all necessary fields for stopping the capturing
     */

    private void onStopCapturing() {
        executor.shutdownNow();
        executor = null;
        multiplier = 1d;
        mic.close();
        started = false;
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
