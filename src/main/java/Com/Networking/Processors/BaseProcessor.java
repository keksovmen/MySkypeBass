package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base processor
 * Implements simple actions
 * <p>
 * Not thread safe
 */

public abstract class BaseProcessor {

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

//    @Override
    public void setListener(Consumer<AbstractDataPackage> listener) {
        listeners.add(listener);
    }

    /**
     * How to remove actions
     *
     * @param listener to remove
     */

//    @Override
    public void removeListener(Consumer<AbstractDataPackage> listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners
     */

//    @Override
    public void clear() {
        listeners.clear();
    }
}
