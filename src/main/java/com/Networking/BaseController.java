package com.Networking;

import com.Networking.Processors.Processable;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;
import com.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.Socket;

/**
 * Template method
 * Start() is main method
 */

public abstract class BaseController implements Starting {

    protected Socket socket;
    protected BaseReader reader;
    protected volatile boolean work;

    protected BaseController(Socket socket) {
        this.socket = socket;
        //All init to null by default
    }

    /**
     * Happens first
     * Determine is connection accepted or not
     *
     * @return true if connection accepted
     */

    protected abstract boolean authenticate();

    /**
     * Lazy initialisation if such exists,
     * happens after authentication
     */

    protected abstract void dataInitialisation();

    /**
     * Called in the last moment of life cycle
     * Should release resources ext.
     */

    protected abstract void cleanUp();

    /**
     * @return object that will handle AbstractDataPackages
     */

    protected abstract Processable getProcessor();

    /**
     * If authentication failed
     */

    protected abstract void onAuthenticateError();

    /**
     * Action that will happen each time in a loop
     * until close() or error occurs
     *
     * @throws IOException if network fails
     */

    protected void mainLoopAction() throws IOException {
        AbstractDataPackage read = reader.read();
        getProcessor().process(read);
        DataPackagePool.returnPackage(read);
    }

    /**
     * Basically produce AbstractDataPackages and give them to
     * Processable until error or close() occurs
     */

    private void mainLoop() {
        while (work) {
            try {
                mainLoopAction();
            } catch (IOException e) {
                close();
            }
        }
    }

    /**
     * Start new thread that will produce and give away data packages
     *
     * @param name name of this thread
     * @return true if new tread was created false otherwise
     */

    @Override
    public boolean start(String name) {
        if (work)
//            throw new IllegalStateException("Already started");
            return false;
        work = true;

        if (!authenticate()) {
            onAuthenticateError();
            return false;
        }

        dataInitialisation();

        new Thread(() -> {
            mainLoop();
            cleanUp();
        }, name).start();
        return true;
    }

    /**
     * Release resources and stop this thread
     */

    @Override
    public synchronized void close() {
        work = false;
        if (socket.isClosed())
            return;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
