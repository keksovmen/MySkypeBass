package com.Abstraction.Util.Logging.Loggers;

public interface BaseLogger {

    void logp(String sourceClass, String method);

    void logp(String sourceClass, String method, String message);

    void entering(String sourceClass, String method);

    void entering(String sourceClass, String method, Object argument);

    void exiting(String sourceClass, String method);

    void exiting(String sourceClass, String method, Object argument);

    void loge(String sourceClass, String sourceMethod, Throwable exception);

    void loge(String sourceClass, String sourceMethod, String message, Throwable exception);
}
