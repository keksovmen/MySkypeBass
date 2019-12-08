package com.Abstraction.Audio.Input;

import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Collection.ArrayBlockingQueueWithWait;
import com.Abstraction.Util.Resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMicrophone implements DefaultMic, ChangeableInput {

    protected final ButtonsHandler helpHandlerPredecessor;

    protected volatile AudioInputLine inputLine;
    protected volatile int indexOfParticularMixer;

    protected volatile boolean isMuted;
    protected volatile boolean isWorking;

    /**
     * For not stopping mic to do send action
     * without it can cause some glitches in mic recording
     */

    private volatile ExecutorService executorService;


    public AbstractMicrophone(ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        executorService = createExecutor();
    }

    @Override
    public void changeInput(int indexOfParticularInputDevice) {
        indexOfParticularMixer = indexOfParticularInputDevice;
        if (inputLine != null)
            inputLine.close();

        try {
            inputLine = AudioSupplier.getInstance().getInput(indexOfParticularMixer);
        } catch (AudioLineException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mute() {
        isMuted = !isMuted;
        if (!isMuted) {
            synchronized (this) {
                notify();
            }
        }
    }

    @Override
    public void init() {
        changeInput(AudioSupplier.getInstance().getDefaultForInput());
    }

    @Override
    public synchronized boolean start(String name) {
        if (isWorking)
            return false;
        if (!onStart())
            return false;

        new Thread(this::workingLoop, name).start();
        return true;
    }

    @Override
    public synchronized void close() {
        if (!isWorking)
            return;
        isWorking = false;
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        inputLine.close();
        notify();
    }

    /**
     * Override if needed
     * Default implementation
     */

    protected void workingLoop() {
        while (isWorking) {
            if (isMuted) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) { //should't happen
                    }
                }
            }
            if (!isWorking)
                break;
            byte[] bytes = bassBoost(readFromMic());

            synchronized (this) {
                if (!executorService.isShutdown()) {
                    executorService.execute(() -> helpHandlerPredecessor.handleRequest(
                            BUTTONS.SEND_SOUND, new Object[]{bytes}
                    ));
                }
            }
        }
    }

    /**
     * Override if needed
     *
     * @return byte[] array of actual sound from mic
     */

    protected byte[] readFromMic() {
        byte[] bytes = new byte[AudioSupplier.getInstance().getMicCaptureSize()];
        inputLine.read(bytes);
        return bytes;
    }

    /**
     * Override for bass boost function
     * Should modify @data and return it
     *
     * @param data to modify
     * @return modified data
     */

    protected abstract byte[] bassBoost(byte[] data);

    /**
     * Default implementation returns single threaded
     * with waiting queue
     *
     * @return default executor
     */

    protected ExecutorService createExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueueWithWait<>(Resources.getMicQueueSize())
        );
    }

    private boolean onStart() {
        isWorking = true;
        isMuted = false;
        if (inputLine == null || !inputLine.isOpen()) {
            try {
                inputLine = AudioSupplier.getInstance().getInput(indexOfParticularMixer);
            } catch (AudioLineException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (executorService.isShutdown())
            executorService = createExecutor();
        return true;
    }
}
