package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Util.Interfaces.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base processor
 * Implements simple actions
 * <p>
 * Not thread safe
 */

public abstract class BaseProcessor implements Registration<Consumer<AbstractDataPackage>> {

    /**
     * Where all listeners are live
     */

    final List<Consumer<AbstractDataPackage>> listeners = new ArrayList<>();

    /**
     * How to register actions
     *
     * @param listener actions
     */

    @Override
    public boolean registerListener(Consumer<AbstractDataPackage> listener) {
        return listeners.add(listener);
    }

    /**
     * How to remove actions
     *
     * @param listener to remove
     */

    @Override
    public boolean removeListener(Consumer<AbstractDataPackage> listener) {
        return listeners.remove(listener);
    }

    /**
     * Removes all listeners
     */

    public void clear() {
        listeners.clear();
    }
}
