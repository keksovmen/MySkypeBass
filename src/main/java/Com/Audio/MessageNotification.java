package Com.Audio;

import Com.Util.Checker;
import Com.Util.XMLWorker;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Purpose is to play message notifications
 */

class MessageNotification {

    private final List<String> notificationPaths;

    MessageNotification() {
        notificationPaths = new ArrayList<>();
        fillPaths();
    }

    /**
     * Fills the array with resources locations
     * Files.list doesn't work with resources!!!!!!
     * When add a new sound must specify it here
     * <p>
     * FIND A WAY HOW TO READ DIRECTORY WITH RESOURCES
     * AND GET CONTENT SEPARATELY WITHOUT DIRECTLY SPECIFYING NAMES
     * <p>
     * Update: find not what I wanted but still good enough
     * don't need to recompile each time when add a new sound
     */

    private void fillPaths() {
        XMLWorker.retrieveNames("/sound/Notifications.xml").
                forEach(s -> notificationPaths.add("/sound/messageNotification/" + s));
    }

    /**
     * STARTS NEW THREAD
     * Play message notification
     * each time opens new line
     * can't be stopped until the end of the sound
     *
     * @param idOfTrack track id in notificationPaths
     */

    private void playMessageSound(int idOfTrack) {
        new Thread(() -> {
            try (BufferedInputStream inputStream = new BufferedInputStream(
                    Checker.getCheckedInput(notificationPaths.get(idOfTrack)));
                 SourceDataLine sourceDataLine = AudioLineProvider.getFromInput(inputStream);
                 AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {

                //open source data line in default mixer
                //start playing sound will close input stream and source line
                Player.playLoop(audioInputStream, sourceDataLine);

            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }, "Message notifier").start();
    }

    /**
     * Plays random message notification
     */

    void playRandomMessageSound() {
        playMessageSound(ThreadLocalRandom.current().nextInt(notificationPaths.size()));
    }

    /**
     * Plays particular message notification
     *
     * @param indexOfTrack track id in soundNotifications
     */

    void playIndexedMessageSound(int indexOfTrack) {
        if (indexOfTrack < 0 || indexOfTrack >= notificationPaths.size()) {
            playRandomMessageSound();
        } else {
            playMessageSound(indexOfTrack);
        }
    }
}
