package com.Audio;

import com.Audio.Input.ChangeableInput;
import com.Audio.Output.ChangeableOutput;
import com.Client.ButtonsHandler;
import com.Client.LogicObserver;
import com.Pipeline.ActionableLogic;
import com.Pipeline.BUTTONS;

import javax.sound.sampled.Mixer;

/**
 * Change outputs and input
 */
public class AudioSettings implements ButtonsHandler {

    private final ChangeableInput input;
    private final ChangeableOutput output;

    public AudioSettings(ChangeableInput input, ChangeableOutput output) {
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
                input.changeInput((Mixer.Info) data[0]);
                return;
            }
            case CHANGE_OUTPUT:{
                output.changeOutput((Mixer.Info) data[0]);
                return;
            }
        }
    }
}
