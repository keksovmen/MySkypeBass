package com.Abstraction.Client;

import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Interfaces.Registration;

/**
 * Base interface for application logic
 * You must attach observers to modelObservation them
 * Observer pattern where observers are {@link LogicObserver}
 * Root of Chain of Responsibility for {@link ButtonsHandler}
 */

public interface Logic extends Registration<LogicObserver>, ButtonsHandler {

    /**
     * Loop over observers with given params
     *
     * @param action to display
     * @param data   see {@link ACTIONS} for description
     */

    void notifyObservers(ACTIONS action, Object[] data);
}
