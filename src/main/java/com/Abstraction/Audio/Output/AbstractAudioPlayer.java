package com.Abstraction.Audio.Output;

import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.BaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractAudioPlayer extends Magnitafon implements ChangeableOutput, Playable {

    protected final Map<Integer, AudioOutputLine> outputLines;
    protected final AbstractCallNotificator callNotificator;

    /**
     * For not blocking main thread while playing message notifications
     */

    protected final ExecutorService executorService;

    protected volatile int outputMixerId;

    public AbstractAudioPlayer() {
        outputLines = createLinesStorage();
        callNotificator = createCallNotificator();
        executorService = createExecutor();
    }

    /**
     * Factory method
     *
     * @return your storage for lines
     */

    protected Map<Integer, AudioOutputLine> createLinesStorage() {
        return new HashMap<>();
    }

    /**
     * Factory method
     *
     * @return call notificator for your platform
     */

    protected abstract AbstractCallNotificator createCallNotificator();

    /**
     * Factory method
     *
     * @return executor for messages
     */

    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool();
    }

    /**
     * Trying to change volume of particular dude
     *
     * @param who        the dude
     * @param percentage from 0 to 100 %
     */

    @Override
    public void changeVolume(int who, int percentage) {
        AudioOutputLine outputLine = outputLines.get(who);
        if (outputLine == null)
            throw new NullPointerException("There is no such line " + who);
        if (!outputLine.isVolumeChangeSupport())
            return;
        outputLine.setVolume(percentage);
    }

    /**
     * Changes output device
     *
     * @param indexOfParticularMixer index of desired output
     */

    @Override
    public synchronized void changeOutput(int indexOfParticularMixer) {
        outputMixerId = indexOfParticularMixer;
        outputLines.forEach((integer, line) -> {
            line.close();
            addOutput(integer);
        });
        callNotificator.changeOutput(indexOfParticularMixer);
    }

    @Override
    public synchronized void playSound(int who, byte[] data) {
        AudioOutputLine outputLine = outputLines.get(who);
        if (outputLine == null)            //Just ignore it until update is occurs
            return;
        playData(outputLine, data);
    }

    @Override
    public void playMessage(int track, int delay) {
        executorService.execute(() -> {
            try {
                Thread.sleep(delay);
                playMessage(track);
            } catch (InterruptedException ignored) {
                //won't happen
            }
        });

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
    public synchronized void close() {
        outputLines.forEach((integer, line) -> line.close());
        outputLines.clear();
    }

    @Override
    public synchronized void update(UnEditableModel model) {
        Set<BaseUser> conversation = model.getConversation();
        Map<Integer, BaseUser> userMap = model.getUserMap();

        Map<Integer, AudioOutputLine> tmp = new HashMap<>();

        outputLines.forEach((integer, outputLine) -> {
            if (conversation.contains(userMap.get(integer)))
                tmp.put(integer, outputLine);
            else
                outputLine.close();
        });
        outputLines.clear();
        outputLines.putAll(tmp);

        conversation.forEach(user -> {
            if (!outputLines.containsKey(user.getId()))
                addOutput(user.getId());
        });
    }

    @Override
    public void init() {
        changeOutput(AudioSupplier.getInstance().getDefaultForOutput());
    }

    private synchronized void addOutput(int id) {
        try {
            outputLines.put(id, AudioSupplier.getInstance().getOutput(outputMixerId));
        } catch (AudioLineException e) {
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


}
