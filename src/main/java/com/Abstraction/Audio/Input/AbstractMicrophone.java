package com.Abstraction.Audio.Input;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Pipeline.BUTTONS;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Platform independent microphone representation
 */

public abstract class AbstractMicrophone implements DefaultMic, ChangeableInput {

//    /**
//     * Indicate that an input device has't been initialised
//     */
//
//    private static int NOT_INITIALISED_INPUT_ID = -1;

    /**
     * Chain of Responsibility
     * Where to send commands
     */

    protected final ButtonsHandler helpHandlerPredecessor;

    /**
     * For mute actions, and main loop synchronisations
     */

    protected final Lock muteLock;

    /**
     * Abstract input line for reading data
     */

    protected volatile AudioInputLine inputLine;

    /**
     * Which audio Device is used {@link AudioHelper#getInputLines()} key from here
     */

    protected volatile int indexOfParticularInputDevice;

    /**
     * Muted state if true, mean main loop must be waiting
     */

    protected volatile boolean isMuted;

    /**
     * Working state if true, Capture thread must be launched and not closed
     */

    protected volatile boolean isWorking;


    public AbstractMicrophone(ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        muteLock = new ReentrantLock();
//        indexOfParticularInputDevice = NOT_INITIALISED_INPUT_ID;
    }

    /**
     * Close previously opened line if such exists and obtain new one
     * Also modifies {@link #indexOfParticularInputDevice} to param value
     *
     * @param indexOfParticularInputDevice key from {@link AudioHelper#getInputLines()}
     */

    @Override
    public synchronized void changeInput(int indexOfParticularInputDevice) {
        this.indexOfParticularInputDevice = indexOfParticularInputDevice;
        if (inputLine != null)
            inputLine.close();

        try {
            inputLine = AudioSupplier.getInstance().getInput(this.indexOfParticularInputDevice);
        } catch (AudioLineException e) {
            e.printStackTrace();
            inputLine = null;
        }
    }

    /**
     * Mute just put capture thread in wait state
     * And notifies if un muted
     */

    @Override
    public void mute() {
        isMuted = !isMuted;
        if (!isMuted) {
            synchronized (muteLock) {
                muteLock.notify();
            }
        }
    }

    /**
     * Starts new thread for capturing audio
     * Obtain input device
     *
     * @param name with given name
     * @return true if new thread is started false otherwise
     */

    @Override
    public synchronized boolean start(String name) {
        if (isWorking)
            return false;
        if (!onStart())
            return false;

        new Thread(this::workingLoop, name).start();
        return true;
    }

    /**
     * Kills started thread
     * Closes input device
     */

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
     * Sets default audio input device
     */

    @Override
    public void init() {
        changeInput(AudioSupplier.getInstance().getDefaultForInput());
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
                    }
                }
            }
            if (!isWorking)
                break;
            checkExistenceOfMic();
            byte[] bytes = bassBoost(readFromMic());

            helpHandlerPredecessor.handleRequest(BUTTONS.SEND_SOUND, new Object[]{bytes});
        }
    }

    /**
     * If input line is null will wait until {@link #changeInput(int)} is called
     */

    private synchronized void checkExistenceOfMic() {
        while (inputLine == null) {
            try {
                wait();
            } catch (InterruptedException ignored) {
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
     * Should modify data and return it
     *
     * @param data to modify
     * @return modified data
     */

    protected abstract byte[] bassBoost(byte[] data);


    /**
     * Putting flags in proper state and obtaining input line
     *
     * @return true if input line is acquired
     */

    private boolean onStart() {
        isWorking = true;
        isMuted = false;
        if (inputLine == null) {
            try {
//            changeInput(getDeviceId());
////            if (inputLine == null) {
////                return false;
////            }
                inputLine = AudioSupplier.getInstance().getInput(indexOfParticularInputDevice);
            } catch (AudioLineException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

//    private int getDeviceId() {
//        return indexOfParticularInputDevice == NOT_INITIALISED_INPUT_ID ?
//                AudioSupplier.getInstance().getDefaultForInput() : indexOfParticularInputDevice;
//    }
}
