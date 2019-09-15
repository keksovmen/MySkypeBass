package Bin.Networking.Protocol;

/**
 * Contains protocol size constants
 *
 * All values in bytes
 *
 * All parts of the package must be equal sized
 */

public interface ProtocolBitMap {

    /**
     * Size of the whole part that you can be 100% sure to read
     */
    int PACKET_SIZE = 8;

    /**
     * Size of instruction part
     */

    int INSTRUCTION_SIZE = 2;

    /**
     * Size of length part
     */

    int LENGTH_SIZE = 2;

    /**
     * Size of sender id part
     */

    int FROM_SIZE = 2;

    /**
     * Size of receiver id part
     */

    int TO_SIZE = 2;

    /**
     * Defines allowed negative values or not
     */

    boolean SIGNED_ALLOW = false;

    /**
     * Max possible value to be put in any part of packet except the data
     *
     * Will throw exception division by zero if the conditions are not complied
     */

    int MAX_VALUE = ((int)(Math.pow(2, 8 * INSTRUCTION_SIZE)) /
                        (((INSTRUCTION_SIZE | LENGTH_SIZE | FROM_SIZE | TO_SIZE) * 4)
                == PACKET_SIZE ? 1 : 0)) - 1;
}
