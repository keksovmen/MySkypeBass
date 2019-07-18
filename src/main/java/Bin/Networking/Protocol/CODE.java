package Bin.Networking.Protocol;

import java.util.Arrays;
import java.util.Optional;

/**
 * Instruction your handlers reaction depends on its values
 * <p>
 * Must be numerated
 * This can't be 2 identical id
 */

public enum CODE {
    SEND_NAME(1),   //Uses when first time connect to server
    SEND_ID(2),     //not used
    SEND_AUDIO_FORMAT(3),//sends audio format
    SEND_USERS(4),  //sendSound request or response with users on server
    SEND_MESSAGE(5),//When client sendSound message to another client, or to conference
    SEND_CALL(6),   //when client call someone
    SEND_APPROVE(7),//when call accepted
    SEND_DENY(8),   //when call denied
    SEND_CANCEL(9), //when caller cancelled the call
    SEND_SOUND(10), //sends sound data to conference
    SEND_DISCONNECT(11),    //when disconnecting from the server
    SEND_ADD(12),   //server sendSound it when some one was added to your conversation
    SEND_REMOVE(13),//server sendSound it when some one was removed to your conversation
    SEND_CONFIRM(14),//not used
    SEND_DISCONNECT_FROM_CONV(15),//when client exited a conversation he sends it
    SEND_STOP_CONV(16);//when you are last one in conversation server sends it to you

    /**
     * Unique id
     */

    private final int code;

    CODE(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Static factory
     *
     * @param code unique id
     * @return CODE for this id or null
     */

    public static CODE parse(int code) {
        Optional<CODE> first = Arrays.stream(CODE.values()).filter(code1 -> code1.getCode() == code).findFirst();
        return first.orElse(null);
    }
}
