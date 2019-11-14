package com.Audio;

import com.Audio.Input.Capture;
import com.Audio.Input.DefaultMic;
import com.Audio.Output.AudioPlayer;
import com.Audio.Output.Playable;
import com.Model.BaseUnEditableModel;
import com.Networking.Utility.BaseUser;
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

public class Audio implements UpdaterAndHandler, ActionableLogic {

    private final Playable player;
    private final DefaultMic mic;
    private final ActionableLogic audioLogic;

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


    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        switch (action) {
            case INCOMING_MESSAGE: {
                onIncomingMessage(stringData);
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
                player.playSound(intData, bytesData);
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
    public void act(BUTTONS button, Object plainData, String stringData, int integerData) {
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
                onIncomingMessage(stringData);
                break;
            }

        }

        audioLogic.act(button, plainData, stringData, integerData);
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
