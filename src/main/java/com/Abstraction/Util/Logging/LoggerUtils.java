package com.Abstraction.Util.Logging;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class LoggerUtils {


    public static final Path clientFilePath =
            Paths.get(System.getProperty("user.home") +
                    "/SkypeButBassBoosted/Client.log");

    public static Logger clientLogger = null;
    public static Logger serverLogger = null;

    /**
     * Need for later initialisation
     * Because I first create appropriate directory for log files
     * and its process call clientFilePath field, so
     * this static class initialise and loggers will be null
     * cause still LogManager.getLogManager().readConfiguration() wasn't called
     */

    public static void initLoggers(){
        clientLogger = Logger.getLogger("com.ClientLogger");
        serverLogger = Logger.getLogger("com.ServerLogger");
    }
}
