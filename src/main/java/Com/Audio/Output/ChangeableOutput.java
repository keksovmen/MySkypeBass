package Com.Audio.Output;

public interface ChangeableOutput extends ChangeOutput {

    void changeVolume(int who, int percentage);

}
