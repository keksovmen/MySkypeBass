package com.Abstraction.Networking.Processors;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.Logic;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;

import java.io.IOException;

/**
 * Represent client side networkHelper for incoming messages from server
 * But doesn't handle reading these messages from Input stream, only handle their meaning
 */

public class ClientProcessor implements Processable {

    /**
     * Where made user changes
     */

    protected final ChangeableModel model;

//    /**
//     * When you making call you have to be properly synchronised
//     * It lets you do that
//     */
//
//    private final ClientUser user;

    /**
     * To notify LogicObserver about changes
     */

    private final Logic logic;


    public ClientProcessor(ChangeableModel model, /*ClientUser user,*/ Logic logic) {
        this.model = model;
//        this.user = user;
//        this.user = model.getMyself();// will not be changed during life time of Processor
        this.logic = logic;
    }

    /**
     * Routes to proper networkHelper method
     *
     *
     * @param dataPackage incoming data
     * @return false in 1 case, when you can't handle given request, indicates end of loop
     */

    @Override
    public boolean process(AbstractDataPackage dataPackage) {
        switch (dataPackage.getHeader().getCode()) {
            case SEND_USERS:
                return onUsersRequest(dataPackage);
            case SEND_MESSAGE:
                return onIncomingMessage(dataPackage);
            case SEND_CALL:
                return onIncomingCall(dataPackage);
            case SEND_SOUND:
                return onSendSound(dataPackage);
            case SEND_DISCONNECT:
                return false; //don't do much, just indicate end of loop, exception handlers will handle
            case SEND_ADD_TO_CONVERSATION:
                return onAddToConversation(dataPackage);
            case SEND_REMOVE_FROM_CONVERSATION:
                return onRemoveDudeFromConversation(dataPackage);
            case SEND_DISCONNECT_FROM_CONV:
                return onExitConversation(dataPackage);
            case SEND_ADD_TO_USER_LIST:
                return onAddToUserList(dataPackage);
            case SEND_REMOVE_FROM_USER_LIST:
                return onRemoveFromUserList(dataPackage);
            case SEND_ACCEPT_CALL:
                return onCallAccept(dataPackage);
            case SEND_DENY_CALL:
                return onCallDeny(dataPackage);
            case SEND_CANCEL_CALL:
                return onCallCanceled(dataPackage);
            case SEND_BOTH_IN_CONVERSATIONS:
                return onBothInConversation(dataPackage);
            case SEND_ADD_WHOLE_CONVERSATION:
                return onAddWholeConversation(dataPackage);
        }
        return false;
    }

    /**
     * Called by Owner -> Handler, when network connection dies
     */

    @Override
    public void close() {
        model.getMyself().drop();
        model.clear();
        model.clearConversation();
        logic.notifyObservers(ACTIONS.DISCONNECTED, null);
    }

    /**
     * Update model
     *
     * @param dataPackage contain String with users
     * @return true
     */

    protected boolean onUsersRequest(AbstractDataPackage dataPackage) {
        String users = dataPackage.getDataAsString();
        model.addToModel(BaseUser.parseUsers(users));
        return true;
    }


    protected boolean onIncomingMessage(AbstractDataPackage dataPackage) {
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.INCOMING_MESSAGE, new Object[]{
                sender,
                dataPackage.getDataAsString(),
                dataPackage.getHeader().getTo() == WHO.CONFERENCE.getCode() ? 1 : 0
        });
        return true;
    }

    /**
     * First lock the user then handle incoming call
     * And don't forget to unlock, 'cause once I did
     *
     * @param dataPackage contain data
     * @return true
     */

    protected boolean onIncomingCall(AbstractDataPackage dataPackage) {
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        ClientUser myself = model.getMyself();

        myself.lock();
        if (myself.isCalling() != ClientUser.NO_ONE) {
            //Auto deny because you already calling and send some shit
            //that will tell that you wre called
            try {
                myself.getWriter().writeDeny(sender.getId());
                logic.notifyObservers(ACTIONS.CALLED_BUT_BUSY, new Object[]{sender});
            } catch (IOException ignored) {
            } finally {
                myself.unlock();
            }
            return true;
        }
        myself.call(sender.getId());
        myself.unlock();

        String dudesInConv = dataPackage.getDataAsString();
        logic.notifyObservers(ACTIONS.INCOMING_CALL, new Object[]{
                sender, dudesInConv
        });
        return true;
    }

    protected boolean onSendSound(AbstractDataPackage dataPackage) {
        int from = dataPackage.getHeader().getFrom();
        logic.notifyObservers(ACTIONS.INCOMING_SOUND, new Object[]{
                model.getUserMap().get(from),
                dataPackage.getData(),
                from
        });
        return true;
    }

    protected boolean onAddToConversation(AbstractDataPackage dataPackage) {
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        model.addToConversation(baseUser);
        return true;
    }

    protected boolean onRemoveDudeFromConversation(AbstractDataPackage dataPackage) {
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        model.removeFromConversation(baseUser);
        return true;
    }

    protected boolean onExitConversation(AbstractDataPackage dataPackage) {
        logic.notifyObservers(ACTIONS.EXITED_CONVERSATION, null);
        model.clearConversation();
        AbstractDataPackagePool.clearStorage();
        return true;
    }

    protected boolean onAddToUserList(AbstractDataPackage dataPackage) {
        String user = dataPackage.getDataAsString();
        model.addToModel(BaseUser.parse(user));
        return true;
    }

    protected boolean onRemoveFromUserList(AbstractDataPackage dataPackage) {
        int user = dataPackage.getDataAsInt();
        model.removeFromModel(user);
        return true;
    }

    protected boolean onCallAccept(AbstractDataPackage dataPackage) {
        model.getMyself().drop();
        BaseUser dude = model.getUserMap().get(dataPackage.getHeader().getFrom());
        AbstractClient.callAcceptRoutine(logic, model, dude);
        return true;
    }

    protected boolean onCallDeny(AbstractDataPackage dataPackage) {
        model.getMyself().drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_DENIED, new Object[]{new BaseUser(baseUser.getName(), baseUser.getId())});
        return true;
    }

    protected boolean onCallCanceled(AbstractDataPackage dataPackage) {
        model.getMyself().drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_CANCELLED, new Object[]{new BaseUser(baseUser.getName(), baseUser.getId())});
        return true;
    }

    protected boolean onBothInConversation(AbstractDataPackage dataPackage) {
        ClientUser myself = model.getMyself();

        if (myself.isCalling() == dataPackage.getHeader().getFrom())
            myself.drop();
        logic.notifyObservers(ACTIONS.BOTH_IN_CONVERSATION, new Object[]{
                model.getUserMap().get(dataPackage.getHeader().getFrom())
        });
        return true;
    }

    protected boolean onAddWholeConversation(AbstractDataPackage dataPackage){
        BaseUser[] baseUsers = BaseUser.parseUsers(dataPackage.getDataAsString());
        for (BaseUser user : baseUsers) {
            model.addToConversation(user);
        }
        return true;
    }
}
