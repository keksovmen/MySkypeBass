package com.Abstraction.Networking.Processors;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.Logic;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.Users.PlainUser;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;

import java.io.IOException;

/**
 * Represent client side networkHelper for incoming messages from server
 * But doesn't handle reading these messages from Input stream, only handle their meaning
 * <p>
 * Literally thrush because, when you need new a handler
 * you must add new case in switch statement that is garbage
 * Solution is make functional interface which method is to handle {@link AbstractDataPackage}
 * And dynamically add them through implementation of {@link com.Abstraction.Util.Interfaces.Registration}
 * Then it class will looks kinda as Composite pattern
 * Why didn't i made it this way?
 * 'Cause my lazy ass and not sure about overhead when access through many objects
 * Approximate overhead is O(n) to find proper handler + O(n) to see if a handler capable of handling it
 * So O(2n) instead of hash function in switch statement O(1)
 * But switch statement is shitty practice in this case, because if you need new case branch you have to modify
 * existing code and not to expand through inheritance
 */

public class ClientProcessor implements Processable {

    protected final BaseLogger clientLogger = LogManagerHelper.getInstance().getClientLogger();

    /**
     * Where made user changes
     */

    protected final ChangeableModel model;

    /**
     * To notify LogicObserver about changes
     */

    private final Logic logic;


    public ClientProcessor(ChangeableModel model, Logic logic) {
        this.model = model;
        this.logic = logic;
    }

    /**
     * Routes to proper networkHelper method
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
            case SEND_DISCONNECT_FROM_CONVERSATION:
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
     * Called by Owner {@code ->} Handler, when network connection dies
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
        clientLogger.logp(getClass().getName(), "onUsersRequest");
        String users = dataPackage.getDataAsString();
        model.addToModel(User.parseUsers(users));
        return true;
    }


    protected boolean onIncomingMessage(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onIncomingMessage");
        User sender = model.getUser(dataPackage.getHeader().getFrom());
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
        clientLogger.logp(getClass().getName(), "onIncomingCall");
        User sender = model.getUser(dataPackage.getHeader().getFrom());
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
                model.getUser(from),
                dataPackage.getData(),
                from
        });
        return true;
    }

    protected boolean onAddToConversation(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onAddToConversation");
        User baseUser = model.getUser(dataPackage.getHeader().getFrom());
        model.addToConversation(baseUser);
        return true;
    }

    protected boolean onRemoveDudeFromConversation(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onRemoveDudeFromConversation");
        User baseUser = model.getUser(dataPackage.getHeader().getFrom());
        model.removeFromConversation(baseUser);
        return true;
    }

    protected boolean onExitConversation(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onExitConversation");
        logic.notifyObservers(ACTIONS.EXITED_CONVERSATION, null);
        model.clearConversation();
        AbstractDataPackagePool.clearStorage();
        return true;
    }

    protected boolean onAddToUserList(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onAddToUserList");
        String user = dataPackage.getDataAsString();
        model.addToModel(User.parse(user));
        return true;
    }

    protected boolean onRemoveFromUserList(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onRemoveFromUserList");
        int user = dataPackage.getDataAsInt();
        model.removeFromModel(user);
        return true;
    }

    protected boolean onCallAccept(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onCallAccept");
        model.getMyself().drop();
        User dude = model.getUser(dataPackage.getHeader().getFrom());
        AbstractClient.callAcceptRoutine(logic, model, dude);
        return true;
    }

    protected boolean onCallDeny(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onCallDeny");
        model.getMyself().drop();
        User baseUser = model.getUser(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_DENIED, new Object[]{new PlainUser(baseUser.getName(), baseUser.getId())});
        return true;
    }

    protected boolean onCallCanceled(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onCallCanceled");
        model.getMyself().drop();
        User baseUser = model.getUser(dataPackage.getHeader().getFrom());
        logic.notifyObservers(ACTIONS.CALL_CANCELLED, new Object[]{new PlainUser(baseUser.getName(), baseUser.getId())});
        return true;
    }

    protected boolean onBothInConversation(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onBothInConversation");
        ClientUser myself = model.getMyself();

        if (myself.isCalling() == dataPackage.getHeader().getFrom())
            myself.drop();
        logic.notifyObservers(ACTIONS.BOTH_IN_CONVERSATION, new Object[]{
                model.getUser(dataPackage.getHeader().getFrom())
        });
        return true;
    }

    protected boolean onAddWholeConversation(AbstractDataPackage dataPackage) {
        clientLogger.logp(getClass().getName(), "onAddWholeConversation");
        User[] baseUsers = User.parseUsers(dataPackage.getDataAsString());
        for (User user : baseUsers) {
            model.addToConversation(user);
        }
        return true;
    }
}
