package com.Abstraction.Audio.Output;

import com.Abstraction.Audio.Helper.AudioHelper;

/**
 * Allow change output device for underlying audio output line
 */

public interface ChangeOutputDevice {

    /**
     * @param indexOfParticularDevice key in {@link AudioHelper#getOutputLines()}
     */

    void changeOutputDevice(int indexOfParticularDevice);
}
