package com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.ApplicationFactoryDecorator;
import com.Abstraction.Util.Logging.LogManagerHelper;

public class DesktopApplicationFactoryLog extends ApplicationFactoryDecorator
{

    public DesktopApplicationFactoryLog(AbstractApplicationFactory child)
    {
        super(child);
    }

    @Override
    public LogManagerHelper createLogManager() {
        LogManagerHelper logManager = super.createLogManager();
        logManager.init();
        return logManager;
    }
}
