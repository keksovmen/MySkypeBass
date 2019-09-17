package Bin.Networking.Protocol;

import Bin.Util.Algorithms;

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
     * after reading a header
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
        raw = new byte[ProtocolBitMap.PACKET_SIZE];
    }

    /**
     * Unit test
     *
     * @return true if successful
     */

    public static boolean Test() {
        DataPackageHeader header = new DataPackageHeader();
        for (int i = -300; i < 300; i++) {
            try {
                header.init(CODE.SEND_MESSAGE, i, i, i);
            } catch (IllegalArgumentException e) {
                if (i >= 0) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        DataPackageHeader second = new DataPackageHeader();
        for (int i = 0; i < 3200; i++) {
            header.init(CODE.SEND_MESSAGE, i, i, i);
            second.init(header.getRawHeader());
            if (!second.equals(header)) {
                throw new IllegalStateException("Second isn't equal to first "
                        + header + "\n" + second);
            }
        }
        header.init(CODE.SEND_NAME, 1, 1, 1);
        second.init(new byte[]{0, 1, 0, 1, 0, 1, 0, 1});
        if (!second.equals(header)) {
            throw new IllegalStateException("Second isn't equal to first "
                    + header + "\n" + second);
        }
        header.init(CODE.SEND_NAME, 128, 128, 128);
        second.init(new byte[]{0, 1, 0, -128, 0, -128, 0, -128});
        if (!second.equals(header)) {
            throw new IllegalStateException("Second isn't equal to first "
                    + header + "\n" + second);
        }
        header.init(CODE.SEND_NAME, 256, 256, 256);
        second.init(new byte[]{0, 1, 1, 0, 1, 0, 1, 0});
        if (!second.equals(header)) {
            throw new IllegalStateException("Second isn't equal to first "
                    + header + "\n" + second);
        }
        final int max = ProtocolBitMap.MAX_VALUE;
        header.init(CODE.SEND_NAME, max, max, max);
        second.init(new byte[]{0, 1, -1, -1, -1, -1, -1, -1});
        if (!second.equals(header)) {
            throw new IllegalStateException("Second isn't equal to first "
                    + header + "\n" + second);
        }
        return true;
    }

    /**
     * Calculate raw header
     * Used when init not from raw
     * Or when you change the length
     */

    private void calculateRawHeader(){
        if (raw == null)
            raw = new byte[ProtocolBitMap.PACKET_SIZE];
        raw[0] = (byte) ((code.getCode() >>> 8) & 0xFF);
        raw[1] = (byte) (code.getCode() & 0xFF);
        raw[2] = (byte) ((length >>> 8) & 0xFF);
        raw[3] = (byte) (length & 0xFF);
        raw[4] = (byte) ((from >>> 8) & 0xFF);
        raw[5] = (byte) (from & 0xFF);
        raw[6] = (byte) ((to >>> 8) & 0xFF);
        raw[7] = (byte) (to & 0xFF);
    }

    /**
     * Default init method
     * Modifies current state
     * Checks for invariant
     *
     * @param code   instruction to set
     * @param length of data
     * @param from   sender id
     * @param to     sender id
     */

    @Override
    public void init(CODE code, int length, int from, int to) {
        this.code = code;
        if (ProtocolBitMap.MAX_VALUE < length ||
                ProtocolBitMap.MAX_VALUE < from ||
                ProtocolBitMap.MAX_VALUE < to ||
                length < 0 || from < 0 || to < 0) {
            throw new IllegalArgumentException("Arguments are too big or small, " +
                    "must be in range 0 <= args <= " + ProtocolBitMap.MAX_VALUE +
                    " args {length = " + length + ", from = " + from + ", to " + to + "}");
        }
        this.length = length;
        this.from = from;
        this.to = to;
        calculateRawHeader();
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
        code = CODE.parse(Algorithms.combineTwoBytes(data[0], data[1]));
        length = Algorithms.combineTwoBytes(data[2], data[3]);
        from = Algorithms.combineTwoBytes(data[4], data[5]);
        to = Algorithms.combineTwoBytes(data[6], data[7]);
        raw = data;
    }

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
        return raw;
    }

    @Override
    public CODE getCode() {
        return code;
    }

    @Override
    public int getLength() {
        return length;
    }

    /**
     * Add more flexibility when you already have package
     * but you need then add some data in
     * it will recalculate cache
     *
     * @param length new length of new data to be set
     */

    @Override
    void setLength(int length) {
        if (this.length == length)
            return;
        if (length < 0 || ProtocolBitMap.MAX_VALUE < length) {
            throw new IllegalArgumentException(
                    "Length is out of range 0 <= " +
                            length + " <= " + ProtocolBitMap.MAX_VALUE);
        }
        this.length = length;
        calculateRawHeader();
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
    public String toString() {
        return "DataPackageHeader{" +
                "code=" + code +
                ", length=" + length +
                ", from=" + from +
                ", to=" + to +
                ", raw=" + (raw == null ? Arrays.toString(getRawHeader()) : Arrays.toString(raw)) +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null) return false;
        if (obj instanceof DataPackageHeader) {
            return this.code.equals(((DataPackageHeader) obj).code) &&
                    this.length == ((DataPackageHeader) obj).length &&
                    this.from == ((DataPackageHeader) obj).from &&
                    this.to == ((DataPackageHeader) obj).to;
        }
        return false;
    }

}
