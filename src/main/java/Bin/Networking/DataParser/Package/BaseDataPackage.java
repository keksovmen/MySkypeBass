package Bin.Networking.DataParser.Package;

import Bin.Networking.DataParser.DataParser;
import com.sun.istack.internal.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BaseDataPackage {

    /*
    * Based on object pool
     */
    private static final int INITIAL_SIZE = 12;
    private static final Charset charset = StandardCharsets.UTF_16;

    int from;
    int to;
    int instruction;
    byte[] data;

    private static Queue<BaseDataPackage> pool;

    static {
        pool = new ConcurrentLinkedDeque<>();
    }

    protected BaseDataPackage(){}

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getInstruction() {
        return instruction;
    }

    public byte[] getData() {
        return data;
    }

    public BaseDataPackage init(int from, int to, int instruction, byte[] data){
        this.from = from;
        this.to = to;
        this.instruction = instruction;
        this.data = data;
        return this;
    }

    public BaseDataPackage init(String data, int from, int to, int instruction){
        this.from = from;
        this.to = to;
        this.instruction = instruction;
//        char[] array = Charset.defaultCharset().decode(ByteBuffer.wrap(data.getBytes())).array();
//        this.data = charset.encode(CharBuffer.wrap(array)).array();
//        this.data = charset.encode(data).array();
//        this.data = charset.encode(Charset.defaultCharset().decode(ByteBuffer.wrap(data.getBytes()))).array();
        this.data = data.getBytes(charset);
//        System.out.println("Data = " + Arrays.toString(this.data) + "Length = " + getLength() + "\n" + getDataAsString());
        return this;
    }
    /*
    * data = read first int as length then byte[] with the length
    * parse it here
     */
    public BaseDataPackage init (byte[] data){
        return init(DataParser.convertByteToInt(data, 0),
                DataParser.convertByteToInt(data, 4),
                DataParser.convertByteToInt(data, 8),
                Arrays.copyOfRange(data, INITIAL_SIZE, data.length));
    }

    public String getDataAsString(){
//        return charset.decode(ByteBuffer.wrap(data)).toString();
        return new String(data, charset);
    }

    public int getLength(){
        return INITIAL_SIZE + (data == null ? 0 : data.length);
    }

    @Override
    public String toString() {
        return "BaseDataPackage{" +
                "from=" + from +
                ", to=" + to +
                ", instruction=" + instruction +
                ",\n data=" + Arrays.toString(data) +
                ",\nLength = " + getLength() +
                '}';
    }

    public static BaseDataPackage getObject(){
        BaseDataPackage dataPackage = pool.poll();
        return dataPackage == null ? new BaseDataPackage() : dataPackage;
    }

    public static void returnObject(BaseDataPackage dataPackage){
        pool.offer(makeNull(dataPackage));
    }

    private static BaseDataPackage makeNull(BaseDataPackage dataPackage){
        dataPackage.to = 0;
        dataPackage.from = 0;
        dataPackage.instruction = 0;
        dataPackage.data = null;
        return dataPackage;
    }
}
