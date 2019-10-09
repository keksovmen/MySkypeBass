package com.Networking.Protocol;

/**
 * Contains and handles meta info for a package
 */

public abstract class AbstractHeader {

    AbstractHeader() {
    }

    /**
     * Init from getRawHeader() data
     * Basically when reader reads INITIAL_SIZE
     * put its array in here
     *
     * @param data read array from getRawHeader() method
     */

    public abstract void init(final byte[] data);

    /**
     * Default init method
     * Modifies current state
     *
     * @param code   instruction to set
     * @param length of data
     * @param from   sender
     * @param to     sender
     */

    public abstract void init(CODE code, int length, int from, int to);

    /**
     * Call this method when you are ready to write in a stream
     *
     * @return transformed from int field to INITIAL_SIZE array of bytes
     */

    public abstract byte[] getRawHeader();

    /**
     * @return Instruction what to do
     */

    public abstract CODE getCode();

    /**
     * @return of data except INITIAL_SIZE
     */

    public abstract int getLength();

    /**
     * @return who sendSound it
     */

    public abstract int getFrom();

    /**
     * @return who must receive it
     */

    public abstract int getTo();

    /**
     * Add more flexibility when you already have package
     * but you need then add some data in it will recalculate cache
     *
     * @param length new length of new data to be set
     */

    abstract void setLength(int length);

}