package com.Abstraction.Client;

import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Interfaces.Registration;

/**
 * Base interface for application logic
 * You must attach observers to update them
 */

public interface Logic extends Registration<LogicObserver>, ButtonsHandler {

    /**
     * Loop over observers with given params
     *
     * @param action to display
     * @param data   see ACTIONS for description
     */

    void notifyObservers(ACTIONS action, Object[] data);
}
