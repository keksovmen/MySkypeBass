package com.Abstraction.Util.Monitors;


import com.Abstraction.Util.Resources.Resources;

/**
 * Helper for sending TCP audio data
 *
 * Checks value against given boundary
 */
public class SpeedMonitor {


    private final int minBoundary;

    private final double multiplier;

    /**
     * Displays previous given data times {@link #multiplier}
     */

    private int previouslyAccumulated = 0;


    public SpeedMonitor(int minBoundary) {
        this.minBoundary = minBoundary;
        multiplier = Resources.getInstance().getSpeedMultiplier();
    }

    public boolean checkValue(int value){
        int additional = (int) (previouslyAccumulated * multiplier);
        int result = value + additional;
        //check if int overflow occurs
        if(result < 0) {
            resetAccumulator();
            return true;
        }
        previouslyAccumulated = result;
        if (result >= minBoundary)
            return true;
        return false;
    }

    public void resetAccumulator(){
        previouslyAccumulated = 0;
    }
}
