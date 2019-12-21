package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Abstraction.Util.Resources;
import com.Implementation.Util.DesktopResources;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;

/**
 * Contains static methods for play audio
 */

public class Player {

    private Player() {
    }

    /**
     * Default play loop
     * Thread will wait until the end of sound
     * Handle different frame sizes
     *
     * @param inputStream where get audio to play
     * @param outputLine  where play audio
     * @throws IOException if can't read from input stream
     */

    public static void playWholeFile(AudioInputStream inputStream, SourceDataLine outputLine) throws IOException {
        byte[] data = new byte[((DesktopResources) Resources.getInstance()).getAudioFragmentSize()];
        int amount;
        int frameSize = outputLine.getFormat().getFrameSize();
        while ((amount = inputStream.read(data)) != -1) {
            //handleRequest odd number in case of sample size = 2 bytes
            int j = amount % frameSize;
            if (j != 0) {
                amount -= j;
            }
            outputLine.write(data, 0, amount);
        }
        outputLine.drain();
    }

    /**
     * Read part from input stream then plays it
     * Handle error with sample size
     *
     * @param inputStream to read from
     * @param audioOutput where to play
     * @return amount of bytes read/played
     * @throws IOException if can't read from file
     */

    public static int playOnce(AudioInputStream inputStream, AudioOutputLine audioOutput) throws IOException {
        byte[] data = new byte[((DesktopResources) Resources.getInstance()).getAudioFragmentSize()];
        int frameSize = audioOutput.getFormat().getFrameSize();
        int amount = inputStream.read(data);
        if (amount == -1) {
            return amount;
        }
        //handleRequest odd number in case of sample size = 2 bytes
        int j = amount % frameSize;
        if (j != 0) {
            amount -= j;
        }
        audioOutput.writeBlocking(data, 0, amount);
        return amount;
    }
}
