package com.Abstraction.Audio;

import com.Abstraction.Audio.Helper.AudioHelper;

/**
 * Access point for helper
 * First you need set your instance
 * Singleton
 */

public class AudioSupplier {

    private static AudioHelper helper;

    private AudioSupplier() {

    }

    public static AudioHelper getInstance() {
        if (helper == null)
            throw new NullPointerException("Instance is not initialised, first call setHelper(AudioHelper)!");
        return helper;
    }

    public synchronized static void setHelper(AudioHelper helper) {
        AudioSupplier.helper = helper;
    }

}
