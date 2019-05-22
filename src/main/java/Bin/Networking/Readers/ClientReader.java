package Bin.Networking.Readers;

import Bin.Networking.Processors.ClientProcessor;

import java.io.IOException;
import java.io.InputStream;

public class ClientReader extends BaseReader {

    private ClientProcessor processor;

    public ClientReader(InputStream inputStream, ClientProcessor processor) {
        super(inputStream);
        this.processor = processor;
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
                }
            }
        }, "Client Reader").start();
        return true;
    }

}
