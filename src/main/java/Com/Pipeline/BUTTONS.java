package Com.Pipeline;

public enum BUTTONS {
    CONNECT, // Plain data is String[3] first name ,then ip, then port
    DISCONNECT,
    CREATE_SERVER, // Plain data is String[3] port, sample rate, sample size
    CREATE_SERVER_PANE, // For GUI
    CANCEL_SERVER_CREATION, //For GUI
    SEND_MESSAGE,
    CALL,
    EXIT_CONFERENCE,
    MUTE,
    INCREASE_BASS,

}
