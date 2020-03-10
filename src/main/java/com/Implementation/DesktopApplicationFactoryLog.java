package com.Implementation;

import com.Abstraction.Util.Logging.LogManagerHelper;

public class DesktopApplicationFactoryLog extends DesktopApplicationFactory {

    @Override
    public LogManagerHelper createLogManager() {
        LogManagerHelper logManager = super.createLogManager();
        logManager.init();
        return logManager;
    }
}
