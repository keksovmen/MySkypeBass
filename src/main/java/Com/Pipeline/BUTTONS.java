package Com.Pipeline;

public enum BUTTONS {
    CONNECT, // Plain data is String[3] first name ,then ip, then port
    DISCONNECT,
    CREATE_SERVER, // Plain data is String[3] port, sample rate, sample size
    SEND_MESSAGE, // string is message int is id of receiver
    CALL, // Object is BaseUser who you try to call
    EXIT_CONFERENCE,
    MUTE,
    INCREASE_BASS,
    ASC_FOR_USERS, // no data
    CALL_ACCEPTED, //object is dude who called, string all dudes who is in conversation
    CALL_DENIED, //object is dude who called
    CALL_CANCELLED, // object is user who you tried to call
    VOLUME_CHANGED, // string data is id, int is volume lvl

}
