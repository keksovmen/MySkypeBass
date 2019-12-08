package com.Abstraction.Audio.Misc;

/**
 * throw when can't create audio line for some reason
 */

public class AudioLineException extends Exception {

    public AudioLineException() {
    }

    public AudioLineException(String message) {
        super(message);
    }
}
