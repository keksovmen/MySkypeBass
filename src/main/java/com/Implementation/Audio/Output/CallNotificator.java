package com.Implementation.Audio.Output;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Implementation.Util.Checker;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
             AudioOutputLine outputForFile = AudioSupplier.getInstance().getOutput(indexOfOutput, translate(inputStream));
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

    private static AbstractAudioFormat translate(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
        AudioFormat format = AudioSystem.getAudioFileFormat(inputStream).getFormat();
        return new AbstractAudioFormat(
                (int) format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED),
                format.isBigEndian()
                );
    }

}
