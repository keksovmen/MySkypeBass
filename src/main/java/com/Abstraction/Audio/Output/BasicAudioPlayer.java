package com.Abstraction.Audio.Output;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Util.Resources.AbstractResources;
import com.Abstraction.Util.Resources.Resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Basic version
 * Handles messages on pool threaded way
 */

public abstract class BasicAudioPlayer extends AbstractAudioPlayer {

    /**
     * For non blocking main thread when playing message notifications
     */

    protected final ExecutorService executorService;

    /**
     * Template method patter
     * override 1 factory method
     */

    public BasicAudioPlayer() {
        executorService = createExecutor();
    }

    /**
     * Trying to play a random message
     */

    @Override
    public void playMessage() {
        int size = Resources.getInstance().getNotificationTracks().size();
        if (size == 0)
            return;
        int track = ThreadLocalRandom.current().nextInt(0, size);
        playMessage(track);
    }

    /**
     * Trying to play particular message
     *
     * @param track id of track in {@link AbstractResources#getNotificationTracks()}
     */

    @Override
    public void playMessage(int track) {
        if (Resources.getInstance().getNotificationTracks().size() <= track || track < 0) {
            playMessage(); //Play random one
            return;
        }
        executorService.execute(() -> AudioSupplier.getInstance().playResourceFile(outputDeviceId, track));
    }

    /**
     * Tries to play particular sound with given delay
     *
     * @param track id in {@link AbstractResources#getNotificationTracks()}
     * @param delay time in millis
     */

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

    /**
     * Factory method
     *
     * @return executor for messages
     */

    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool();
    }

//    /**
//     * LISTEN UP FOLKS:
//     * IF YOU SWAP 2 LINES - SourceDataLine and AudioInputStream
//     * YOU WILL GET EXCEPTION IN AudioSupplier.getOutputForFile();
//     * SOMEHOW AUDIO SYSTEM RUINS PREVIOUS INPUT STREAM AND IT COUNTS AS INVALID
//     * BE AWARE
//     *
//     * @param track id of sound
//     */
//
//    private void playMessageSound(int track) {
//        try (BufferedInputStream inputStream = new BufferedInputStream(
//                Checker.getCheckedInput(Resources.getInstance().getMessagePaths().get(track)));
//             AudioOutputLine sourceDataLine = AudioSupplier.getInstance().getOutputForFile(outputDeviceId, inputStream);
//             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {
//
//            Player.playWholeFile(audioInputStream, sourceDataLine);
//
//        } catch (IOException | AudioLineException | UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        }
//    }

}
