package com.Abstraction.Pipeline;

/**
 * Contain all possible buttons on UI part
 */

public enum BUTTONS {
    CONNECT, //data[0] is my name as String, data[1] is host name as String, data[2] is port as String
    DISCONNECT, // no data
    CREATE_SERVER, //data[0] is port, data[1] is sample rate, data[2] is sample size everything as String, data[3] is Boolean encryption mode
    SEND_MESSAGE, //data[0] is message as String, data[1] is id of receiver as Integer
    CALL, // data[0] is BaseUser who you trying to call
    EXIT_CONFERENCE, // no data
    MUTE, //no data
    INCREASE_BASS, // data[0] is lvl in % from 1 = base level to 100 findPercentage max as Integer
    ASC_FOR_USERS, // no data
    CALL_ACCEPTED, //data[0] is BaseUser who called, data[1] is String with dudes in conversation
    CALL_DENIED, //data[0] is BaseUser who called, data[1] is String with dudes in conversation
    CALL_CANCELLED, // data[0] is BaseUser who called, data[1] is String with dudes in conversation
    VOLUME_CHANGED, // data[0] is id as Integer, data[1] is volume in % from 0 to 100 as Integer
    CHANGE_INPUT, // data[0] is Integer id of particular output
    CHANGE_OUTPUT, // data[0] is Integer id of particular output
    PREVIEW_SOUND, // data[0] is id formatted as in message as String, data[1] id of a track as Integer
    SEND_SOUND, // data[0] is sound to send as byte[]
    STOP_SERVER,    //no data
}
