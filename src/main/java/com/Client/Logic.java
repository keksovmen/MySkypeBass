package com.Client;

import com.Pipeline.ACTIONS;
import com.Util.Interfaces.Registration;

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

    void notify(ACTIONS action, Object[] data);
}
