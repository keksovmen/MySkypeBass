package Bin.Networking.Utility;

/**
 * Uses for base writer and reader
 * when they drop an exception call it on main error holder
 * then it must to transmit to others
 * to properly terminate the thread
 */

public interface ErrorHandler {

    void errorCase();

    ErrorHandler[] getNext();

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
