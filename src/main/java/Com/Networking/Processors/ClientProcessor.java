package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.DataPackagePool;

import java.io.Closeable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main purpose is to have registered listeners
 * and feed them with dataPackages
 * Also return to home the packages
 * And do it in some other thread not in readers one
 */

public class ClientProcessor extends Processor implements Executor, Closeable {

    private final ActionStand onAddUserToList;
    private final ActionStand onRemoveUserFromList;
    private final ActionStand onBothInConversation;
    private final ActionStand onRemoveDudeFromConversation;
    private final ActionStand onAddDudeToConversation;

    /**
     * Instead of its own thread you have
     * SINGLE THREAD EXECUTOR for not prone purposes
     */

    private final ExecutorService executor;

    public ClientProcessor() {
        onAddUserToList = new ActionStand();
        onRemoveUserFromList = new ActionStand();
        onBothInConversation = new ActionStand();
        onRemoveDudeFromConversation = new ActionStand();
        onAddDudeToConversation = new ActionStand();

        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Call when you have a package to act on it
     * Simply put in executor queue
     *
     * @param dataPackage valid package
     */

    @Override
    public final boolean process(AbstractDataPackage dataPackage) {
        if (executor.isShutdown())
            return false;
        executor.execute(() -> {
            if (!super.process(dataPackage)) {
                branching(dataPackage);
            }
            DataPackagePool.returnPackage(dataPackage);
        });
        return true;
    }

    @Override
    public void execute(Runnable command) {
        if (!executor.isShutdown())
            executor.execute(command);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    public ActionStand getOnAddUserToList() {
        return onAddUserToList;
    }

    public ActionStand getOnRemoveUserFromList() {
        return onRemoveUserFromList;
    }

    public ActionStand getOnBothInConversation() {
        return onBothInConversation;
    }

    public ActionStand getOnRemoveDudeFromConversation() {
        return onRemoveDudeFromConversation;
    }

    public ActionStand getOnAddDudeToConversation() {
        return onAddDudeToConversation;
    }

    private void branching(AbstractDataPackage dataPackage) {
        switch (dataPackage.getHeader().getCode()) {
            case SEND_ADD_TO_USER_LIST: {
                onAddUserToList.process(dataPackage);
                return;
            }
            case SEND_REMOVE_FROM_USER_LIST: {
                onRemoveUserFromList.process(dataPackage);
                return;
            }
            case SEND_BOTH_IN_CONVERSATIONS:{
                onBothInConversation.process(dataPackage);
                return;
            }
            case SEND_REMOVE_FROM_CONVERSATION:{
                onRemoveDudeFromConversation.process(dataPackage);
                return;
            }
            case SEND_ADD_TO_CONVERSATION:{
                onAddDudeToConversation.process(dataPackage);
                return;
            }
        }
    }
}
