package Bin.Audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CallNotificator {

    private final Path incomingPath = AudioClient.sounds.resolve(Paths.get("callNotification\\vint.WAV"));
    private final Path outComingPath = AudioClient.sounds.resolve(Paths.get("callNotification\\start.WAV"));
    private final Path bodyPath = AudioClient.sounds.resolve(Paths.get("callNotification\\body.WAV"));

    private SourceDataLine speaker;
    private volatile boolean work;

    public void playIncoming(){
        work = true;
        new Thread(() -> {
            while (work) {
                try (InputStream inputStream = new BufferedInputStream(AudioSystem.getAudioInputStream(incomingPath.toFile()))) {
                    speaker = AudioClient.getInstance().getFromFile(incomingPath.toFile());
                    byte[] data = new byte[AudioClient.CAPTURE_SIZE];
                    int i;
                    int j;
                    while (work) {
                        i = inputStream.read(data);
                        if (i == -1) {
                            break;
                        }
                        j = i % speaker.getFormat().getFrameSize();
                        if (j != 0){
                            i -= j;
                        }
                        speaker.write(data, 0, i);
                    }
                    speaker.drain();
                    speaker.close();
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                    e.printStackTrace();
                } finally {
                    work = false;
                    speaker.close();
                }
            }
        }, "Incoming call").start();
    }

    public void playOutComing(){
        work = true;
        new Thread(() -> {
            try(InputStream inputStream = new BufferedInputStream(AudioSystem.getAudioInputStream(outComingPath.toFile()))){
                speaker = AudioClient.getInstance().getFromFile(incomingPath.toFile());
                byte[] data = new byte[AudioClient.CAPTURE_SIZE];
                int i;
                int j;
                //Play intro of the melody
                while (work) {
                    i = inputStream.read(data);
                    if (i == -1) {
                        break;
                    }
                    j = i % speaker.getFormat().getFrameSize();
                    if (j != 0){
                        i -= j;
                    }
                    speaker.write(data, 0, i);
                }
                //Play body of the melody for n time
                while (work){
                    try(InputStream bodyStream = new BufferedInputStream(AudioSystem.getAudioInputStream(bodyPath.toFile()))){
                        while (work) {
                            i = bodyStream.read(data);
                            if (i == -1) {
                                break;
                            }
                            j = i % speaker.getFormat().getFrameSize();
                            if (j != 0){
                                i -= j;
                            }
                            speaker.write(data, 0, i);
                        }
                        speaker.drain();
                    }
                }
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }finally {
                work = false;
                speaker.close();
            }
        }, "Out coming call").start();
    }

    public void stop(){
        work = false;
    }

}
