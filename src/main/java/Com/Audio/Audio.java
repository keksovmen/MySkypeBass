package Com.Audio;

import Com.Audio.Input.Capture;
import Com.Audio.Input.DefaultMic;
import Com.Audio.Output.AudioPlayer;
import Com.Audio.Output.Playable;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.ActionsHandler;
import Com.Pipeline.BUTTONS;
import Com.Util.FormatWorker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Audio implements ActionsHandler, ActionableLogic {

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

    //    private final


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
            case REMOVE_DUDE_FROM_CONVERSATION: {
                player.removeOutput(intData);
                return;
            }
            case ADD_DUDE_TO_CONVERSATION: {
                player.addOutput(from.getId());
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
                return;
            }
        }
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
            case CALL: {
                player.playCall();
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

        }

        audioLogic.act(button, plainData, stringData, integerData);
    }

    private void dropCallAction() {
        player.stopCall();
    }

    private void onIncomingMessage(String message) {
        List<Integer> integers = FormatWorker.retrieveMessageMeta(message);
        if (integers.size() == 0) {
            executorService.execute(player::playMessage);
            return;
        }
        integers.forEach(integer ->
                executorService.execute(() ->
                        player.playMessage(integer)
                ));
    }

    private void onCallAccepted(/*BaseUser dude, String others*/) {
//        player.addOutput(dude.getId());
//        for (BaseUser baseUser : BaseUser.parseUsers(others)) {
//            player.addOutput(baseUser.getId());
//        }
        dropCallAction();
        mic.start("Microphone capture");
    }

    private void onExitConversation(){
        player.close();
        mic.close();
    }
}
