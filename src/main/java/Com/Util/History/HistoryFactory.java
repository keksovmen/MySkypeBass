package Com.Util.History;

import Com.Util.Resources;

public class HistoryFactory {

    private HistoryFactory(){}

    public static History<String> getStringHistory(){
        return new StringHistory(Resources.historySize);
    }
}
