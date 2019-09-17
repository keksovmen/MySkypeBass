package Bin.Audio;

import Bin.Networking.Protocol.AbstractHeader;
import Bin.Networking.Protocol.ProtocolBitMap;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Util.Checker;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * SingleTone class
 * Integer gets from the server as unique id for each person in a conversation
 * AudioFormat gets from the server
 * TargetDataLine obtains as conversation started along with sourceDataLine
 * After receiving audioFormat check if client can obtain such lines
 */

public class AudioClient implements ErrorHandler {


    private static volatile AudioClient audioClient;  //Instance

    static final int CAPTURE_SIZE = 8192;      //8 kB for sound notification and util sounds

    private int micCaptureSize;      //for capture and play equals sample rate / 2 * sample size or get from the property

    private AudioFormat audioFormat;    //receive from a server

    private final Map<Integer, SourceDataLine> mainAudio;

    /**
     * Defines ability to use it
     */

    private boolean mic;
    private boolean speaker;

    private final AudioCapture capture;
    private final MessageNotification notification;

    private AudioClient() {
        mainAudio = new HashMap<>();
        capture = new AudioCapture();
        notification = new MessageNotification();
    }


    //    double checked locking volatile
    public static AudioClient getInstance() {
        AudioClient local = audioClient;
        if (local == null) {
            synchronized (AudioClient.class) {
                local = audioClient;
                if (local == null) {
                    local = audioClient = new AudioClient();
                }
            }
        }
        return local;
    }

    /**
     * Set audio format
     *
     * @param audioFormat to be set
     * @return true only if speaker can play that sound
     */

    public boolean setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        defineCaptureSizeMain((int) audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(), ProtocolBitMap.MAX_VALUE);
        speaker = AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat));
        mic = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
        return speaker & mic;
    }

    /**
     * Defines value for capturing size on mic
     * You can use default calculated or specify your own
     *
     * @param sampleRate       of audio format
     * @param sampleSizeInButs of audio format
     * @param maxPossible      defined by AbstractHeader
     */

    void defineCaptureSizeMain(int sampleRate, int sampleSizeInButs, final int maxPossible) {
        int value = (sampleRate / 2) * (sampleSizeInButs / 8);
        try {
            InputStream resourceAsStream = Checker.getCheckedInput("/properties/Audio.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            String bufferSize = properties.getProperty("bufferSize");
            if (bufferSize != null && bufferSize.length() != 0) {
                int propValue = Integer.valueOf(bufferSize);
                if (propValue < sampleRate * sampleSizeInButs / 8) {
                    value = propValue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (value >= maxPossible) {
            int i = maxPossible % 2;
            value = maxPossible - i;
        }
        micCaptureSize = value;
    }

    /**
     * For more flexibility but not implemented the way i thought
     *
     * @return if mic is able to work
     */

    public boolean isMic() {
        return mic;
    }

    /**
     * For more flexibility but not implemented the way i thought
     *
     * @return if speaker is able to work
     */

    public boolean isSpeaker() {
        return speaker;
    }

    /**
     * Play sound from source line
     * If line is filled flush it's content
     * due not to busy the thread
     *
     * @param IDofUser whose audio it is
     * @param sound    array of bytes WAW format
     */

    public void playAudio(int IDofUser, byte[] sound) {
        SourceDataLine line = mainAudio.get(IDofUser);
        if (line.available() < sound.length) {
            line.flush();
        }
        line.write(sound, 0, sound.length);
    }

    /**
     * Gets volume control
     *
     * @param id for who
     * @return volume control null if speaker can't be used
     */

    public FloatControl getSettings(int id) {
        SourceDataLine sourceDataLine = mainAudio.get(id);
        if (sourceDataLine == null) {
            return null;
        }
        return (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
    }

    /**
     * Call when need to add a user
     * Try to get speaker line and put it according to id
     * the line is ready to be played
     *
     * @param id for who to open
     * @return true only if successfully putted the line, false if can't due to hardware
     */

    public boolean add(int id) {
        if (!speaker || mainAudio.containsKey(id)) {
            return false;
        }
        try {
            mainAudio.put(id, AudioLineProvider.obtainAndOpenSource(audioFormat));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Removes source line and closes it
     *
     * @param id who to remove
     */

    public void remove(int id) {
        if (mainAudio.containsKey(id)) {
            mainAudio.remove(id).close();
        }
    }

    /**
     * Call when conversation is ended
     * remove and closes all lines
     */

    public /*synchronized*/ void close() {
        capture.close();
        mainAudio.forEach((integer, sourceDataLine) -> sourceDataLine.close());
        mainAudio.clear();
    }

    /**
     * Plays random message notification
     */

    public void playRandomMessageSound() {
        notification.playRandomMessageSound();
    }

    /**
     * Plays particular message notification
     *
     * @param indexOfTrack track id in soundNotifications if out of bounds will play random
     */

    public void playIndexedMessageSound(int indexOfTrack) {
        notification.playIndexedMessageSound(indexOfTrack);
    }

    /**
     * Start a new thread for capturing audio
     * Also adds for each user a source line
     * Only if you system can capture or play audio
     *
     * @param sendSound to write sound to the server
     * @param usersId   all users that must be add
     */

    public void startConversation(final Consumer<byte[]> sendSound, final int... usersId) {
        if (speaker) {
            for (int i : usersId) {
                add(i);
            }
        }
        if (mic) {
            capture.start(sendSound, audioFormat);
        }
    }

    /**
     * Wrapper over capture class
     *
     * @return actual state of mute
     */

    public boolean mute() {
        return capture.mute();
    }

    /**
     * Creates action for changing value of bass boost
     *
     * @return ready to use function
     */

    public Consumer<Double> changeMultiplier() {
        return capture.changeMultiplier();
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public int getMicCaptureSize() {
        return micCaptureSize;
    }

    @Override
    public void errorCase() {
        iterate();
        close();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }

    @Override
    public String toString() {
        return "AudioClient{" +
                "\n mainAudio=" + mainAudio +
                ",\n audioFormat=" + audioFormat +
                ",\n mic=" + mic +
                ",\n speaker=" + speaker +
                '}';
    }

}
