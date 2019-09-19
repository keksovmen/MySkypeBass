package Com.GUI.Interfaces;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ConferencePaneActions {

    /**
     * Action for end a call
     *
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Runnable endCall() throws IllegalStateException;

    /**
     * Action for mute the microphone
     *
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Supplier<Boolean> mute() throws IllegalStateException;

    /**
     * Action for changing magnitude of bass boost
     *
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<Double> changeMultiplier() throws IllegalStateException;

    /**
     * Action for sending message to all who in the conversation
     *
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<String> sendMessageToConference() throws IllegalStateException;

    void updateEndCall(Runnable endCall);

    void updateMute(Supplier<Boolean> mute);

    void updateChangeMultiplier(Consumer<Double> changeMultiplier);

    void updateSendMessageToConference(Consumer<String> sendMessageToConference);
}
