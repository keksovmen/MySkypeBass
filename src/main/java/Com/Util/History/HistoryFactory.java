package Com.Util.History;

import Com.Util.Resources;

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
