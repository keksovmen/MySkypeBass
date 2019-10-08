package Com.Audio.Output;

import Com.Model.Updater;
import Com.Util.Interfaces.Initialising;

public interface Playable extends Initialising, Updater {

    void playSound(int from, byte[] data);

    void playMessage();

    void playMessage(int track);

    void playCall();

    void stopCall();

    void close();
}
