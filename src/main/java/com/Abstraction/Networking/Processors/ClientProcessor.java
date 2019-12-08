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

public class ClientProcessor implements Processable {

    private final ChangeableModel model;
    private final ClientUser user;
    private final Logic logic;

    public ClientProcessor(ChangeableModel model, ClientUser user, Logic logic) {
        this.model = model;
        this.user = user;
        this.logic = logic;
    }

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
                return false;
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
        }
        return false;
    }

    /**
     * Called by Owner -> Handler, when network connection dies
     */

    @Override
    public void close() {
        user.drop();
        model.clear();
        model.clearConversation();
        logic.notifyObservers(ACTIONS.DISCONNECTED, null);
    }

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

    protected boolean onIncomingCall(AbstractDataPackage dataPackage) {
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        user.lock();
        if (user.isCalling() != ClientUser.NO_ONE) {
            //Auto deny because you already calling and send some shit
            //that will tell that you wre called
            try {
                user.getWriter().writeDeny(user.getId(), sender.getId());
                logic.notifyObservers(ACTIONS.CALLED_BUT_BUSY, new Object[]{sender});
            } catch (IOException ignored) {
            }
            return true;
        }
        user.call(sender.getId());
        user.unlock();

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
        user.drop();
        BaseUser dude = model.getUserMap().get(dataPackage.getHeader().getFrom());

        AbstractClient.callAcceptRoutine(dude, dataPackage.getDataAsString(), logic, model);
        return true;
    }

    protected boolean onCallDeny(AbstractDataPackage dataPackage) {
        user.drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_DENIED, new Object[]{baseUser});
        return true;
    }

    protected boolean onCallCanceled(AbstractDataPackage dataPackage) {
        user.drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_CANCELLED, new Object[]{baseUser});
        return true;
    }

    protected boolean onBothInConversation(AbstractDataPackage dataPackage) {
        if (user.isCalling() == dataPackage.getHeader().getFrom())
            user.drop();
        logic.notifyObservers(ACTIONS.BOTH_IN_CONVERSATION, new Object[]{
                model.getUserMap().get(dataPackage.getHeader().getFrom())
        });
        return true;
    }
}
