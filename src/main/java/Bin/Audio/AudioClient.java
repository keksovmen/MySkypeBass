package Bin.Audio;

import Bin.Expendable;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private static final int CAPTURE_SIZE = 16384;
    private static final Path sounds = Paths.get("src\\main\\resources\\sound\\");
    private Map<Integer, SourceDataLine> mainAudio;
//    private Map<Integer, SourceDataLine> soundBoardAudio;

    private AudioFormat audioFormat;

    private TargetDataLine targetDataLine;

    /*
    * Defines ability to use it
     */
    private boolean mic;
    private boolean speaker;

    private static volatile AudioClient audioClient;

    private Random random;

    private AudioClient(){
        mainAudio = new HashMap<>();
        random = new Random();

//        soundBoardAudio = new HashMap<>();

//        audioClient = this;
    }

    //double checked locking volatile
    public static AudioClient getInstance(){
        AudioClient local = audioClient;
        if (local == null){
            synchronized (AudioClient.class){
                local = audioClient;
                if (local == null){
                    local = audioClient = new AudioClient();
                }
            }
        }
        return local;
    }

    public boolean setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        speaker = AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat));
        mic = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
        return speaker & mic;
    }

    public static boolean isFormatSupported(AudioFormat audioFormat){
        return AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, audioFormat)) &
                AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, audioFormat));
    }

    public boolean isMic() {
        return mic;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    private boolean obtainSourceLine(int IDofUser){
        if (!speaker) return false;
        try {
            mainAudio.put(IDofUser, (SourceDataLine) obtainAndOpen(SourceDataLine.class));
//            soundBoardAudio.put(IDofUser, (SourceDataLine) obtainAndOpen(SourceDataLine.class));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private DataLine obtainAndOpen(Class<? extends DataLine> lineType) throws LineUnavailableException {
        DataLine line;
        if (lineType == SourceDataLine.class){
            line =  AudioSystem.getSourceDataLine( audioFormat);
            ((SourceDataLine) line).open(audioFormat, (int) (audioFormat.getFrameRate()));
        }else {
            line = AudioSystem.getTargetDataLine(audioFormat);
            ((TargetDataLine) line).open(audioFormat, (int) (audioFormat.getFrameRate()));
        }
        line.start();

        return line;
    }

    private void clearSourceLine(int IDofUser){
        if (mainAudio.containsKey(IDofUser)) mainAudio.remove(IDofUser).close();
//        if (soundBoardAudio.containsKey(IDofUser)) soundBoardAudio.remove(IDofUser).close();
    }

    private boolean obtainTargetLine(){
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

    private void closeTargetLine(){
        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
            targetDataLine = null;
        }
    }

    public void playAudio(int IDofUser, boolean isMainAudio, byte[] sound){
        SourceDataLine line = null;
        if (isMainAudio)
            line = mainAudio.get(IDofUser);
//        else
//            line = soundBoardAudio.get(IDofUser);
//        System.out.println("Available " + line.available() + " length " + sound.length + " buffer size " + line.getBufferSize());
        if (line.available() < sound.length) {
            line.flush();
            System.out.println("flush");
        }
        line.write(sound, 0, sound.length);
    }

    byte[] captureAudio(){
        byte[] data = null;
        if (mic){
            data = new byte[CAPTURE_SIZE];
            targetDataLine.read(data, 0, data.length);
        }
        return data;
    }


    // Handle the exception
    public FloatControl getSettings(int id){
        SourceDataLine sourceDataLine = mainAudio.get(id);
        return (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
    }

    public boolean add(int id) {
        obtainTargetLine();
        return obtainSourceLine(id);
    }

    public void remove(int id) {
        clearSourceLine(id);
    }

    public void close() {
        mainAudio.forEach((integer, sourceDataLine) -> sourceDataLine.close());
        mainAudio.clear();
//        soundBoardAudio.forEach((integer, sourceDataLine) -> sourceDataLine.close());
//        soundBoardAudio.clear();
        closeTargetLine();
    }

    public void playMessageSound(){
            new Thread(() -> {
                try {
                    //obtain a random sound for notification
                    List<Path> collect = Files.list(sounds.resolve(Paths.get("messageNotification"))).collect(Collectors.toList());//think optimize to cashe it
//                    random = new Random();
                    Path path = collect.get(random.nextInt(collect.size()));

                    //open source data line in default mixer
                    AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(path.toFile());
                    SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
                    sourceDataLine.open(audioFileFormat.getFormat());
                    sourceDataLine.start();

                    //start playing sound
                    InputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile());

                    byte[] data = new byte[CAPTURE_SIZE];
                    int amount;
                    while ((amount = audioInputStream.read(data)) != -1){
                        sourceDataLine.write(data, 0, amount);
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();

                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                    e.printStackTrace();
                }
            }, "Message notifier").start();
    }



    @Override
    public String toString() {
        return "AudioClient{" +
                "\n mainAudio=" + mainAudio +
//                ",\n soundBoardAudio=" + soundBoardAudio +
                ",\n audioFormat=" + audioFormat +
                ",\n targetDataLine=" + targetDataLine +
                ",\n mic=" + mic +
                ",\n speaker=" + speaker +
                '}';
    }

    //    public boolean isFormatAcceptable(Class<?> lineClass){
//        return AudioSystem.isLineSupported(new DataLine.Info(lineClass, audioFormat));
//    }
}
