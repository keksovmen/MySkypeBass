package com.Abstraction.Util.History;

import com.Abstraction.Util.Resources.Resources;

/**
 * Factory for obtaining History<> objects
 */

public class HistoryFactory {

    private HistoryFactory() {
    }

    public static History<String> getStringHistory() {
        return new StringHistory(Resources.getInstance().getHistorySize());
    }
}
