package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;

/**
 * Contain actions for base message types
 */

public class Processor implements Processable{

    private ActionStand onUsers;
    private ActionStand onMessage;
    private ActionStand onCall;
    private ActionStand onDisconnect;
    private ActionStand onCallAccept;
    private ActionStand onCallDeny;
    private ActionStand onCallCancel;
    private ActionStand onExitConference;


    public Processor() {
        onUsers = new ActionStand();
        onMessage = new ActionStand();
        onCall = new ActionStand();
        onDisconnect = new ActionStand();
        onCallAccept = new ActionStand();
        onCallDeny = new ActionStand();
        onCallCancel = new ActionStand();
        onExitConference = new ActionStand();
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
            //And so on
        }
        return false;
    }
}
