package Com.Audio.Output;

import javax.sound.sampled.Mixer;

public interface ChangeOutput {

    void changeOutput(Mixer.Info mixerInfo);
}
