package com.Abstraction.Networking.Utility;

/**
 * Throw when data you want to send is larger than possible length of the package
 */

public class ProtocolValueException extends Exception {

    public ProtocolValueException(String message) {
        super(message);
    }
}
