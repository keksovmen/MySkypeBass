package com.Abstraction.Util.Resources;

/**
 * Contain resources that can be got directly
 * Or from suitable static functions - preferred!
 */

public class Resources {

    private static AbstractResources instance;


    private Resources() {
    }

    public static AbstractResources getInstance() {
        if (instance == null)
            throw new NullPointerException("First initialise with setInstance method!");
        return instance;
    }

    public static synchronized void setInstance(AbstractResources resources) {
        if (instance != null)
            throw new IllegalStateException("Already initialised with - " + instance.toString());
        instance = resources;
    }


}
