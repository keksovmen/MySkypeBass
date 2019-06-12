package Bin.Networking.DataParser;

import Bin.Networking.Writers.BaseWriter;

import java.util.Arrays;

public class DataPackageHeader {

    public static final int INITIAL_SIZE = 8;
    public static final int MAX_LENGTH = Short.MAX_VALUE * 2;

    private BaseWriter.CODE code;
    private int length;
    private int from;
    private int to;
    private byte[] raw;


    public void init (BaseWriter.CODE code, int length, int from, int to){
        this.code = code;
        if (length > MAX_LENGTH){
            throw new IllegalArgumentException("length must be less or equal to " + MAX_LENGTH);
        }
        this.length = length;
        this.from = from;
        this.to = to;
    }

    public void init (final byte[] data){
        code = BaseWriter.CODE.parse(parser(data, 0));
        length = parser(data, 2);
        from = parser(data, 4);
        to = parser(data, 6);
        raw = data;
    }

    private int parser(byte[] data, int position){
        return (((data[position]  & 0xff) << 8) + (data[++position] & 0xff));
    }

//    public static void main(String[] args) {
//        System.out.println(Integer.toBinaryString(-2));
//        DataPackageHeader dataPackageHeader = new DataPackageHeader();
//        dataPackageHeader.init(BaseWriter.CODE.SEND_MESSAGE, MAX_LENGTH, 3, 2);
//        System.out.println(dataPackageHeader);
//        dataPackageHeader.init(dataPackageHeader.getRawHeader());
//        System.out.println(dataPackageHeader);
//    }

    public byte[] getRawHeader(){
        byte[] pocket = new byte[INITIAL_SIZE];
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

    public BaseWriter.CODE getCode() {
        return code;
    }

    public int getLength() {
        return length;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public byte[] getRaw() {
        return raw;
    }

    void setLength(int length){
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
