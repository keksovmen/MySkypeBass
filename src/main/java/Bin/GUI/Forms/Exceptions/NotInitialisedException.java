package Bin.GUI.Forms.Exceptions;

/**
 * Main purpose is to avoid null pointer
 * And make it easy to find what wasn't initialised
 * Drop it when get something and it is null
 */

public class NotInitialisedException extends Exception {

    public NotInitialisedException() {
    }

    public NotInitialisedException(String message) {
        super(message);
    }
}
