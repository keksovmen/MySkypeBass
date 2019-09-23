package Com.Pipeline;

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
    INCOMING_MESSAGE, //User is who or null if some how not present in model sent and string is message
    DISCONNECTED,
    OUT_CALL, // BaseUser who you calling to
    INCOMING_CALL, // BaseUser who called you string data is dudes in conversation with him
    CALL_ACCEPTED, // BaseUser main dude string ochres in a conversation
    CALL_DENIED, // BaseUser main dude
    CALL_CANCELLED, // BaseUser main dude
    CALLED_BUT_BUSY, // BaseUser is the dude who called
    ALREADY_CALLING_SOMEONE, // no data



}
