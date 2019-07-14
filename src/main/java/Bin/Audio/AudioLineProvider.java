package Bin.Audio;

import com.sun.istack.internal.NotNull;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

class AudioLineProvider {

    private AudioLineProvider() {
    }

    /**
     * Try to get a default source line from default mixer
     *
     * @param audioFormat format to be played
     * @return opened source line
     * @throws LineUnavailableException if there is no such line
     */

    static SourceDataLine obtainAndOpenSource(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
        sourceDataLine.open(audioFormat, (int) audioFormat.getFrameRate());
        return sourceDataLine;
    }

    /**
     * Try to get a default target line from default mixer
     *
     * @param audioFormat format to be captured
     * @return opened target line
     * @throws LineUnavailableException if there is no such line
     */

    static TargetDataLine obtainAndOpenTarget(AudioFormat audioFormat) throws LineUnavailableException {
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open(audioFormat, (int) audioFormat.getFrameRate());
        return targetDataLine;
    }

    /**
     * Obtain and open source data line for notifications
     *
     * @param file contain audio meta inf
     * @return ready to be written audio output
     * @throws IOException                   if can't read a file
     * @throws UnsupportedAudioFileException if system can't open that type of line
     * @throws LineUnavailableException      if the line is already in use
     */

    static SourceDataLine getFromFile(@NotNull File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
        sourceDataLine.open(audioFileFormat.getFormat());
        sourceDataLine.start();
        return sourceDataLine;
    }

    /**
     * Same as above but get it from input stream
     * input stream must be obtained not from AudioSystem.getAudioInputStream()
     *
     * @param inputStream MUST BE BUFFERED CAUSE SUPPORT MARC/RESET
     *                    leading to a sound data
     * @return ready to be written audio output
     * @throws IOException                   if file can't be read
     * @throws UnsupportedAudioFileException if system can't open that type of line
     * @throws LineUnavailableException      if the line is already in use
     */

    static SourceDataLine getFromInput(@NotNull BufferedInputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(inputStream);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFileFormat.getFormat());
        sourceDataLine.open(audioFileFormat.getFormat());
        sourceDataLine.start();
        return sourceDataLine;
    }
}
