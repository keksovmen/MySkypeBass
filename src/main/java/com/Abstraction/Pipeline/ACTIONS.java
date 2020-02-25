package com.Abstraction.Pipeline;

/**
 * Contain all possible actions that can occur
 * on the logic part of this program
 */

public enum ACTIONS {
    CONNECT_FAILED, // doesn't contain any data
    CONNECT_SUCCEEDED, //data[0] is your BaseUser.toString();
    CONNECTION_TO_SERVER_FAILED, //doesn't contain any data, for exceptional cases when server died
    AUDIO_FORMAT_NOT_ACCEPTED, //data[0] is AudioFormat
    AUDIO_FORMAT_ACCEPTED, //data[0] is AudioFormat
    WRONG_HOST_NAME_FORMAT, //data[0] is host name as String
    WRONG_PORT_FORMAT, //data[0] is port as String
    WRONG_SAMPLE_RATE_FORMAT, //data[0] is rate as String
    WRONG_SAMPLE_SIZE_FORMAT, //data[0] is size as String
    PORT_ALREADY_BUSY, //data[0] is port number as String
    SERVER_CREATED, // no data
    SERVER_CREATED_ALREADY, // no data
    PORT_OUT_OF_RANGE, //data[0] is port range as String and data[1] is port itself as Integer
    INCOMING_MESSAGE, //data[0] is BaseUser who send or null if some how not present in model, data[1] is message as String, data[2] is flag as Integer 1 if message to conversation 0 otherwise
    DISCONNECTED, // no data, 100% will be called after CONNECTION_TO_SERVER_FAILED
    OUT_CALL, // data[0] is BaseUser who you calling to
    INCOMING_CALL, //data[0] is BaseUser who called you, data[1] is String contain dudes as BaseUser.toString() in conversation with him
    CALL_ACCEPTED, // no data, all dudes will be send through ADD_TO_CONVERSATION
    CALL_DENIED, //data[0] is BaseUser who denied a call
    CALL_CANCELLED, //data[0] is BaseUser who cancelled a call
    CALLED_BUT_BUSY, //data[0] BaseUser is the dude who called
    ALREADY_CALLING_SOMEONE, // no data
    BOTH_IN_CONVERSATION, //data[0] is BaseUser or null who you called
    EXITED_CONVERSATION, // no data
    INCOMING_SOUND, //data[0] is BaseUser who sent or null if not present in model, data[1] is byte[] sound, data[2] is Integer id of the dude who sent it
    INVALID_AUDIO_FORMAT, //data[0] is message as String
    ALREADY_CONNECTED_TO_SERVER, //data[0] is your BaseUser.toString() need in case of something
    CIPHER_FORMAT_IS_NOT_ACCEPTED, //data[0] is String contain message
    CIPHER_FORMAT_ON_SERVER_IS_NOT_ACCEPTED, //data[0] is String contain message
    UDP_SOCKET_NOT_BINDED,  //data[0] is String contain message

}
