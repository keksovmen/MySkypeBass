package com.Abstraction.Model;

/**
 * Implement if you want to listen for users update
 */

public interface Updater {

    /**
     * Will be called each time when users on client side get updated
     *
     * @param model where you can get copy of map
     */

    void update(UnEditableModel model);
}
