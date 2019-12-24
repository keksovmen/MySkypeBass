package com.Abstraction.Audio.Settings;

import com.Abstraction.Audio.Input.ChangeableInput;
import com.Abstraction.Audio.Output.ChangeableOutput;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Pipeline.BUTTONS;

/**
 * Handles {@link BUTTONS}
 */

public class BaseAudioSettings implements ButtonsHandler {

    /**
     * Mic
     */

    protected final ChangeableInput input;

    /**
     * AudioPlayer
     */

    protected final ChangeableOutput output;

    public BaseAudioSettings(ChangeableInput input, ChangeableOutput output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        switch (button) {
            case MUTE:{
                input.mute();
                return;
            }
            case VOLUME_CHANGED:{
                output.changeVolume((Integer) data[0], (Integer) data[1]);
                return;
            }
            case INCREASE_BASS:{
                input.changeBassLevel((Integer) data[0]);
                return;
            }
            case CHANGE_INPUT:{
                input.changeInput((int) data[0]);
                return;
            }
            case CHANGE_OUTPUT:{
                output.changeOutputDevice((int) data[0]);
                return;
            }
        }
    }
}
