package Com.Pipeline;

public enum BUTTONS {
    CONNECT, // Plain data is String[3] first name ,then ip, then port
    DISCONNECT,
    CREATE_SERVER, // Plain data is String[3] port, sample rate, sample size
    SEND_MESSAGE, // string is message int is id of receiver
    CALL,
    EXIT_CONFERENCE,
    MUTE,
    INCREASE_BASS,
    ASC_FOR_USERS, // no data

}
