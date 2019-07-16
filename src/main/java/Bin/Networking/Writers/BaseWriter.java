package Bin.Networking.Writers;

import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;

/**
 * Base writer that only can write AbstractDataPackage or its children
 */

public abstract class BaseWriter {

    /**
     * Where to write
     */

    final DataOutputStream outputStream;

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

    /**
     * Uses as default representation of main characters
     */

    public enum WHO {
        NO_NAME(0), //your id when first connect to the server
        SERVER(1), //package for the server
        CONFERENCE(2);  //package for the conference

        /**
         * Unique id
         */

        private final int code;

        WHO(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        /**
         * Indicates start of unique users id
         * Must be greater than max value of WHO enum
         */

        public static final int SIZE = WHO.values().length;
    }

    /**
     * You can use only write() method
     *
     * @param outputStream where to write
     */

    BaseWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
    }

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     * @throws IOException if network failing occurs
     */

    synchronized void write(AbstractDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());// cashed in other implementation @see serverWriter
        if (dataPackage.getHeader().getLength() != 0) {
            outputStream.write(dataPackage.getData());
        }
        outputStream.flush();
//        System.out.println(dataPackage + " " + Thread.currentThread().getName());
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

}
