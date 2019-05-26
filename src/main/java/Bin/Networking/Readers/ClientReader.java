package Bin.Networking.Readers;

import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Writers.BaseWriter;

import java.io.IOException;
import java.io.InputStream;

public class ClientReader extends BaseReader {

    private ClientProcessor processor;
//    private Runnable disconnectAction;

    public ClientReader(InputStream inputStream, ClientProcessor processor /*Runnable disconnectAction*/) {
        super(inputStream);
        this.processor = processor;
//        this.disconnectAction = disconnectAction;
    }

    @Override
    public void process() throws IOException {
        processor.doJob(read());
    }

    @Override
    public boolean start() {
        new Thread(() ->{
            while (work){
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    work = false;
                    processor.doJob(DataPackagePool.getPackage().init(BaseWriter.CODE.SEND_DISCONNECT, BaseWriter.WHO.SERVER.getCode(), BaseWriter.WHO.NO_NAME.getCode()));
//                    disconnectAction.run();
                }
            }
        }, "Client Reader").start();
        return true;
    }

}
