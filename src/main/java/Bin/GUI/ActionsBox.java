package Bin.GUI;

import Bin.GUI.Interfaces.MainFrameActions;
import Bin.Networking.Utility.BaseUser;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Purpose is to have all actions for each GUI element in 1 place
 * First you need to initialise them with background actions
 * Then update on each GUI element by passing it through them
 * They will take what them need
 */

public class ActionsBox implements MainFrameActions {

    /**
     * Obtain name and id after you successfully connected to a server
     */

    private Supplier<String> nameAndId;

    /**
     * Cancel call in call dialog box
     */

    private Consumer<BaseUser> cancelCall;

    /**
     * Accept call in call dialog box
     */

    private Consumer<BaseUser[]> acceptCall;

    /**
     * Deny call in call dialog box
     */

    private Consumer<BaseUser> denyCall;

    /**
     * End call in conference pane
     */

    private Runnable endCall;

    /**
     * Mute your mic on conference pane
     */

    private Supplier<Boolean> mute;

    /**
     * Connect to a server in first skin
     */

    private Function<String[], Boolean> connect;

    /**
     * Create a server on audio format stats
     */

    private Function<String[], Boolean> createServer;

    /**
     * Disconnect on second skin
     */

    private Runnable disconnect;

    /**
     * Refresh users in popup menu on second skin
     */

    private Runnable callForUsers;

    /**
     * Send message from third skin
     */

    private BiConsumer<Integer, String> sendMessage;

    /**
     * Close third skin
     */

    private Runnable closeTab;

    /**
     * Call some one on second skin
     */

    private Consumer<BaseUser> callSomeOne;

    /**
     * Change bass boost value on audio capture
     */

    private Consumer<Double> changeMultiplier;


    @Override
    public Supplier<String> nameAndId() {
        if (nameAndId == null) {
            throw new IllegalStateException("Name and ID is not initialised");
        }
        return nameAndId;
    }

    @Override
    public Consumer<BaseUser> cancelCall() {
        if (cancelCall == null) {
            throw new IllegalStateException("Cancel call is not initialised");
        }
        return cancelCall;
    }

    @Override
    public Consumer<BaseUser[]> acceptCall() {
        if (acceptCall == null) {
            throw new IllegalStateException("Accept call is not initialised");
        }
        return acceptCall;
    }

    @Override
    public Consumer<BaseUser> denyCall() {
        if (denyCall == null) {
            throw new IllegalStateException("Deny call is not initialised");
        }
        return denyCall;
    }

    @Override
    public Runnable endCall() {
        if (endCall == null) {
            throw new IllegalStateException("End call is not initialised");
        }
        return endCall;
    }

    @Override
    public Supplier<Boolean> mute() {
        if (mute == null) {
            throw new IllegalStateException("Mute is not initialised");
        }
        return mute;
    }

    @Override
    public Function<String[], Boolean> connect() {
        if (connect == null) {
            throw new IllegalStateException("Connect is not initialised");
        }
        return connect;
    }

    @Override
    public Function<String[], Boolean> createServer() {
        if (createServer == null) {
            throw new IllegalStateException("Create server is not initialised");
        }
        return createServer;
    }

    @Override
    public Runnable disconnect() {
        if (disconnect == null) {
            throw new IllegalStateException("Disconnect is not initialised");
        }
        return disconnect;
    }

    @Override
    public Runnable callForUsers() {
        if (callForUsers == null) {
            throw new IllegalStateException("Call for users is not initialised");
        }
        return callForUsers;
    }

    @Override
    public BiConsumer<Integer, String> sendMessage() {
        if (sendMessage == null) {
            throw new IllegalStateException("Send message is not initialised");
        }
        return sendMessage;
    }

    @Override
    public Runnable closeTab() {
        if (closeTab == null) {
            throw new IllegalStateException("Close tab is not initialised");
        }
        return closeTab;
    }

    @Override
    public Consumer<BaseUser> callSomeOne() {
        if (callSomeOne == null) {
            throw new IllegalStateException("Call some one is not initialised");
        }
        return callSomeOne;
    }

    @Override
    public Consumer<Double> changeMultiplier() {
        if (changeMultiplier == null) {
            throw new IllegalStateException("Change multiplier one is not initialised");
        }
        return changeMultiplier;
    }

    @Override
    public void updateConnect(Function<String[], Boolean> connect) {
        this.connect = connect;
    }

    @Override
    public void updateCreateServer(Function<String[], Boolean> createServer) {
        this.createServer = createServer;
    }

    @Override
    public void updateDisconnect(Runnable disconnect) {
        this.disconnect = disconnect;
    }

    @Override
    public void updateCallForUsers(Runnable callForUsers) {
        this.callForUsers = callForUsers;
    }

    @Override
    public void updateCancelCall(Consumer<BaseUser> cancelCall) {
        this.cancelCall = cancelCall;
    }

    @Override
    public void updateAcceptCall(Consumer<BaseUser[]> acceptCall) {
        this.acceptCall = acceptCall;
    }

    @Override
    public void updateDenyCall(Consumer<BaseUser> denyCall) {
        this.denyCall = denyCall;
    }

    @Override
    public void updateEndCall(Runnable endCall) {
        this.endCall = endCall;
    }

    @Override
    public void updateMute(Supplier<Boolean> mute) {
        this.mute = mute;
    }

    @Override
    public void updateSendMessage(BiConsumer<Integer, String> sendMessage) {
        this.sendMessage = sendMessage;
    }

    @Override
    public void updateCloseTab(Runnable closeTab) {
        this.closeTab = closeTab;
    }

    @Override
    public void updateNameAndId(Supplier<String> nameAndId) {
        this.nameAndId = nameAndId;
    }

    @Override
    public void updateCallSomeOne(Consumer<BaseUser> callSomeOne) {
        this.callSomeOne = callSomeOne;
    }

    @Override
    public void updateChangeMultiplier(Consumer<Double> changeMultiplier) {
        this.changeMultiplier = changeMultiplier;
    }

    /**
     * Check if all fields are not null
     * CAUTION DO NOT ADD ANY FIELDS TO THIS CLASS AND ITS SONS!
     * Uses reflections
     *
     * @return true if all actions are initialised
     */

    public boolean isInit() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                if (field.get(this) == null) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
