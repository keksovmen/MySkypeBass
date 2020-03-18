package com.Abstraction.Util.Logging;

import com.Abstraction.Util.Interfaces.Initialising;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Logging.Loggers.ClientLogger;
import com.Abstraction.Util.Logging.Loggers.LoggerWithInitialisation;
import com.Abstraction.Util.Logging.Loggers.ServerLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public abstract class LogManagerHelper implements Initialising {

    protected static LogManagerHelper instance;

    protected final LoggerWithInitialisation clientLogger;
    protected final LoggerWithInitialisation serverLogger;


    public LogManagerHelper() {
        clientLogger = new ClientLogger();
        serverLogger = new ServerLogger();
    }

    @Override
    public void init() {
        if (!prepareFiles()) {
            return;
        }
        try {
            LogManager.getLogManager().readConfiguration(getPropertiesStream());
            clientLogger.init();
            serverLogger.init();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Logging initialisation is failed due to resource reading");
        }
    }

    public BaseLogger getClientLogger() {
        return clientLogger;
    }

    public BaseLogger getServerLogger() {
        return serverLogger;
    }

    /**
     * Must prepare files where logs will live
     * Platform dependent I think
     * Check path in logging.properties
     *
     * @return true only if files are ready to be written in
     */

    protected abstract boolean prepareFiles();

    protected abstract InputStream getPropertiesStream() throws IOException;

    public static LogManagerHelper getInstance() {
        if (instance == null)
            throw new IllegalStateException("LogManager is not initialised yet!");
        return instance;
    }

    public static void setInstance(LogManagerHelper instance) {
        if (LogManagerHelper.instance != null)
            throw new IllegalStateException("LoggerManager already initialised!");
        LogManagerHelper.instance = instance;
    }
}
