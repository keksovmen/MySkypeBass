package com.Abstraction.Util.Monitors;


import com.Abstraction.Util.Resources.Resources;

import java.util.function.Consumer;

/**
 * Helper for sending TCP audio data
 *
 * Checks value against given boundary
 */
public class SpeedMonitor {


    private final int minBoundary;

    private final double multiplier;

    private final Consumer<Runnable> pushAsyncTask;

    /**
     * Displays previous given data times {@link #multiplier}
     */

    private int previouslyAccumulated = 0;

    /**
     * Indicates can you check value or not
     */

    private volatile boolean isAllowed = true;


    public SpeedMonitor(int minBoundary, Consumer<Runnable> pushAsyncTask) {
        this.minBoundary = minBoundary;
        this.pushAsyncTask = pushAsyncTask;
        multiplier = Resources.getInstance().getSpeedMultiplier();
    }

    /**
     *
     * @param value {@code >=} 0
     */

    public synchronized void feedValue(int value){
        int additional = (int) (previouslyAccumulated * multiplier);
        int result = value + additional;
        //check if int overflow occurs
        if(result < 0) {
            handleBoundaryBreach();
            return;
        }
        previouslyAccumulated = result;
        if (result >= minBoundary) {
            handleBoundaryBreach();
            return;
        }
    }

    public boolean isAllowed(){
        return isAllowed;
    }

    private void handleBoundaryBreach(){
        resetAccumulator();
        isAllowed = false;
        pushWakeUpCall();
    }

    private void resetAccumulator(){
        previouslyAccumulated = 0;
    }

    private void setAllowed(){
        isAllowed = true;
        resetAccumulator();
    }

    private void pushWakeUpCall(){
        pushAsyncTask.accept(() -> {
            try {
                Thread.sleep((long) (Resources.getInstance().getThreadSleepDuration() * 1000));
            } catch (InterruptedException e) {
                //shouldn't happen, not using interactions
                e.printStackTrace();
            } finally {
                synchronized (this) {
                    setAllowed();
                }
            }
        });
    }

    public int getMinBoundary() {
        return minBoundary;
    }
}
