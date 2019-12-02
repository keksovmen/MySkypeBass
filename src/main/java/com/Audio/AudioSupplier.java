package com.Audio;

import com.Audio.Helpers.AudioHelper;
import com.Audio.Helpers.SimpleHelper;

public class AudioSupplier {

    private static final AudioHelper helper = new SimpleHelper();

    private AudioSupplier() {

    }

    public static AudioHelper getInstance() {
        return helper;
    }

}
