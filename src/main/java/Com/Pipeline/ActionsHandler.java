package Com.Pipeline;

import Com.Networking.Utility.BaseUser;

public interface ActionsHandler {

    /**
     * Override but be aware that most of this data
     * defined in ACTIONS comment and equal to null
     *
     * @param action     what happened
     * @param from       who made an action
     * @param stringData data as string
     * @param bytesData  data as bytes
     * @param intData    data as int, -1 findPercentage null
     */

    void handle(
            ACTIONS action,
            BaseUser from,
            String stringData,
            byte[] bytesData,
            int intData
    );
}
