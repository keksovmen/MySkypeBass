package Com.GUI.Interfaces;

import java.util.function.Function;

public interface AudioFormatStatsActions {

    /**
     * Function for creating a server
     * String[0..] values @see serer create methods
     *
     * @return not null action
     * @throws IllegalStateException if it is null
     */

    Function<String[], Boolean> createServer() throws IllegalStateException;

    void updateCreateServer(Function<String[], Boolean> createServer);
}
