package com.Audio;

import com.Audio.Input.Capture;
import com.Audio.Input.DefaultMic;
import com.Audio.Output.AudioPlayer;
import com.Audio.Output.Playable;
import com.Client.ButtonsHandler;
import com.Client.LogicObserver;
import com.Model.BaseUnEditableModel;
import com.Model.Updater;
import com.Networking.Utility.Users.BaseUser;
import com.Pipeline.ACTIONS;
import com.Pipeline.ActionableLogic;
import com.Pipeline.BUTTONS;
import com.Pipeline.UpdaterAndHandler;
import com.Util.FormatWorker;
import com.Util.Pair;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Audio implements Updater, LogicObserver, ButtonsHandler {

    private final Playable player;
    private final DefaultMic mic;
    private final ButtonsHandler audioLogic;

    private final ExecutorService executorService;

    public Audio(Consumer<byte[]> sendData) {
        AudioPlayer audioPlayer = new AudioPlayer();
        Capture capture = new Capture(sendData);
        AudioSettings settings = new AudioSettings(capture, audioPlayer);

        executorService = Executors.newCachedThreadPool();

        player = audioPlayer;
        mic = capture;
        audioLogic = settings;

    }

    //    private final


    @Override
    public void observe(ACTIONS action, Object[] data) {
        switch (action) {
            case INCOMING_MESSAGE: {
                onIncomingMessage((String) data[1]);
                return;
            }
            case INCOMING_CALL: {
                player.playCall();
                return;
            }
            case OUT_CALL: {
                player.playCall();
                return;
            }
            case CALL_ACCEPTED: {
                onCallAccepted();
                return;
            }
            case CALL_DENIED: {
                dropCallAction();
                return;
            }
            case CALL_CANCELLED: {
                dropCallAction();
                return;
            }
            case AUDIO_FORMAT_ACCEPTED: {
                player.init();
                mic.init();
                return;
            }
            case INCOMING_SOUND: {
                player.playSound((int) data[2], (byte[]) data[1]);
                return;
            }
            case EXITED_CONVERSATION: {
                onExitConversation();
                return;
            }
            case DISCONNECTED: {
                onExitConversation();
                player.stopCall();
                return;
            }
            case BOTH_IN_CONVERSATION: {
                dropCallAction();
                return;
            }
        }
    }

    @Override
    public void update(BaseUnEditableModel model) {
        player.update(model);
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        switch (button) {
            case CALL_CANCELLED: {
                dropCallAction();
                break;
            }
            case CALL_DENIED: {
                dropCallAction();
                break;
            }
            case EXIT_CONFERENCE: {
                mic.close();
                break;
            }
            case DISCONNECT: {
                mic.close();
                break;
            }
            case PREVIEW_SOUND:{
                onIncomingMessage((String) data[0]);
                break;
            }

        }

        audioLogic.handleRequest(button, data);
    }

    private void dropCallAction() {
        player.stopCall();
    }

    private void onIncomingMessage(String message) {
        List<Pair<Integer, Integer>> pairs = FormatWorker.retrieveMessageMeta(message);
        if (pairs.size() == 0) {
            executorService.execute(player::playMessage);
            return;
        }
        pairs.forEach(pair ->
                executorService.execute(() ->
                        player.playMessage(pair.getFirst(), pair.getSecond())
                ));
    }

    private void onCallAccepted(/*BaseUser dude, String others*/) {
        dropCallAction();
        mic.start("Microphone capture");
    }

    private void onExitConversation() {
        mic.close();
    }
}
