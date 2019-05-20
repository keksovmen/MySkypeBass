package Bin.Networking.DataParser.Package;

public class DataPackageBuilder {

    private DataPackage dataPackage;

    public void start(){
        dataPackage = new DataPackage();
    }

    public void setFrom(int from){
        dataPackage.from = from;
    }

    public void setTo(int to){
        dataPackage.to = to;
    }

    public void setInstruction(byte instruction){
        dataPackage.instruction = instruction;
    }

    public void setMarker(byte marker){
        dataPackage.marker = marker;
    }

    public void setData(byte[] data){
        dataPackage.data = data;
    }

    public void setOffset(int offset){
        dataPackage.offset = offset;
    }

    public DataPackage getDataPackage(){
        return dataPackage;
    }
}
