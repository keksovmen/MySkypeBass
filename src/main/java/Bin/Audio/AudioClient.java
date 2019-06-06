package Bin.Audio;

import Bin.Expendable;
import Bin.Networking.Writers.ClientWriter;
import com.sun.istack.internal.NotNull;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AudioClient implements Expendable {

    /*
     * SingleTone class
     * Integer gets from the server as unique id for each person in a conversation
     * AudioFormat gets from the server
     * TargetDataLine obtains as conversation started along with sourceDataLine
     * After receiving audioFormat check if client can obtain such lines
     */

    static final int CAPTURE_SIZE = 16384;      //16 kB
    static final Path sounds = Paths.get("src\\main\\resources\\sound\\");  //root for sounds

    private static volatile AudioClient audioClient;


    private AudioFormat audioFormat;

    private Map<Integer, SourceDataLine> mainAudio;
    private TargetDataLine targetDataLine;

    /*
     * Defines ability to use it
     */
    private boolean mic;
    private boolean speaker;

    private AudioCapture capture;

    private Random random;
    private List<Path> soundNotifications;

    private AudioClient() {
        mainAudio = new HashMap<>();
        capture = new AudioCapture();
        random = new Random();
        try {
            soundNotifications = Files.list(sounds.resolve(Paths.get("messageNotification"))).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            soundNotifications = new ArrayList<>(1);
        }
    }

    //double checked locking volatile
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
     * @return true only if mic and speaker are accepted it
     */

    public boolean setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        speaker = AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat));
        mic = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
        return speaker & mic;
    }

//    public static boolean isFormatSupported(AudioFormat audioFormat){
//        return AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat)) &
//                AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
//    }

    public boolean isMic() {
        return mic;
    }

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
        if (!speaker)
            return false;     //should remove it, because logic is you can't use without mic and speaker but must change it
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
        if (!mic) return false;
        if (targetDataLine != null && targetDataLine.isOpen()) return true;
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
     *
     * @return null if mic is not supported or byte[CAPTURE_SIZE]
     */

    byte[] captureAudio() {
        byte[] data = null;
        if (mic) {
            data = new byte[CAPTURE_SIZE];
            targetDataLine.read(data, 0, data.length);
        }
        return data;
    }


    // Handle the exception
    public FloatControl getSettings(int id) {
        SourceDataLine sourceDataLine = mainAudio.get(id);
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
        obtainTargetLine();
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

    public void close() {
        mainAudio.forEach((integer, sourceDataLine) -> sourceDataLine.close());
        mainAudio.clear();
        capture.close();
        closeTargetLine();
    }

    /**
     * Play message notification
     * each time opens new line and select random sound
     * can't be stopped until the end of sound
     */

    public void playMessageSound() {
        new Thread(() -> {
            try {
                //obtain a random sound for notification
                Path path = soundNotifications.get(random.nextInt(soundNotifications.size()));

                //open source data line in default mixer
                SourceDataLine sourceDataLine = getFromFile(path.toFile());

                //start playing sound
                InputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile());

                byte[] data = new byte[CAPTURE_SIZE];
                int amount;
                int j;
                while ((amount = audioInputStream.read(data)) != -1) {
                    j = amount % sourceDataLine.getFormat().getFrameSize();
                    if (j != 0){
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
     * @throws IOException if can't read a file
     * @throws UnsupportedAudioFileException if system can't open that type of line
     * @throws LineUnavailableException if the line is already in use
     */

    SourceDataLine getFromFile(@NotNull File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
        sourceDataLine.open(audioFileFormat.getFormat());
        sourceDataLine.start();
        return sourceDataLine;
    }

    /**
     * Start a new thread for capturing audio
     * Also adds for each user a source line
     * @param writer to write sound on the server
     * @param myId from me
     * @param usersId all users that must be add
     */

    public void startConversation(final ClientWriter writer, final int myId, final int... usersId){
        for (int i : usersId) {
            add(i);
        }
        capture.start(writer, myId);
    }

    public boolean mute(){
        return capture.mute();
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
