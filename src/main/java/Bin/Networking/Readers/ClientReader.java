package Bin.Networking.Readers;

import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Writers.BaseWriter;

import java.io.IOException;
import java.io.InputStream;

public class ClientReader extends BaseReader implements ErrorHandler {

    private ClientProcessor processor;

    public ClientReader(InputStream inputStream, ClientProcessor processor) {
        super(inputStream);
        this.processor = processor;
    }

    public ClientReader(InputStream inputStream, ClientProcessor processor, ErrorHandler mainErrorHandler) {
        super(inputStream, mainErrorHandler);
        this.processor = processor;
    }

    private void process() {
        processor.doJob(readA());
    }

    public boolean start() {
        new Thread(() ->{
            while (work){
                    process();
            }
        }, "Client Reader").start();
        return true;
    }


//    @Override
//    public void errorCase() {
//        super.errorCase();
//    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }
}
