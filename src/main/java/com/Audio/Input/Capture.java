package com.Audio.Input;

import com.Audio.AudioSupplier;
import com.Client.ButtonsHandler;
import com.Pipeline.BUTTONS;
import com.Util.Algorithms;
import com.Util.Collection.ArrayBlockingQueueWithWait;
import com.Util.Resources;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents a microphone
 * Can be muted
 * And bass boosted
 */

public class Capture implements DefaultMic, ChangeableInput {

    private static final float MIN_BASS_LVL = 1f;
    private static final float MAX_BASS_LVL = 20f;

    private final ButtonsHandler helpHandlerPredecessor;

    private volatile TargetDataLine mic = null;
    private volatile Mixer.Info mixer = null;

    private volatile boolean muted = false;
    private volatile boolean isWorking = false;

    private volatile float bassLvl = 1f;

    /**
     * For not stopping mic to do send action
     * without it can cause some glitches in mic recording
     */

    private volatile ExecutorService executorService;


    public Capture(ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
    }

    @Override
    public void changeInput(Mixer.Info mixer) {
        if (mixer == null /*&& this.mixer == null*/)
            mixer = AudioSupplier.getInstance().getDefaultForInput();
        this.mixer = mixer;
        if (mic != null)
            mic.close();

        try {
            mic = AudioSupplier.getInstance().getInput(this.mixer);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mute() {
        muted = !muted;
        if (!muted) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public void IncreaseBass(int percentage) {
        bassLvl = Algorithms.findPercentage((int) MIN_BASS_LVL, (int) MAX_BASS_LVL, percentage);
    }

    @Override
    public synchronized boolean start(String name) {
        if (isWorking)
            return false;
        try {
            onStart();
        } catch (LineUnavailableException e) {
            return false;
        }

        new Thread(() -> {
            while (isWorking) {
                if (muted) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                if (!isWorking)
                    break;
                byte[] bytes = bassBoost(readFromMic());

                synchronized (this) {
                    if (!executorService.isShutdown()) {
                        executorService.execute(() -> helpHandlerPredecessor.handleRequest(
                                BUTTONS.SEND_SOUND, new Object[]{bytes}));
                    }
                }
            }
        }, name).start();

        return true;
    }

    private void onStart() throws LineUnavailableException {
        isWorking = true;
        muted = false;
        if (mic == null || !mic.isOpen())
            mic = AudioSupplier.getInstance().getInput(mixer);
        executorService = getDefaultOne();
    }

    @Override
    public synchronized void close() {
        if (!isWorking)
            return;
        isWorking = false;
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        mic.close();
        bassLvl = 1f;
        this.notify();
    }

    @Override
    public void init() {
        changeInput(null);
    }

    private byte[] readFromMic() {
        byte[] bytes = new byte[AudioSupplier.getInstance().getMicCaptureSize()];
        mic.read(bytes, 0, bytes.length);
        return bytes;
    }

    private byte[] bassBoost(byte[] data) {
        if (bassLvl == 1f)
            return data;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] * bassLvl);
        }
        return data;
    }

    private ExecutorService getDefaultOne() {
        return new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueueWithWait<>(Resources.getMicQueueSize()));
    }
}
