package com.Abstraction.Util.Logging.Loggers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientLogger implements LoggerWithInitialisation {

    protected Logger logger;

    @Override
    public void logp(String sourceClass, String method) {
        if (logger != null)
            logger.logp(Level.FINER, sourceClass, method, "");
    }

    @Override
    public void logp(String sourceClass, String method, String message) {
        if (logger != null)
            logger.logp(Level.FINER, sourceClass, method, message);
    }

    @Override
    public void entering(String sourceClass, String method) {
        if (logger != null)
            logger.entering(sourceClass, method);
    }

    @Override
    public void entering(String sourceClass, String method, Object argument) {
        if (logger != null)
            logger.entering(sourceClass, method, argument);
    }

    @Override
    public void exiting(String sourceClass, String method) {
        if (logger != null)
            logger.exiting(sourceClass, method);
    }

    @Override
    public void exiting(String sourceClass, String method, Object argument) {
        if (logger != null)
            logger.exiting(sourceClass, method, argument);
    }

    @Override
    public void init() {
        logger = Logger.getLogger("com.ClientLogger");
    }
}
