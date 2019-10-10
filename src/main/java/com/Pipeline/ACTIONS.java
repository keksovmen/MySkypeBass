package com.Pipeline;

/**
 * Contain all possible actions that can occur
 * on the logic part of this program
 */

public enum ACTIONS {
    CONNECT_FAILED, // doesn't contain any data
    CONNECT_SUCCEEDED, //string data is your BaseUser.toString();
    CONNECTION_TO_SERVER_FAILED, //doesn't contain any data
    AUDIO_FORMAT_NOT_ACCEPTED, //stringData is audioFormat.toString()
    AUDIO_FORMAT_ACCEPTED, //stringData is audioFormat.toString()
    WRONG_HOST_NAME_FORMAT, // string data is host name
    WRONG_PORT_FORMAT, //string data is port
    WRONG_SAMPLE_RATE_FORMAT, //string data is rate
    WRONG_SAMPLE_SIZE_FORMAT, //string data is size
    PORT_ALREADY_BUSY, //string data is port
    SERVER_CREATED, // no data
    SERVER_CREATED_ALREADY, // no data
    PORT_OUT_OF_RANGE, // string data is port range and intData is port
    INCOMING_MESSAGE, //User is who or null if some how not present in model sent and string is message, int 1 if message to conversation 0 otherwise
    DISCONNECTED, // no data
    OUT_CALL, // BaseUser who you calling to
    INCOMING_CALL, // BaseUser who called you string data is dudes in conversation with him
    CALL_ACCEPTED, // no info, all dudes will be send through ADD_TO_CONVERSATION
    CALL_DENIED, // BaseUser main dude
    CALL_CANCELLED, // BaseUser main dude
    CALLED_BUT_BUSY, // BaseUser is the dude who called
    ALREADY_CALLING_SOMEONE, // no data
    BOTH_IN_CONVERSATION, // user who you called
    EXITED_CONVERSATION, // to clear sound objects and close them and GUI too
    INCOMING_SOUND, // byte [] sound, int id of the dude who sent it
    INVALID_AUDIO_FORMAT, // string is message


}
