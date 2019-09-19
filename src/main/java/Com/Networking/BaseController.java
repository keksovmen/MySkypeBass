package Com.Networking;

import Com.Networking.Processors.Processable;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Utility.Starting;

import java.io.IOException;
import java.net.Socket;

public abstract class BaseController implements Starting {

    Socket socket;
    BaseReader reader;
    //    Processable processor;
    volatile boolean work;

    public BaseController() {
        //All init to null
    }

    abstract boolean authenticate();

    abstract void dataInitialisation();

    abstract void cleanUp();

    abstract Processable getProcessor();

    void mainLoopAction() throws IOException {
        getProcessor().process(reader.read());
    }

    private void mainLoop() {
        while (work) {
            try {
                mainLoopAction();
            } catch (IOException e) {
//                e.printStackTrace();
                close();
            }
        }
    }

    @Override
    public boolean start(String name) {
        if (work)
            return false;
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
    public void close() {
        work = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
