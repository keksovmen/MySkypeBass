package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;

/**
 * Contain actions for base message types
 */

public class Processor implements Processable{

    private ActionStand onUsers;
    private ActionStand onMessage;
    private ActionStand onCall;
//    private ActionStand onUsers;


    public Processor() {
        onUsers = new ActionStand();
        onMessage = new ActionStand();
        onCall = new ActionStand();
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
            //And so on
        }
        return false;
    }
}
