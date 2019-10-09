package com.Networking;

import com.Networking.Processors.Processable;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;
import com.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.Socket;

public abstract class BaseController implements Starting {

    Socket socket;
    BaseReader reader;
    volatile boolean work;

    public BaseController() {
        //All init to null
    }

    abstract boolean authenticate();

    abstract void dataInitialisation();

    abstract void cleanUp();

    abstract Processable getProcessor();

    void mainLoopAction() throws IOException {
        AbstractDataPackage read = reader.read();
        getProcessor().process(read);
        DataPackagePool.returnPackage(read);
    }

    private void mainLoop() {
        while (work) {
            try {
                mainLoopAction();
            } catch (IOException e) {
                close();
            }
        }
    }

    @Override
    public boolean start(String name) {
        if (work)
            throw new IllegalStateException("Already started");
//            return false;
        work = true;

        if (!authenticate()) {
            close();
            return false;
        }

        dataInitialisation();

        new Thread(() -> {
            mainLoop();
            cleanUp();
        }, name).start();
        return true;
    }

    @Override
    public synchronized void close() {
        work = false;
        if (socket.isClosed())
            return;
        try {
            socket.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }
}
