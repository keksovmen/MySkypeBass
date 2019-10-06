package Com.Audio.Output;

import Com.Util.Initialising;

public interface Playable extends Initialising {

    void playSound(int from, byte[] data);

    void playMessage();

    void playMessage(int track);

    void playCall();

    void stopCall();

    void removeOutput(int id);

    void addOutput(int id);

    void close();
}
