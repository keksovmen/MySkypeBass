package Bin.Audio;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.InputStream;

class Player {

    private Player() {
    }

    /**
     * Default play loop
     * Thread will wait until the end of sound
     * Handle different frame sizes
     * Will close source line at the end
     *
     * @param inputStream    where get audio to play
     * @param sourceDataLine where play audio
     * @throws IOException if can't read from input stream
     */

    static void playLoop(InputStream inputStream, SourceDataLine sourceDataLine) throws IOException {
        byte[] data = new byte[AudioClient.CAPTURE_SIZE];
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
        sourceDataLine.close();
    }

    static int playOnce(InputStream inputStream, SourceDataLine sourceDataLine) throws IOException {
        byte[] data = new byte[AudioClient.CAPTURE_SIZE];
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
//        sourceDataLine.drain();
//        sourceDataLine.close();
    }
}
