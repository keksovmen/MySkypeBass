package com.Abstraction.Client;

import com.Abstraction.Pipeline.ACTIONS;

/**
 * Interface for every one who wants to see updates
 * on a client logic side
 */

public interface LogicObserver {

    /**
     * Implementer should react some how on given action with
     * correspond resources
     *
     * @param action command
     * @param data   see {@link com.Abstraction.Pipeline.ACTIONS} enum for more info what it could be
     * @see ACTIONS
     */

    void observe(ACTIONS action, Object[] data);
}
