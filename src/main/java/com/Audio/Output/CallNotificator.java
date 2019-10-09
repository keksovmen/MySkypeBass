package com.Audio.Output;

import com.Audio.AudioSupplier;
import com.Util.Checker;
import com.Util.Interfaces.Starting;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Need for playing sound while calling or get called
 */

public class CallNotificator implements Starting, ChangeOutput {


    private volatile Mixer.Info mixer;
    private volatile boolean work;


    @Override
    public boolean start(String name) {
        if (work)
            return false;
        work = true;

        new Thread(() -> {
            while (work) {
                playOneFile("/sound/callNotification/Call.WAV");
            }
        }, name).start();

        return true;
    }

    @Override
    public void close() {
        work = false;
    }

    @Override
    public void changeOutput(Mixer.Info mixerInfo) {
        mixer = mixerInfo;
    }

    /**
     * Play one given file fully
     * Will close all used resource at the end
     *
     * @param name of resource to be played
     */

    private void playOneFile(String name) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                Checker.getCheckedInput(name));
             SourceDataLine outputForFile = AudioSupplier.getOutputForFile(mixer, inputStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)
        ) {
            while (work) {
                if (Player.playOnce(audioInputStream, outputForFile) == -1) {
                    break;
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
            work = false;
        }
    }

}
