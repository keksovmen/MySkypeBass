package Bin.Networking.Protocol;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents a structure of a protocol
 * Contain header where is meta inf
 * and the body that might be null
 */

public class BaseDataPackage extends AbstractDataPackage {

    /**
     * Need to use charset for string conversion
     * If you don't use it all the strings that were sent
     * will display as random tokens
     */

    private static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Header with meta info
     */

    private final AbstractHeader header;

    /**
     * Body with data might be null
     */

    private byte[] data;

    /**
     * Package private because you retrieve it from
     * object pool @see DataPackagePool
     */

    BaseDataPackage() {
        header = new DataPackageHeader();
        data = null;
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

    @Override
    public BaseDataPackage initZeroLength(final CODE code, final int from, final int to) {
        header.init(code, 0, from, to);
        return this;
    }

    /**
     * For sending byte array of data could be anything
     * Has 1 main purpose is to sendSound sound
     *
     * @param code instruction to do
     * @param from who sent it
     * @param to   who will receive
     * @param data any byte array might be null
     * @return this instance for shorter code
     */

    @Override
    public BaseDataPackage initRaw(final CODE code, final int from, final int to, @Nullable final byte[] data) {
        header.init(code, data == null ? 0 : data.length, from, to);
        this.data = data;
        return this;
    }

    /**
     * For sending strings
     * As i see it will decode from default charset to
     * standard one
     *
     * @param code instruction to do
     * @param from who sent it
     * @param to   who will receive
     * @param data string not null
     * @return this instance for shorter code
     */

    @Override
    public BaseDataPackage initString(final CODE code, final int from, final int to, @NotNull final String data) {
        return initRaw(code, from, to, data.getBytes(charset));
    }

    /**
     * Work as builder
     * when you read header you know how long is body
     *
     * @param data to set
     */

    @Override
    public void setData(byte[] data) {
        this.data = data;
        header.setLength(data.length);
    }

    /**
     * Check if data is null
     *
     * @return string >= 0 length
     */

    @Override
    public String getDataAsString() {
        return data == null ? "" : new String(data, charset);
    }

    /**
     * You must be sure that there is number as string
     *
     * @return int value of data
     */

    @Override
    public int getDataAsInt() {
        return Integer.parseInt(getDataAsString());
    }

    @Override
    public AbstractHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    /**
     * Work as builder
     * Uses on server side currently
     * Allow to set string instead of byte array
     *
     * @param data this instance for shorter code
     */

    @Override
    public void setData(String data) {
        this.data = data.getBytes(charset);
        header.setLength(this.data.length);
    }

    /**
     * Call it when package is done and returned to pool
     */

    @Override
    void clear() {
        data = null;
    }

    @Override
    public String toString() {
        return "BaseDataPackage{" +
                "header=" + header +
                ", data=" + Arrays.toString(data) +
                ", dataAsString=" + getDataAsString() + '}';
    }
}
