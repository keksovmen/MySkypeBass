package Bin.Audio;

import Bin.Main;
import Bin.Networking.Protocol.AbstractHeader;
import Bin.Networking.Utility.ErrorHandler;
import com.sun.istack.internal.NotNull;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

    private int CAPTURE_SIZE_MAIN;      //for capture and play equals sample rate / 2 * sample size or get from the property

    private AudioFormat audioFormat;    //receive from a server

    private final Map<Integer, SourceDataLine> mainAudio;
    private TargetDataLine targetDataLine;

    /**
     * Defines ability to use it
     */

    private boolean mic;
    private boolean speaker;

    private final AudioCapture capture;

    private final List<String> soundNotifications;

    private AudioClient() {
        mainAudio = new HashMap<>();
        capture = new AudioCapture();
        soundNotifications = new ArrayList<>();
        fillSoundNames();
    }

    /**
     * Fills the array with resources locations
     * Files.list doesn't work with resources!!!!!!
     * When add a new sound must specify it here
     * FIND A WAY HOW TO READ DIRECTORY WITH RESOURCES
     * AND GET CONTENT SEPARATELY WITHOUT DIRECTLY SPECIFYING NAMES
     */

    private void fillSoundNames() {
        soundNotifications.add("/sound/messageNotification/AAAAAAAAAAAAA.WAV");
        soundNotifications.add("/sound/messageNotification/Artist.WAV");
        soundNotifications.add("/sound/messageNotification/Ass.WAV");
        soundNotifications.add("/sound/messageNotification/Club.WAV");
        soundNotifications.add("/sound/messageNotification/College.WAV");
        soundNotifications.add("/sound/messageNotification/Comming.WAV");
        soundNotifications.add("/sound/messageNotification/Fisting.WAV");
        soundNotifications.add("/sound/messageNotification/Like.WAV");
        soundNotifications.add("/sound/messageNotification/Penetration 1.WAV");
        soundNotifications.add("/sound/messageNotification/Penetration 2.WAV");
        soundNotifications.add("/sound/messageNotification/Power.WAV");
        soundNotifications.add("/sound/messageNotification/Take.WAV");
        soundNotifications.add("/sound/messageNotification/WO.WAV");
        soundNotifications.add("/sound/messageNotification/YEAAA.WAV");
        soundNotifications.add("/sound/messageNotification/Cum.WAV");
        soundNotifications.add("/sound/messageNotification/Atention.WAV");
        soundNotifications.add("/sound/messageNotification/Beat.WAV");
        soundNotifications.add("/sound/messageNotification/LetsGo.WAV");
        soundNotifications.add("/sound/messageNotification/Dicks.WAV");
        soundNotifications.add("/sound/messageNotification/Penetration3.WAV");
        soundNotifications.add("/sound/messageNotification/Shut.WAV");
        soundNotifications.add("/sound/messageNotification/Rip.WAV");
        soundNotifications.add("/sound/messageNotification/Sorry.WAV");
        soundNotifications.add("/sound/messageNotification/Sun.WAV");
        soundNotifications.add("/sound/messageNotification/Amazing.WAV");
        soundNotifications.add("/sound/messageNotification/Reach.WAV");
        soundNotifications.add("/sound/messageNotification/Point.WAV");
        soundNotifications.add("/sound/messageNotification/Two.WAV");
        soundNotifications.add("/sound/messageNotification/PullUp.WAV");
        soundNotifications.add("/sound/messageNotification/Doing.WAV");
        soundNotifications.add("/sound/messageNotification/Fucked.WAV");
        soundNotifications.add("/sound/messageNotification/Tool.WAV");
        soundNotifications.add("/sound/messageNotification/Challenges.WAV");
        soundNotifications.add("/sound/messageNotification/Pants.WAV");
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
        defineCaptureSizeMain((int) audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(), AbstractHeader.getMaxLength());
        speaker = AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat));
        mic = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
        return speaker;
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
        Properties properties = new Properties();
        InputStream resourceAsStream = Main.class.getResourceAsStream("/properties/Audio.properties");
        int value = (sampleRate / 2) * (sampleSizeInButs / 8);
        if (resourceAsStream != null) {
            try {
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
        }
        if (value >= maxPossible) {
            int i = maxPossible % 2;
            value = maxPossible - i;
        }
        CAPTURE_SIZE_MAIN = value;
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
     * Try to get speaker line and put it according to id
     * the line is ready to be played
     *
     * @param IDofUser for whom to open
     * @return true only if successfully opened the line
     * false if can't due to hardware
     */

    private boolean obtainSourceLine(int IDofUser) {
        if (!speaker) {
            return false;     //should remove it, because logic is you can't use without mic and speaker but must change it
        }
        try {
            if (!mainAudio.containsKey(IDofUser)) {
                mainAudio.put(IDofUser, (SourceDataLine) obtainAndOpen(SourceDataLine.class));
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Try to get a default lineType from default mixer
     *
     * @param lineType source or target dataLine
     * @return source or target dataLine ready to be player or captured
     * @throws LineUnavailableException if the desired line can't be opened due
     *                                  to resource restriction
     */

    private DataLine obtainAndOpen(Class<? extends DataLine> lineType) throws LineUnavailableException {
        DataLine line;
        if (lineType == SourceDataLine.class) {
            line = AudioSystem.getSourceDataLine(audioFormat);
            ((SourceDataLine) line).open(audioFormat, (int) (audioFormat.getFrameRate()));
        } else {     //target data line expected here
            line = AudioSystem.getTargetDataLine(audioFormat);
            ((TargetDataLine) line).open(audioFormat, (int) (audioFormat.getFrameRate()));
        }
        line.start();

        return line;
    }

    /**
     * Removes and closes a line on end of call
     *
     * @param IDofUser whose line to remove
     */
    private void clearSourceLine(int IDofUser) {
        if (mainAudio.containsKey(IDofUser)) {
            mainAudio.remove(IDofUser).close();
        }
    }

    /**
     * Same meaning as @see obtainSourceLine but for mic
     *
     * @return @see upper
     */

    private boolean obtainTargetLine() {
        if (!mic) {
            return false;
        }
        if (targetDataLine != null && targetDataLine.isOpen()) {
            return true;
        }
        try {
            targetDataLine = (TargetDataLine) obtainAndOpen(TargetDataLine.class);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Closes mic line
     */

    private void closeTargetLine() {
        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
            targetDataLine = null;
        }
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
     * Capture sound from mic
     * Uses a 1 time created byte array for increase performances
     * You don't need to allocate each time new byte array
     * just rewrite bytes in it
     *
     * @return null if mic is not supported or byte[CAPTURE_SIZE]
     */

    byte[] captureAudio() {
        byte[] data = null;
        if (mic) {
            data = new byte[CAPTURE_SIZE_MAIN];
            targetDataLine.read(data, 0, data.length);
        }
        return data;
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
     * Obtain lines for ready to use
     *
     * @param id for who to open
     * @return true if speaker is opened
     */

    public boolean add(int id) {
        return obtainSourceLine(id);
    }

    /**
     * Remove source line
     *
     * @param id who to remove
     */

    public void remove(int id) {
        clearSourceLine(id);
    }

    /**
     * Call when conversation is ended
     * remove and closes all lines
     */

    public /*synchronized*/ void close() {
        mainAudio.forEach((integer, sourceDataLine) -> sourceDataLine.close());
        mainAudio.clear();
        capture.close();
        closeTargetLine();
    }

    /**
     * STARTS NEW THREAD
     * Plays random message notification
     * each time opens new line and select random sound
     * can't be stopped until the end of sound
     */

    public void playRandomMessageSound() {
        playMessageSound(ThreadLocalRandom.current().nextInt(soundNotifications.size()));
    }

    /**
     * STARTS NEW THREAD
     * Plays particular message notification
     * each time opens new line and select random sound
     * can't be stopped until the end of sound
     *
     * @param indexOfTrack track id in soundNotifications
     */

    public void playIndexedMessageSound(int indexOfTrack) {
        if (indexOfTrack < 0 || indexOfTrack >= soundNotifications.size()) {
            playRandomMessageSound();
        } else {
            playMessageSound(indexOfTrack);
        }
    }

    /**
     * STARTS NEW THREAD
     * Play message notification
     * each time opens new line and select random sound
     * can't be stopped until the end of sound
     *
     * @param idOfTrack track id in soundNotifications
     */

    void playMessageSound(int idOfTrack) {
        new Thread(() -> {
            try {
                //obtain a random sound for notification
                if (soundNotifications == null) return;

                InputStream resourceAsStream = Main.class.getResourceAsStream(soundNotifications.get(idOfTrack));
                /* Drop all computation if there is no such resource
                 * Needs when messed up with a name
                 */
                if (resourceAsStream == null) {
                    return;
                }
                BufferedInputStream inputStream = new BufferedInputStream(resourceAsStream);


                //open source data line in default mixer
                SourceDataLine sourceDataLine = getFromInput(inputStream);

                //start playing sound
                InputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);

                byte[] data = new byte[CAPTURE_SIZE];
                int amount;
                int j;
                while ((amount = audioInputStream.read(data)) != -1) {
                    //handle odd number in case of sample size = 2 bytes
                    j = amount % sourceDataLine.getFormat().getFrameSize();
                    if (j != 0) {
                        amount -= j;
                    }
                    sourceDataLine.write(data, 0, amount);
                }
                sourceDataLine.drain();
                sourceDataLine.close();

            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }, "Message notifier").start();
    }

    /**
     * Obtain and open source data line for notifications
     *
     * @param file contain audio meta inf
     * @return ready to be written audio output
     * @throws IOException                   if can't read a file
     * @throws UnsupportedAudioFileException if system can't open that type of line
     * @throws LineUnavailableException      if the line is already in use
     */

    SourceDataLine getFromFile(@NotNull File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
        sourceDataLine.open(audioFileFormat.getFormat());
        sourceDataLine.start();
        return sourceDataLine;
    }

    /**
     * Same as above but get it from input stream
     * input stream must be obtained not from AudioSystem.getAudioInputStream()
     *
     * @param inputStream MUST BE BUFFERED CAUSE SUPPORT MARC/RESET
     *                    leading to a sound data
     * @return ready to be written audio output
     * @throws IOException                   if file can't be read
     * @throws UnsupportedAudioFileException if system can't open that type of line
     * @throws LineUnavailableException      if the line is already in use
     */

    SourceDataLine getFromInput(@NotNull BufferedInputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(inputStream);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
        sourceDataLine.open(audioFileFormat.getFormat());
        sourceDataLine.start();
        return sourceDataLine;
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
            obtainTargetLine();
            capture.start(sendSound, this::captureAudio);
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

    @Override
    public void errorCase() {
        close();
        iterate();
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
                ",\n targetDataLine=" + targetDataLine +
                ",\n mic=" + mic +
                ",\n speaker=" + speaker +
                '}';
    }

}
