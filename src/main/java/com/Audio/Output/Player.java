package com.Audio.Output;

import com.Util.Resources;

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
     * @param inputStream    where get audio to play
     * @param sourceDataLine where play audio
     * @throws IOException if can't read from input stream
     */

    public static void playLoop(AudioInputStream inputStream, SourceDataLine sourceDataLine) throws IOException {
        byte[] data = new byte[Resources.getAudioFragmentSize()];
        int amount;
        int j;
        int frameSize = sourceDataLine.getFormat().getFrameSize();
        while ((amount = inputStream.read(data)) != -1) {
            //handle odd number in case of sample size = 2 bytes
            j = amount % frameSize;
            if (j != 0) {
                amount -= j;
            }
            sourceDataLine.write(data, 0, amount);
        }
        sourceDataLine.drain();
    }

    /**
     * Read part from input stream then plays it
     * Handle error with sample size
     *
     * @param inputStream    to read from
     * @param sourceDataLine where to play
     * @return amount of bytes read/played
     * @throws IOException if can't read from file
     */

    public static int playOnce(AudioInputStream inputStream, SourceDataLine sourceDataLine) throws IOException {
        byte[] data = new byte[Resources.getAudioFragmentSize()];
        int frameSize = sourceDataLine.getFormat().getFrameSize();
        int amount = inputStream.read(data);
        if (amount == -1) {
            return amount;
        }
        //handle odd number in case of sample size = 2 bytes
        int j = amount % frameSize;
        if (j != 0) {
            amount -= j;
        }
        sourceDataLine.write(data, 0, amount);
        return amount;
    }
}
