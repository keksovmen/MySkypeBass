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

//    @Override
//    public void createServerSocket() {
//        A:
//        while (work){
//            try {
//                process();
//            } catch (IOException e) {
//                e.printStackTrace();
//                break A;
//            }
//        }
//        try {
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void process() throws IOException {
        processor.push(read());
    }

    @Override
    public void start() {
        new Thread(() ->{
            while (work){
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    work = false;
//                    processor.wakeUp();
                }
            }
        }, "Client Reader").start();
    }
}
