package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Util.Checker;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Need for playing sound while calling or get called
 */

public class CallNotificator extends AbstractCallNotificator {


    @Override
    protected void playCallSound() {
        playOneFile("/sound/callNotification/Call.WAV");
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
             AudioOutputLine outputForFile = AudioSupplier.getInstance().getOutputForFile(indexOfOutput, inputStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)
        ) {
            while (isWorking) {
                if (Player.playOnce(audioInputStream, outputForFile) == -1) {
                    break;
                }
            }
        } catch (IOException | AudioLineException | UnsupportedAudioFileException e) {
            isWorking = false;
        }
    }

}
