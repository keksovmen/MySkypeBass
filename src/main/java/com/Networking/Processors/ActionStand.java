package com.Networking.Processors;

import com.Networking.Protocol.AbstractDataPackage;

import java.util.function.Consumer;

/**
 * Knows it's action and can change it
 */

public class ActionStand implements Processable {

    private Consumer<AbstractDataPackage> action;

    public ActionStand() {
        action = null;
    }

    public boolean process(AbstractDataPackage dataPackage) {
        if (action == null)
            return false;
        action.accept(dataPackage);
        return true;
    }

    public void setListener(Consumer<AbstractDataPackage> listener) {
        action = listener;
    }

    public void removeListener() {
        action = null;
    }

    public boolean isListenerSet() {
        return action != null;
    }

    @Override
    public void close() {

    }
}
