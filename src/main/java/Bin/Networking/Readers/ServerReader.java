package Bin.Networking.Readers;

import Bin.Networking.ServerController;
import Bin.Networking.DataParser.BaseDataPackage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerReader extends BaseReader {

    private ServerController controller;
    private List<Consumer<BaseDataPackage>> handlers;


    public ServerReader(InputStream inputStream, ServerController controller) {
        super(inputStream);
        this.controller = controller;
        handlers = new ArrayList<>();
    }


    @Override
    public void process() throws IOException {
        BaseDataPackage dataPackage = read();
        handlers.forEach(consumer -> consumer.accept(dataPackage));
    }

    public void addListener(Consumer<BaseDataPackage> consumer) {
        handlers.add(consumer);
    }

    @Override
    public boolean start() {
        new Thread(() -> {
            while (work) {
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    work = false;
                    controller.disconnect();
                }
            }
        }, "Server Reader + " + controller.getId()).start();
        return true;
    }


}
