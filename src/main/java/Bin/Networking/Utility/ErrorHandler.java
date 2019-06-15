package Bin.Networking.Utility;

/**
 * Uses for base writer and reader
 * when they drop an exception call it on main error holder
 * then it must to transmit to others
 * to properly terminate the thread
 */

public interface ErrorHandler {

    /**
     * Call when everything is broken
     */

    void errorCase();

    /**
     *
     * @return null or others who can call errorCase()
     */

    ErrorHandler[] getNext();

    /**
     * MUST CALL ANY TIME WHEN getNext return not null
     */

    default void iterate(){
        ErrorHandler[] errorHandlers = getNext();
        if (errorHandlers == null){
            return;
        }
        for (ErrorHandler errorHandler : errorHandlers) {
            if (errorHandler == null){
                continue;
            }
            errorHandler.errorCase();
            errorHandler.iterate();
        }
    }

}
