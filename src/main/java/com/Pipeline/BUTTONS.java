package com.Pipeline;

/**
 * Contain all possible buttons on UI part
 */

public enum BUTTONS {
    CONNECT, // Plain data is String[3] first name ,then ip, then port
    DISCONNECT, // no data
    CREATE_SERVER, // Plain data is String[3] port, sample rate, sample size
    SEND_MESSAGE, // string is message int is id of receiver
    CALL, // Object is BaseUser who you try to call
    EXIT_CONFERENCE, // no data
    MUTE,
    INCREASE_BASS, // int lvl in % from 1 = base level to 100 findPercentage max
    ASC_FOR_USERS, // no data
    CALL_ACCEPTED, //object is dude who called, string all dudes who is in conversation
    CALL_DENIED, //object is dude who called
    CALL_CANCELLED, // object is user who you tried to call
    VOLUME_CHANGED, // string data is id, int is volume in % from 0 to 100
    CHANGE_INPUT, // object is mixer
    CHANGE_OUTPUT, // object is mixer

}
