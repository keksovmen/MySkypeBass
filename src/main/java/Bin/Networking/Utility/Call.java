package Bin.Networking.Utility;

import Bin.Audio.CallNotificator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Need on client side
 * Purpose is to be a flag that can hold some data
 * Access only from one thread at a time
 */

public class Call implements ErrorHandler {

    /**
     * The flag showing if you are currently calling or get called someone
     */

    private final AtomicBoolean isCalling;

    /**
     * Who called or you call
     */

    private BaseUser receiver;

    /**
     * Handle sound notifications
     */

    private CallNotificator sound;

    public Call() {
        isCalling = new AtomicBoolean();
        sound = new CallNotificator();
    }

    /**
     * Not used version
     *
     * @param receiver who called or you call
     */

    public Call(BaseUser receiver) {
        isCalling = new AtomicBoolean();
        this.receiver = receiver;
    }

    public boolean isCalling() {
        return isCalling.get();
    }

    public BaseUser getReceiver() {
        return receiver;
    }

    public void setCalling(boolean value) {
        sound.stop();
        isCalling.set(value);
    }

    /**
     * Define what sound to play depends on isIncoming
     *
     * @param value      calling state
     * @param isIncoming mean you called or not
     */

    public void setCalling(boolean value, boolean isIncoming) {
        if (value) {
            if (isIncoming) {
                sound.playIncoming();
            } else {
                sound.playOutComing();
            }
        } else {
            sound.stop();
        }
        isCalling.set(value);
    }

    public void setReceiver(BaseUser receiver) {
        this.receiver = receiver;
    }

    @Override
    public void errorCase() {
        receiver = null;
        setCalling(false);
        iterate();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }
}
