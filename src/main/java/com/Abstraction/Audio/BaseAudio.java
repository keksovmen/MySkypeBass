package com.Abstraction.Audio;

import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Audio.Input.DefaultMic;
import com.Abstraction.Audio.Settings.BaseAudioSettings;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.Playable;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Collection.Pair;

import java.util.List;

/**
 * Handles incoming {@link ACTIONS}
 */

public class BaseAudio implements SimpleComponent {

    /**
     * To play notifications and incoming sound
     */

    protected final Playable player;

    /**
     * To start and close mic thread
     */

    protected final DefaultMic mic;

    /**
     * To delegate some {@link BUTTONS} actions
     */

    protected final BaseAudioSettings audioSettings;

    /**
     *
     * @param helpHandlerPredecessor chain of responsibility pattern
     * @param factory for platform independence
     */

    public BaseAudio(ButtonsHandler helpHandlerPredecessor, AudioFactory factory) {
        AbstractAudioPlayer abstractPlayer = factory.createPlayer();
        AbstractMicrophone abstractMicrophone = factory.createMicrophone(helpHandlerPredecessor);
        BaseAudioSettings abstractSettings = factory.createSettings(abstractMicrophone, abstractPlayer);

        player = abstractPlayer;
        mic = abstractMicrophone;
        audioSettings = abstractSettings;

    }

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
    public void modelObservation(UnEditableModel model) {
        player.modelObservation(model);
    }

    /**
     * Handle what you can by yourself
     * then delegate to audioLogic
     *
     * @param button command
     * @param data   see BUTTONS enum for more info
     */

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
                onExitConversation();
                break;
            }
            case DISCONNECT: {
                onExitConversation();
                break;
            }
            case PREVIEW_SOUND: {
                onIncomingMessage((String) data[0]);
                break;
            }

        }

        audioSettings.handleRequest(button, data);
    }


    protected void dropCallAction() {
        player.stopCall();
    }

    /**
     *
     * @param message that may contain meta info as {@link FormatWorker#retrieveMessageMeta(String)}
     */

    protected void onIncomingMessage(String message) {
        List<Pair<Integer, Integer>> pairs = FormatWorker.retrieveMessageMeta(message);
        if (pairs.size() == 0) {
            player.playMessage();
        } else {
            pairs.forEach(pair -> player.playMessage(pair.getFirst(), pair.getSecond()));
        }
    }

    protected void onCallAccepted() {
        dropCallAction();
        mic.start("Microphone capture");
    }

    protected void onExitConversation() {
        mic.close();
    }
}
