package com.Client;

import com.Pipeline.ACTIONS;

/**
 * Interface for every one who wants to see updates
 * on a client logic side
 */

public interface LogicObserver {

    /**
     * Implementer should react some how on given action with
     * correspond resources
     * @param action command
     * @param data see ACTIONS enum for more info what it could be
     */

    void observe(ACTIONS action, Object[] data);
}
