package com.Abstraction.Util.Logging.Loggers;

import java.util.logging.Logger;

public class ServerLogger extends ClientLogger{


    @Override
    public void init() {
        logger = Logger.getLogger("com.ServerLogger");
    }
}
