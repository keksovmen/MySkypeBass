package com.Abstraction.Audio.Settings;

import com.Abstraction.Audio.Input.ChangeableInput;
import com.Abstraction.Audio.Output.ChangeableOutput;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Pipeline.BUTTONS;

public class BaseAudioSettings implements ButtonsHandler {

    protected final ChangeableInput input;
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
                input.IncreaseBass((Integer) data[0]);
                return;
            }
            case CHANGE_INPUT:{
                input.changeInput((int) data[0]);
                return;
            }
            case CHANGE_OUTPUT:{
                output.changeOutput((int) data[0]);
                return;
            }
        }
    }
}
