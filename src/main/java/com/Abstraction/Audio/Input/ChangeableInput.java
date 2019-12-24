package com.Abstraction.Audio.Input;

import com.Abstraction.Audio.Helper.AudioHelper;

/**
 * Represent input device ability to change some properties
 */

public interface ChangeableInput {

    /**
     * Implementation should change input device to given id
     *
     * @param indexOfParticularInputDevice key from {@link AudioHelper#getInputLines()}
     */

    void changeInput(int indexOfParticularInputDevice);


    void mute();

    /**
     * Bass boost function
     *
     * @param percentage from 0 - 100%
     */

    void changeBassLevel(int percentage);
}
