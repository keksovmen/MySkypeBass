package Bin.Networking.DataParser.Package;

import Bin.Networking.DataParser.DataParser;

import java.util.Arrays;

public class DataPackage extends BaseDataPackage {

    /*
    * Create this class only from builder
     */

    byte instruction;
    byte marker;
    byte[] data;
    int offset;





    public byte getMarker() {
        return marker;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "DataPackage{" +
                "from=" + from +
                ", to=" + to +
                ", instruction=" + instruction +
                ", marker=" + marker +
                ", data=" + Arrays.toString(data) +
                ",\n offset=" + offset +
                '}';
    }

}
