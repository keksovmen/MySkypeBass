package Bin.Networking.DataParser;

import Bin.Networking.Writers.BaseWriter;

public class DataPackageHeader {

    public static final int INITIAL_SIZE = 8;

    private BaseWriter.CODE code;
    private int length;
    private int from;
    private int to;
    private byte[] raw;


    public void init (BaseWriter.CODE code, int length, int from, int to){
        this.code = code;
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

    //test this sheet with 2 bytes and more about negative numbers
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

    private int parser(byte[] data, int position){
        return (data[position] << 8) + data[++position];
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

    @Override
    public String toString() {
        return "DataPackageHeader{" +
                "code=" + code +
                ", length=" + length +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
