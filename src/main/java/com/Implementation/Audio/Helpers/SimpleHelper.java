package com.Implementation.Audio.Helpers;

import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Misc.AbstractAudioFormatWithMic;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Implementation.Util.Checker;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources.Resources;
import com.Implementation.Audio.Input.AudioInputDesktop;
import com.Implementation.Audio.Output.AudioOutputDesktop;
import com.Implementation.Audio.Output.Player;
import com.Implementation.Main;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimpleHelper extends AudioHelper {

    private final Map<Integer, Mixer.Info> sourceLines;
    private final Map<Integer, Mixer.Info> targetLines;

//    private AbstractAudioFormat abstractAudioFormat;

    public SimpleHelper() {
        sourceLines = new HashMap<>();
        targetLines = new HashMap<>();
//        abstractAudioFormat = null;
    }

    @Override
    public AudioOutputLine getOutput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException {
        try {

            AudioFormat format = parseFormat(audioFormat);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format, sourceLines.get(idOfParticularMixer));
            sourceDataLine.open(format);
            sourceDataLine.start();
            return new AudioOutputDesktop(sourceDataLine, audioFormat);
        } catch (LineUnavailableException e) {
            throw new AudioLineException("Can't open output line, because unavailable");
        }
    }

    @Override
    public AudioOutputLine getOutput(int idOfParticularMixer) throws AudioLineException {
        return getOutput(idOfParticularMixer, getDefaultAudioFormat());
    }

    @Override
    public void playResourceFile(int idOfParticularMixer, int trackId) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                Checker.getCheckedInput(Main.NOTIFICATION_PATH + Resources.getInstance().getNotificationTracks().get(trackId).name));
             SourceDataLine sourceDataLine = getOutputForFile(idOfParticularMixer, inputStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {

            Player.playWholeFile(audioInputStream, sourceDataLine);

        } catch (IOException | AudioLineException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDefaultForOutput() {
        return 0;
    }

    @Override
    public int getDefaultForInput() {
        return 0;
    }

    @Override
    public AudioInputLine getInput(int idOfParticularMixer, AbstractAudioFormat audioFormat) throws AudioLineException {
        try {
            AudioFormat format = parseFormat(audioFormat);
            TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(format, targetLines.get(idOfParticularMixer));
            targetDataLine.open(format);
            targetDataLine.start();
            return new AudioInputDesktop(targetDataLine, audioFormat);
        } catch (LineUnavailableException e) {
            throw new AudioLineException("Can't open input line, because unavailable");
        }
    }

    @Override
    public AudioInputLine getInput(int idOfParticularMixer) throws AudioLineException {
        return getInput(idOfParticularMixer, getDefaultAudioFormat());
    }

    @Override
    public int getMicCaptureSize() {
        return getDefaultAudioFormat().getMicCaptureSize();
    }

    @Override
    public Map<Integer, String> getOutputLines() {
        return transform(sourceLines);
    }

    @Override
    public Map<Integer, String> getInputLines() {
        return transform(targetLines);
    }

    @Override
    public boolean isFormatSupported(AbstractAudioFormatWithMic format) {
        AudioFormat platformFormat = parseFormat(format);
        sourceLines.clear();
        targetLines.clear();
        boolean result = isLineExist(platformFormat, SourceDataLine.class) &&
                isLineExist(platformFormat, TargetDataLine.class);
        if (result) {
            setDefaultFormat(format);
        }
        return result;
    }

    private boolean isLineExist(AudioFormat format, Class<? extends DataLine> dataLineClass) {
        boolean result = false;
        int id = 0;
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(new DataLine.Info(dataLineClass, format))) {
                if (dataLineClass.equals(SourceDataLine.class)) {
                    sourceLines.put(id, info);
                } else if (dataLineClass.equals(TargetDataLine.class)) {
                    targetLines.put(id, info);
                }
                id++;
                result = true;
            }
        }
        return result;
    }

    private SourceDataLine getOutputForFile(int idOfParticularMixer, BufferedInputStream file) throws IOException, AudioLineException {
        try {
            AudioFormat format = AudioSystem.getAudioFileFormat(file).getFormat();
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format, sourceLines.get(idOfParticularMixer));
            sourceDataLine.open(format);
            sourceDataLine.start();
            return sourceDataLine;
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            throw new AudioLineException("Can't open output line, because unsupported file extension");
        }
    }

    private static AudioFormat parseFormat(AbstractAudioFormat audioFormat) {
        return new AudioFormat
                (
                        audioFormat.getSampleRate(),
                        audioFormat.getSampleSizeInBits(),
                        audioFormat.getChannelsAmount(),
                        audioFormat.isSigned(),
                        audioFormat.isBigEndian()
                );
    }

    private static Map<Integer, String> transform(Map<Integer, ?> from) {
        Map<Integer, String> result = new HashMap<>();
        from.forEach((integer, info) -> result.put(integer, info.toString()));
        return result;
    }
}
