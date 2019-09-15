package Bin.Networking.Protocol;

import Bin.Util.Algorithms;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Instruction your handlers reaction depends on its values
 * <p>
 * Must be numerated
 * There can't be 2 identical id
 */

public enum CODE {
    SEND_NAME(1),   //Uses when first time connect to server
    SEND_AUDIO_FORMAT(2),//sends audio format
    SEND_APPROVE(3),//when audio format is appropriate or call accepted
    SEND_DENY(4),   //when audio format is not appropriate or call denied
    SEND_ID(5),     //server sends your unique id
    SEND_USERS(6),  //sendSound request or response with users on server
    SEND_MESSAGE(7),//When client sendSound message to another client, or to conference
    SEND_CALL(8),   //when client call someone
    SEND_CANCEL(9), //when caller cancelled the call
    SEND_SOUND(10), //sends sound data to conference
    SEND_DISCONNECT(11),    //when disconnecting from the server
    SEND_ADD(12),   //server sendSound it when some one was added to your conversation
    SEND_REMOVE(13),//server sendSound it when some one was removed to your conversation
    SEND_CONFIRM(14),//not used
    SEND_DISCONNECT_FROM_CONV(15),//when client exited a conversation he sends it
    SEND_STOP_CONV(16);//when you are last one in conversation server sends it to you

    private static boolean checked = false;

    /**
     * Unique id
     */

    private final int code;

    /**
     * First range checking
     *
     * @param code unique id
     */

    CODE(int code) {
        if (code < 0 || ProtocolBitMap.MAX_VALUE < code) {
            throw new IllegalArgumentException("Unique id {" + code + "} is out of range");
        }
        this.code = code;
    }

    /**
     * Static factory for converting by id
     *
     * @param code unique id
     * @return CODE for this id or exception
     */

    public static CODE parse(int code) {
        if (!checked) {
            throw new IllegalStateException("First you must call CODE.uniqueIdCheck()" +
                    " to be sure that there is not equal elements");
        }
        CODE search = Algorithms.search(values(), code, (code1, integer) -> code1.getCode() == integer);
        if (search == null) {
            throw new NoSuchElementException("There is no such code id " + code);
        }
        return search;
    }

    /**
     * Must call before using this enum
     * <p>
     * Check for identical IDs
     */

    public static void uniqueIdCheck() {
        if (checked)
            return;
        boolean b = Algorithms.searchForIdentities(values(), Comparator.comparingInt(CODE::getCode));
        if (b) {
            throw new IllegalArgumentException("There is already pair of equal IDs ");
        }
        checked = true;
    }

    public int getCode() {
        return code;
    }


}
