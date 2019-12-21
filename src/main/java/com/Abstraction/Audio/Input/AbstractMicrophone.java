package com.Abstraction.Audio.Input;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Collection.ArrayBlockingQueueWithWait;
import com.Abstraction.Util.Resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMicrophone implements DefaultMic, ChangeableInput {

    protected final ButtonsHandler helpHandlerPredecessor;

    /**
     * For not stopping mic to do send action
     * without it can cause some glitches in mic recording
     */

    protected final ExecutorService executorService;

    /**
     * For mute actions
     */

    protected final Lock muteLock;

    protected volatile AudioInputLine inputLine;
    protected volatile int indexOfParticularMixer;

    protected volatile boolean isMuted;
    protected volatile boolean isWorking;


    public AbstractMicrophone(ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        executorService = createExecutor();
        muteLock = new ReentrantLock();
    }

    @Override
    public synchronized void changeInput(int indexOfParticularInputDevice) {
        indexOfParticularMixer = indexOfParticularInputDevice;
        if (inputLine != null)
            inputLine.close();

        try {
            inputLine = AudioSupplier.getInstance().getInput(indexOfParticularMixer);
        } catch (AudioLineException e) {
            e.printStackTrace();
            inputLine = null;
        }
    }

    @Override
    public void mute() {
        isMuted = !isMuted;
        if (!isMuted) {
            synchronized (muteLock) {
                muteLock.notify();
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
        isMuted = false;

        if (inputLine != null) {
            inputLine.close();
            inputLine = null;
        }
        synchronized (muteLock) {
            muteLock.notify();
        }
        notify();
    }

    /**
     * Override if needed
     * Default implementation
     */

    protected void workingLoop() {
        while (isWorking) {
            if (isMuted) {
                synchronized (muteLock) {
                    try {
                        muteLock.wait();
                    } catch (InterruptedException ignored) { //should't happen
                        ignored.printStackTrace();
                    }
                }
            }
            if (!isWorking)
                break;
            checkExistenceOfMic();
            byte[] bytes = bassBoost(readFromMic());

//            synchronized (this) {
//                if (!executorService.isShutdown()) {
            executorService.execute(() -> helpHandlerPredecessor.handleRequest(
                    BUTTONS.SEND_SOUND, new Object[]{bytes}
            ));
//                }
//            }
        }
    }

    private synchronized void checkExistenceOfMic(){
        while (inputLine == null){
            try {
                wait();
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
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
        inputLine.readBlocking(bytes);
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
                0,
                1,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueueWithWait<>(Resources.getInstance().getMicQueueSize())
        );
    }

    private boolean onStart() {
        isWorking = true;
        isMuted = false;
        if (inputLine == null) {
            try {
                inputLine = AudioSupplier.getInstance().getInput(indexOfParticularMixer);
            } catch (AudioLineException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
