package Bin.Networking.DataParser;

import Bin.Networking.DataParser.Package.DataPackage;
import Bin.Networking.DataParser.Package.DataPackageBuilder;

import java.util.Arrays;

public abstract class DataParser {

    /*
    * //SingleTon class because of using in server part and client part
     */

    protected DataPackageBuilder builder;


    protected DataParser() {
        builder = new DataPackageBuilder();
    }

    /*
    * override if you want another implementation of dataPocket
     */

    public abstract DataPackage parseData(byte [] data);

    /*
    * data should be 4 bytes of size
     */

    public static int convertByteToInt(byte[] data, int offset){
        return ((data[offset++] << 8) | (data[offset++] << 8) | (data[offset++] << 8) | data[offset]);
    }



}
