package com.Util.History;

import com.Util.Resources;

/**
 * Factory for obtaining History<> objects
 */

public class HistoryFactory {

    private HistoryFactory() {
    }

    public static History<String> getStringHistory() {
        return new StringHistory(Resources.getHistorySize());
    }
}
