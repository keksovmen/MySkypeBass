package com.Abstraction.Client;

import com.Abstraction.Pipeline.BUTTONS;

/**
 * Route to interact between GUI and application Logic
 * Also used as Chain of Responsibility
 */

public interface ButtonsHandler {

    /**
     * @param button command
     * @param data   see {@link BUTTONS} enum for more info
     * @see BUTTONS
     */

    void handleRequest(BUTTONS button, Object[] data);
}
