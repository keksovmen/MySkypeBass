package Bin.Networking.Utility;

import Bin.Audio.CallNotificator;

import java.util.concurrent.atomic.AtomicBoolean;

public class Call implements ErrorHandler {

    private final AtomicBoolean isCalling;
    private BaseUser receiver;
    private CallNotificator sound;

    public Call() {
        isCalling = new AtomicBoolean();
        sound = new CallNotificator();
    }

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
