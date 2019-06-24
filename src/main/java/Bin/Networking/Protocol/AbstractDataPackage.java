package Bin.Networking.Protocol;

import Bin.Networking.Writers.BaseWriter;

/**
 * Represents a structure of a protocol
 * Contain a header where is meta info,
 * and the body that might be null
 */

public abstract class AbstractDataPackage {




//    protected AbstractHeader header;



//    protected byte[] data;

    /**
     * Default package private constructor
     * Access to this instances goes through AbstractDataPackagePool
     */

    AbstractDataPackage() {
    }

    /**
     * Initialisation for empty messages
     * They work like control signals
     *
     * @param code instruction to do
     * @param from who sent it
     * @param to   who will receive
     * @return this instance for shorter code
     */

    public abstract BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to);

    /**
     * For sending byte array of data could be anything
     * Has 1 main purpose is to sendSound sound
     *
     * @param code instruction to do
     * @param from who sent it
     * @param to   who will receive
     * @param data any byte array
     * @return this instance for shorter code
     */

    public abstract BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to, final byte[] data);

    /**
     * For sending strings
     * As i see it will decode from default charset to
     * standard one
     *
     * @param code instruction to do
     * @param from who sent it
     * @param to   who will receive
     * @param data string
     * @return this instance for shorter code
     */

    public abstract BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to, final String data);

    /**
     * Mutates data
     * when you read header you know how long is body
     *
     * @param data to set
     */

    public abstract void setData(byte[] data);

    /**
     * Mutates data
     * Uses on server side currently
     * Allow to set string instead of byte array
     *
     * @param data to set
     */

    public abstract void setData(String data);

    /**
     * Should be converted in some charset
     *
     * @return data but as a string
     */

    public abstract String getDataAsString();

    /**
     * When returning to object pull should call it
     */

    abstract void clear();

    /**
     * @return header for this package
     */

    public abstract AbstractHeader getHeader();

    /**
     * @return that consist in data field
     */

    public abstract byte[] getData();

}
