package Bin.Networking.DataParser;

import Bin.Networking.DataParser.Package.DataPackage;

public class MyParser extends DataParser {


    @Override
    public DataPackage parseData(byte [] data){
        builder.start();
        builder.setFrom(convertByteToInt(data, 0));
        builder.setTo(convertByteToInt(data, 4));
        builder.setInstruction(data[8]);
        builder.setMarker(data[9]);
        builder.setOffset(10);
        builder.setData(data);
        return builder.getDataPackage();
    }
}
