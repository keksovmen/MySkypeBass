package com.Abstraction.Audio.Misc;

/**
 * throw when can't create audio line for some reason
 * Wrapper for platform specific exception when obtaining audio resources
 */

public class AudioLineException extends Exception {

    public AudioLineException() {
    }

    public AudioLineException(String message) {
        super(message);
    }
}
