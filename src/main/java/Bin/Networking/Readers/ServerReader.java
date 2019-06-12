package Bin.Networking.Readers;

import Bin.Networking.ServerController;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.Utility.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServerReader extends BaseReader {

//    private ServerController controller;
    private List<Consumer<BaseDataPackage>> handlers;
    private Supplier<Integer> id;


//    public ServerReader(InputStream inputStream, ServerController controller) {
//        super(inputStream);
//        this.controller = controller;
//        handlers = new ArrayList<>();
//    }

    public ServerReader(InputStream inputStream, Supplier<Integer> getId, ErrorHandler mainErrorHandler) {
        super(inputStream, mainErrorHandler);
        this.id = getId;
        handlers = new ArrayList<>();
    }

    public void process() {
        BaseDataPackage dataPackage = readA();
        if (dataPackage != null) {
            handlers.forEach(consumer -> consumer.accept(dataPackage));
        }
    }

    public void addListener(Consumer<BaseDataPackage> consumer) {
        handlers.add(consumer);
    }

    public boolean start() {
        new Thread(() -> {
            while (work) {
                process();
            }
        }, "Server Reader + " + id.get()).start();
        return true;
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }
}
