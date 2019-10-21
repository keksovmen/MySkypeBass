package com.Util.Logging;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class LoggerUtils {

    public static  final Logger clientLogger = Logger.getLogger("com.ClientLogger");
    public static  final Logger serverLogger = Logger.getLogger("com.ServerLogger");

    public static final Path clientFilePath = Paths.get(System.getProperty("user.home") + "/SkypeButBassBoostedClient.log");
}
