package Com.Audio.Input;

import javax.sound.sampled.Mixer;

public interface ChangeableInput {

    void changeInput(Mixer.Info mixer);

    void mute();

    void IncreaseBass(int percentage);
}
