package com.Audio.Output;

import com.Audio.AudioSupplier;
import com.Model.BaseUnEditableModel;
import com.Networking.Utility.BaseUser;
import com.Util.Algorithms;
import com.Util.Checker;
import com.Util.Resources;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.clientLogger;

public class AudioPlayer extends Magnitafon implements ChangeableOutput, Playable {

    private final Map<Integer, SourceDataLine> outputs;
    private final CallNotificator callNotificator;
    private volatile Mixer.Info outputInfo;

    public AudioPlayer() {
        outputs = new HashMap<>();
        callNotificator = new CallNotificator();
    }

    @Override
    public synchronized void changeOutput(Mixer.Info mixerInfo) {
        if (mixerInfo == null && outputInfo == null)
            mixerInfo = AudioSupplier.getDefaultForOutput();
        outputInfo = mixerInfo;
        changeOutputs();
        callNotificator.changeOutput(outputInfo);
    }

    private synchronized void addOutput(int id) {
        try {
            outputs.put(id, AudioSupplier.getOutput(outputInfo));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            //Will be thrown when audio line is already opened
            //Never happens to me, so don't know when this can happen and what to do
            //Fucking happen and I can't for long time understand what was wrong
            //Works like:
            //If you already have open line from a mixer
            //and you trying to open new line from the mixer
            //it trows the exception
        }
    }

    private synchronized void removeOutput(int id) {
        SourceDataLine remove = outputs.remove(id);
        if (remove == null)
            return;
        remove.close();
    }

    @Override
    public void changeVolume(int who, int percentage) {
        SourceDataLine sourceDataLine = outputs.get(who);
        if (sourceDataLine == null)
            throw new NullPointerException("There is no such line " + who);
        if (!sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN))
            return;
        FloatControl control = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        changeVolume(control, percentage);
    }

    private void changeVolume(FloatControl control, int percentage) {
        int minimum = (int) Math.ceil(control.getMinimum());
        int maximum = (int) Math.floor(control.getMaximum());
        int mean = Algorithms.findPercentage(minimum, maximum, percentage);
        control.setValue(mean);
    }

    @Override
    public synchronized void playSound(int from, byte[] data) {
        SourceDataLine sourceDataLine = outputs.get(from);
        if (sourceDataLine == null) {
            //Just ignore it until update is occurs
            return;
        }
        playData(sourceDataLine, data);
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
        playMessageSound(track);
    }

    @Override
    public void playMessage(int track, int delay) {
        try {
            Thread.sleep(delay);
            playMessage(track);
        } catch (InterruptedException ignored) {
            //won't happen
        }
    }

    @Override
    public synchronized void playCall() {
        callNotificator.start("Call sound thread");
    }

    @Override
    public synchronized void stopCall() {
        callNotificator.close();
    }

    @Override
    public void init() {
        changeOutput(null);
    }

    @Override
    public synchronized void close() {
        outputs.forEach((integer, sourceDataLine) -> sourceDataLine.close());
        outputs.clear();
    }

    @Override
    public synchronized void update(BaseUnEditableModel model) {
        Set<BaseUser> conversation = model.getConversation();
        Map<Integer, BaseUser> userMap = model.getUserMap();

        Map<Integer, SourceDataLine> tmp = new HashMap<>();

        clientLogger.logp(Level.FINER, this.getClass().getName(), "update",
                "Changing amount outputs in conversation from - " +
                        Arrays.toString(outputs.keySet().toArray())
                        + ", to - " + Arrays.toString(conversation.toArray())
        );

        outputs.forEach((integer, sourceDataLine) -> {
            if (conversation.contains(userMap.get(integer)))
                tmp.put(integer, sourceDataLine);
            else
                sourceDataLine.close();
        });
        outputs.clear();
        outputs.putAll(tmp);

        conversation.forEach(user -> {
            if (!outputs.containsKey(user.getId()))
                addOutput(user.getId());
        });

        clientLogger.logp(Level.FINER, this.getClass().getName(), "update",
                "Changed outputs result is - " + Arrays.toString(outputs.keySet().toArray()));
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
             SourceDataLine sourceDataLine = AudioSupplier.getOutputForFile(outputInfo, inputStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {

            Player.playLoop(audioInputStream, sourceDataLine);

        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void changeOutputs() {
        outputs.forEach((integer, sourceDataLine) -> {
            sourceDataLine.close();
            addOutput(integer);
        });

    }

}
