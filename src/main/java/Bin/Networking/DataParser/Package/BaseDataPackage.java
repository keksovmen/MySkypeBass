package Bin.Networking.DataParser.Package;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BaseDataPackage {

    /*
     * Based on object pool
     */
    private static final Charset charset = StandardCharsets.UTF_16;

    private DataPackageHeader header;
    private byte[] data;

//    private static Queue<BaseDataPackage> pool;

//    static {
//        pool = new ConcurrentLinkedDeque<>();
//    }

    public BaseDataPackage() {
        header = new DataPackageHeader();
    }


    public void init(DataPackageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public void init(DataPackageHeader header, String data) {
        init(header, data.getBytes(charset));
    }

    public void setData(byte[] data) {
//        init(header, data.getBytes(charset));
        this.data = data;
    }

    public DataPackageHeader getHeader() {
        return header;
    }

    public byte[] getData() {
        return data;
    }

    /*
     * data = read first int as length then byte[] with the length
     * parse it here
     */
//    public BaseDataPackage init(byte[] data) {
//        return init(DataParser.convertByteToInt(data, 0),
//                DataParser.convertByteToInt(data, 4),
//                DataParser.convertByteToInt(data, 8),
//                Arrays.copyOfRange(data, INITIAL_SIZE, data.length));
//    }

    public String getDataAsString() {
        return new String(data, charset);
    }


    public int getFullLength() {
        return DataPackageHeader.INITIAL_SIZE + (data == null ? 0 : data.length);
    }

    void clear(){
        data = new byte[0];
    }

//    @Override
//    public String toString() {
//        return "BaseDataPackage{" +
//                "from=" + from +
//                ", to=" + to +
//                ", instruction=" + instruction +
//                ",\n data=" + Arrays.toString(data) +
//                ",\nLength = " + getFullLength() +
//                '}';
//    }

//    public static BaseDataPackage getObject(){
//        BaseDataPackage dataPackage = pool.poll();
//        return dataPackage == null ? new BaseDataPackage() : dataPackage;
//    }

//    public static void returnObject(BaseDataPackage dataPackage){
//        pool.offer(makeNull(dataPackage));
//    }

//    private static BaseDataPackage makeNull(BaseDataPackage dataPackage){
//        dataPackage.to = 0;
//        dataPackage.from = 0;
//        dataPackage.instruction = 0;
//        dataPackage.data = null;
//        return dataPackage;
//    }
}
