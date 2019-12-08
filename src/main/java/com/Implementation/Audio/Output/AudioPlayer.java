package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Util.Checker;
import com.Abstraction.Util.Resources;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AudioPlayer extends AbstractAudioPlayer {

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new CallNotificator();
    }

    @Override
    public void playMessage() {
        int track = ThreadLocalRandom.current().nextInt(0, Resources.getMessagePaths().size());
        playMessage(track);
    }

    @Override
    public void playMessage(int track) {
        List<String> messagePath = Resources.getMessagePaths();
        if (messagePath.size() <= track || track < 0) {
            playMessage(); //Play random one
            return;
        }
        executorService.execute(() -> playMessageSound(track));
    }


    /**
     * LISTEN UP FOLKS:
     * IF YOU SWAP 2 LINES - SourceDataLine and AudioInputStream
     * YOU WILL GET EXCEPTION IN AudioSupplier.getOutputForFile();
     * SOMEHOW AUDIO SYSTEM RUINS PREVIOUS INPUT STREAM AND IT COUNTS AS INVALID
     * BE AWARE
     *
     * @param track id of sound
     */

    private void playMessageSound(int track) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                Checker.getCheckedInput(Resources.getMessagePaths().get(track)));
             AudioOutputLine sourceDataLine = AudioSupplier.getInstance().getOutputForFile(outputMixerId, inputStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {

            Player.playLoop(audioInputStream, sourceDataLine);

        } catch (IOException | AudioLineException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

}
