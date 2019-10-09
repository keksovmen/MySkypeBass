package com.Networking.Processors;

import com.Networking.Protocol.AbstractDataPackage;

/**
 * Contain actions for base message types
 */

public class Processor implements Processable{

    private final ActionStand onUsers;
    private final ActionStand onMessage;
    private final ActionStand onCall;
    private final ActionStand onDisconnect;
    private final ActionStand onCallAccept;
    private final ActionStand onCallDeny;
    private final ActionStand onCallCancel;
    private final ActionStand onExitConference;
    private final ActionStand onSendSound;


    public Processor() {
        onUsers = new ActionStand();
        onMessage = new ActionStand();
        onCall = new ActionStand();
        onDisconnect = new ActionStand();
        onCallAccept = new ActionStand();
        onCallDeny = new ActionStand();
        onCallCancel = new ActionStand();
        onExitConference = new ActionStand();
        onSendSound = new ActionStand();
    }

    public ActionStand getOnUsers() {
        return onUsers;
    }

    public ActionStand getOnMessage() {
        return onMessage;
    }

    public ActionStand getOnCall() {
        return onCall;
    }

    public ActionStand getOnDisconnect() {
        return onDisconnect;
    }

    public ActionStand getOnCallAccept() {
        return onCallAccept;
    }

    public ActionStand getOnCallDeny() {
        return onCallDeny;
    }

    public ActionStand getOnCallCancel() {
        return onCallCancel;
    }

    public ActionStand getOnExitConference() {
        return onExitConference;
    }

    public ActionStand getOnSendSound() {
        return onSendSound;
    }

    /**
     * Route the data package to an appropriate handler
     *
     * @param dataPackage to route
     * @return true if routed false otherwise
     */

    @Override
    public boolean process(AbstractDataPackage dataPackage) {
        if (dataPackage == null)
            throw new NullPointerException("Data package can't be null, ruins invariant");
        switch (dataPackage.getHeader().getCode()){
            case SEND_USERS:{
                onUsers.process(dataPackage);
                return true;
            }
            case SEND_MESSAGE:{
                onMessage.process(dataPackage);
                return true;
            }
            case SEND_CALL:{
                onCall.process(dataPackage);
                return true;
            }
            case SEND_DISCONNECT:{
                onDisconnect.process(dataPackage);
                return true;
            }
            case SEND_ACCEPT_CALL:{
                onCallAccept.process(dataPackage);
                return true;
            }
            case SEND_DENY_CALL:{
                onCallDeny.process(dataPackage);
                return true;
            }
            case SEND_CANCEL_CALL:{
                onCallCancel.process(dataPackage);
                return true;
            }
            case SEND_DISCONNECT_FROM_CONV:{
                onExitConference.process(dataPackage);
                return true;
            }
            case SEND_SOUND:{
                onSendSound.process(dataPackage);
                return true;
            }
            //And so on
        }
        return false;
    }
}
