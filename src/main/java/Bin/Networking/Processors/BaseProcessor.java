package Bin.Networking.Processors;

import Bin.Networking.Protocol.AbstractDataPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base processor
 * Implements simple actions
 * <p>
 * Not thread safe
 */

public abstract class BaseProcessor implements Processable {

    /**
     * Where all listeners are live
     */

    final List<Consumer<AbstractDataPackage>> listeners;

    BaseProcessor() {
        listeners = new ArrayList<>();
    }

    /**
     * How to register actions
     *
     * @param listener actions
     */

    @Override
    public <T extends Consumer<AbstractDataPackage>> void addListener(T listener) {
        listeners.add(listener);
    }

    /**
     * How to remove actions
     *
     * @param listener to remove
     */

    @Override
    public <T extends Consumer<AbstractDataPackage>> void removeListener(T listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners
     */

    @Override
    public void clear() {
        listeners.clear();
    }
}
