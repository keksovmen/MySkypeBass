package Bin.Networking.Readers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackageHeader;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Utility.ErrorHandler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class BaseReader implements ErrorHandler {

    private final static int BUFFER_SIZE = 16384;

    protected DataInputStream inputStream;
    protected volatile boolean work;

    protected ErrorHandler mainErrorHandler;

    public BaseReader(InputStream inputStream) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        work = true;
    }

    public BaseReader(InputStream inputStream, ErrorHandler mainErrorHandler) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        work = true;
        this.mainErrorHandler = mainErrorHandler;
    }

    public BaseDataPackage read() throws IOException {
        BaseDataPackage aPackage = DataPackagePool.getPackage();

        byte[] header = new byte[DataPackageHeader.INITIAL_SIZE];
        inputStream.readFully(header);
        aPackage.getHeader().init(header);


        int length = aPackage.getHeader().getLength();
        if (length == 0) {
            return aPackage;
        }

        byte[] body = new byte[length];
        inputStream.readFully(body);
        aPackage.setData(body);

        return aPackage;
    }

    public BaseDataPackage readA(){
        BaseDataPackage aPackage = DataPackagePool.getPackage();
        try {

            byte[] header = new byte[DataPackageHeader.INITIAL_SIZE];
            inputStream.readFully(header);
            aPackage.getHeader().init(header);

            int length = aPackage.getHeader().getLength();
            if (length == 0) {
                return aPackage;
            }

            byte[] body = new byte[length];
            inputStream.readFully(body);
            aPackage.setData(body);

        }catch (IOException e){
            e.printStackTrace();
            if (work) { //control if it wasn't your disconnect action
                mainErrorHandler.errorCase();
            }
            return null;
        }
        return aPackage;
    }

    public void close() {
        work = false;
    }

    @Override
    public void errorCase() {
        work = false;
        iterate();
    }
}
