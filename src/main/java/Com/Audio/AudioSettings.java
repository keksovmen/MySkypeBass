package Com.Audio;

import Com.Audio.Input.ChangeableInput;
import Com.Audio.Output.ChangeableOutput;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.BUTTONS;

import javax.sound.sampled.Mixer;

/**
 * Change outputs and input
 */
public class AudioSettings implements ActionableLogic {

    private final ChangeableInput input;
    private final ChangeableOutput output;

    public AudioSettings(ChangeableInput input, ChangeableOutput output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void act(BUTTONS button, Object plainData, String stringData, int integerData) {
        switch (button) {
            case MUTE:{
                input.mute();
                return;
            }
            case VOLUME_CHANGED:{
                output.changeVolume(Integer.parseInt(stringData), integerData);
                return;
            }
            case INCREASE_BASS:{
                input.IncreaseBass(integerData);
                return;
            }
            case CHANGE_INPUT:{
                input.changeInput((Mixer.Info) plainData);
                return;
            }
            case CHANGE_OUTPUT:{
                output.changeOutput((Mixer.Info) plainData);
                return;
            }
        }
    }
}
