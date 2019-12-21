package com.Abstraction.Client;

import com.Abstraction.Pipeline.BUTTONS;

/**
 * Route to interact between GUI and application Logic
 */

public interface ButtonsHandler {

    /**
     * @param button command
     * @param data   see BUTTONS enum for more info
     */

    void handleRequest(BUTTONS button, Object[] data);
}
