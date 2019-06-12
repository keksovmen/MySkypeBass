package Bin.Audio;

import Bin.Main;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Need for playing sound while calling or get called
 */

public class CallNotificator {

    private static final String incomingFileName = "vint";
    private static final String outComingHeadFileName = "start";
    private static final String outComingBodyFileName = "body";

    private SourceDataLine speaker;
    private volatile boolean work;

    /**
     * STARTS NEW THREAD
     * Should call when an incoming call received
     * will play until stop method is called
     */

    public void playIncoming() {
        work = true;
        new Thread(() -> {
            while (work) {
                try (BufferedInputStream inputStream = new BufferedInputStream(
                        Main.class.getResourceAsStream("/sound/callNotification/" + incomingFileName + ".WAV"))) {
                    speaker = AudioClient.getInstance().getFromInput(inputStream);
                    byte[] data = new byte[AudioClient.CAPTURE_SIZE];
                    int i;
                    int j;
                    while (work) {
                        i = inputStream.read(data);
                        if (i == -1) {
                            break;
                        }
                        //handle odd number in case of sample size = 2 bytes
                        j = i % speaker.getFormat().getFrameSize();
                        if (j != 0) {
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

    /**
     * STARTS NEW THREAD
     * Should call when an out coming call is made
     * will play until stop method is called
     */

    public void playOutComing() {
        work = true;
        new Thread(() -> {
            try (BufferedInputStream inputStream = new BufferedInputStream(
                    Main.class.getResourceAsStream("/sound/callNotification/" + outComingHeadFileName + ".WAV"))) {
                speaker = AudioClient.getInstance().getFromInput(inputStream);
                byte[] data = new byte[AudioClient.CAPTURE_SIZE];
                int i;
                int j;
                //Play intro of the melody
                while (work) {
                    i = inputStream.read(data);
                    if (i == -1) {
                        break;
                    }
                    //handle odd number in case of sample size = 2 bytes
                    j = i % speaker.getFormat().getFrameSize();
                    if (j != 0) {
                        i -= j;
                    }
                    speaker.write(data, 0, i);
                }
                //Play body of the melody for n time
                while (work) {
                    try (InputStream bodyStream = new BufferedInputStream(
                            Main.class.getResourceAsStream("/sound/callNotification/" + outComingBodyFileName + ".WAV"))) {
                        while (work) {
                            i = bodyStream.read(data);
                            if (i == -1) {
                                break;
                            }
                            j = i % speaker.getFormat().getFrameSize();
                            if (j != 0) {
                                i -= j;
                            }
                            speaker.write(data, 0, i);
                        }
                        speaker.drain();
                    }
                }
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                work = false;
                speaker.close();
            }
        }, "Out coming call").start();
    }

    public void stop() {
        work = false;
    }

}
