package Com.Pipeline;

public enum ACTIONS {
    CONNECT_FAILED, // doesn't contain any data
    CONNECT_SUCCEEDED, //string data is your BaseUser.toString();
    CONNECTION_SERVER_FAILED, //doesn't contain any data
    AUDIO_FORMAT_NOT_ACCEPTED, //stringData is audioFormat.toString()
    AUDIO_FORMAT_ACCEPTED, //stringData is audioFormat.toString()
    WRONG_HOST_NAME_FORMAT, // string data is host name
    WRONG_PORT_FORMAT, //string data is port
    WRONG_SAMPLE_RATE_FORMAT, //string data is rate
    WRONG_SAMPLE_SIZE_FORMAT, //string data is size
    PORT_ALREADY_BUSY, //string data is port
    SERVER_CREATED,
    SERVER_CREATED_ALREADY,
    PORT_OUT_OF_RANGE, // string data is port range and intData is port
    INCOMING_MESSAGE, //User is who or null if some how not present in model sent and string is message




}
