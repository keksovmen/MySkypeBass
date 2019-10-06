package Com.Audio.Output;

public interface ChangeableOutput extends ChangeOutput {

    //    void changeOutput(Mixer.Info mixerInfo);
    void changeVolume(int who, int percentage);

//    void addOutput(int id);

//    void removeOutput(int id);
}
