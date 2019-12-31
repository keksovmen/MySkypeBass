package com.Abstraction.Networking.Protocol;

import com.Abstraction.Util.Algorithms;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Contain all possible instruction that can be send to each other
 * <p>
 * Must be numerated
 * There can't be 2 identical id
 */

public enum CODE {
    SEND_NAME(1),   //Uses when first time connect to server
    SEND_AUDIO_FORMAT(2),//sends audio format
    SEND_APPROVE(3),//when audio format is appropriate or call accepted
    SEND_AUDIO_FORMAT_DENY(4),   // in authenticate show is audio format not acceptable
    SEND_ID(5),     //server sends your unique id
    SEND_USERS(6),  //sendSound request or response with users on server
    SEND_MESSAGE(7),//When client sendSound message to another client, or to conference
    SEND_CALL(8),   //when client call someone
    SEND_CANCEL(9), // not used
    SEND_SOUND(10), //sends sound data to conference
    SEND_DISCONNECT(11),    //when disconnecting from the server
    SEND_ADD_TO_CONVERSATION(12),   //server sendSound it when some one was added to your conversation
    SEND_REMOVE_FROM_CONVERSATION(13),//server sendSound it when some one was removed to your conversation
    SEND_CONFIRM(14), // not used
    SEND_DISCONNECT_FROM_CONV(15),//when client exited a conversation he sends it
    SEND_ADD_TO_USER_LIST(16),//when you connected to the server it will tell every one that you are online
    SEND_REMOVE_FROM_USER_LIST(17),//when you disconnect from the server it will tell every one that you are gone
    SEND_ACCEPT_CALL(18), // when dude accepts an incoming call
    SEND_DENY_CALL(19), // when dude denies an incoming call
    SEND_CANCEL_CALL(20), // when dude cancel an out coming call
    SEND_BOTH_IN_CONVERSATIONS(21), // when you trying to call some one but you both in conversations
    SEND_SERVER_CIPHER_MODE(22), // when you trying to call some one but you both in conversations
    SEND_SERVER_PLAIN_MODE(23), // when you trying to call some one but you both in conversations
    SEND_PUBLIC_ENCODED_KEY(24), // when you trying to call some one but you both in conversations
    SEND_ALGORITHM_PARAMETERS_ENCODED(25), // when you trying to call some one but you both in conversations
    SEND_AUDIO_FORMAT_ACCEPT(26),   // in authenticate show is audio format not acceptable
    SEND_CIPHER_MODE_ACCEPTED(27),   // in authenticate show is audio format not acceptable
    SEND_CIPHER_MODE_DENIED(28);   // in authenticate show is audio format not acceptable



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
