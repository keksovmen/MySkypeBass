package Bin.Networking.Protocol;

import java.util.Arrays;

/**
 * Contains and handles meta info for a package
 * <p>
 * Bytes look like
 * 1,2 = code
 * 3, 4 = length
 * 5, 6 = from
 * 7, 8 = to
 * <p>
 * For each field you have 2 bytes
 */

public class DataPackageHeader extends AbstractHeader {

    /*
     * Static init of values @see AbstractHeader
     */

    static {
        INITIAL_SIZE = 8;
        MAX_LENGTH = Short.MAX_VALUE * 2;
    }

    /**
     * Instruction what to do
     */

    private CODE code;

    /**
     * Length of data that was sent
     * if 0 mean no data was sent
     * <p>
     * NOT INCLUDES INITIAL_SIZE
     * <p>
     * Purpose is to show how much need to read
     * after reading header
     */

    private int length;

    /**
     * Who sent the package
     */

    private int from;

    /**
     * Who will receive the package
     */

    private int to;

    /**
     * For cashed on server side purposes
     */

    private byte[] raw;

    /**
     * Package private cause you don't need to create it
     * DataPackage creates it by it self
     */

    DataPackageHeader() {
    }

    /**
     * Default init method
     * Modifies current state
     *
     * @param code   instruction to set
     * @param length of data
     * @param from   sender
     * @param to     sender
     */

    @Override
    public void init(CODE code, int length, int from, int to) {
        this.code = code;
        if (length > getMaxLength()) {
            throw new IllegalArgumentException("length must be less or equal to " + getMaxLength());
        }
        this.length = length;
        this.from = from;
        this.to = to;
    }

    /**
     * Init from getRawHeader() data
     * Basically when reader reads INITIAL_SIZE
     * put its array in here
     *
     * @param data read array from getRawHeader() method
     */

    @Override
    public void init(final byte[] data) {
        code = CODE.parse(parser(data, 0));
        length = parser(data, 2);
        from = parser(data, 4);
        to = parser(data, 6);
        raw = data;
    }

    /**
     * Simply shift bytes and makes sure
     * they in appropriate state
     *
     * @param data     from getRawHeader()
     * @param position display where to start
     * @return parsed value as int
     */

    private int parser(byte[] data, int position) {
        return (((data[position] & 0xff) << 8) + (data[++position] & 0xff));
    }

//    public static void main(String[] args) {
//        System.out.println(Integer.toBinaryString(-2));
//        DataPackageHeader dataPackageHeader = new DataPackageHeader();
//        dataPackageHeader.init(BaseWriter.CODE.SEND_MESSAGE, MAX_LENGTH, 3, 2);
//        System.out.println(dataPackageHeader);
//        dataPackageHeader.init(dataPackageHeader.getRawHeader());
//        System.out.println(dataPackageHeader);
//    }

    /**
     * Call this method when you are ready to write in a stream
     * Must provide proper conversion
     * <p>
     * see how byte ordering works in java and conversion from one type to other
     *
     * @return transformed from int field to INITIAL_SIZE array of bytes
     */

    @Override
    public byte[] getRawHeader() {
        byte[] pocket = new byte[getInitialSize()];
        pocket[0] = (byte) ((code.getCode() >> 8) & 0xFF);
        pocket[1] = (byte) (code.getCode() & 0xFF);
        pocket[2] = (byte) ((length >> 8) & 0xFF);
        pocket[3] = (byte) (length & 0xFF);
        pocket[4] = (byte) ((from >> 8) & 0xFF);
        pocket[5] = (byte) (from & 0xFF);
        pocket[6] = (byte) ((to >> 8) & 0xFF);
        pocket[7] = (byte) (to & 0xFF);
        return pocket;
    }

    @Override
    public CODE getCode() {
        return code;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public int getTo() {
        return to;
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }


    /**
     * Add more flexibility when you already have package
     * but you need then add some data in it will recalculate cache
     *
     * @param length new length of new data to be set
     */

    @Override
    void setLength(int length) {
        this.length = length;
        raw = getRawHeader();
    }

    @Override
    public String toString() {
        return "DataPackageHeader{" +
                "code=" + code +
                ", length=" + length +
                ", from=" + from +
                ", to=" + to +
                ", raw=" + Arrays.toString(getRawHeader()) +
                '}';
    }
}
