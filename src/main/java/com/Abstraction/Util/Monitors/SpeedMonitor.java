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

    /**
     * Indicates can you check value or not
     */

    private boolean isAllowed = true;


    public SpeedMonitor(int minBoundary) {
        this.minBoundary = minBoundary;
        multiplier = Resources.getInstance().getSpeedMultiplier();
    }

    /**
     *
     * @param value {@code >=} 0
     * @return true if total value grater than boundary
     */

    public boolean checkValue(int value){
        int additional = (int) (previouslyAccumulated * multiplier);
        int result = value + additional;
        //check if int overflow occurs
        if(result < 0) {
            resetAccumulator();
            isAllowed = false;
            return true;
        }
        previouslyAccumulated = result;
        if (result >= minBoundary) {
            isAllowed = false;
            return true;
        }
        return false;
    }

    public void resetAccumulator(){
        previouslyAccumulated = 0;
    }

    public boolean isAllowed(){
        return isAllowed;
    }

    public void setAllowed(){
        isAllowed = true;
        resetAccumulator();
    }
}
