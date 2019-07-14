package Bin.Audio;

import Bin.Util.Checker;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Need for playing sound while calling or get called
 */

public class CallNotificator {

    private static final String incomingFileName = "vint.WAV";
    private static final String outComingHeadFileName = "start.WAV";
    private static final String outComingBodyFileName = "body.WAV";

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
                playOneFile("/sound/callNotification/" + incomingFileName);
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
            playOneFile("/sound/callNotification/" + outComingHeadFileName);
            //Play body of the melody for n time
            while (work) {
                playOneFile("/sound/callNotification/" + outComingBodyFileName);
            }
        }, "Out coming call").start();
    }

    public void stop() {
        work = false;
    }

    /**
     * Play one given file fully
     * Will close all used resource at the end
     *
     * @param name of resource to be played
     */

    private void playOneFile(String name) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                Checker.getCheckedInput(name))) {
            speaker = AudioLineProvider.getFromInput(inputStream);
            while (work) {
                if (Player.playOnce(inputStream, speaker) == -1) {
                    break;
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
            work = false;
        } finally {
            speaker.drain();
            speaker.close();
        }
    }

}
