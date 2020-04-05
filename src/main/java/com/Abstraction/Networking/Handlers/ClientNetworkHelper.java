package com.Abstraction.Networking.Handlers;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Networking.BaseDataPackageRouter;
import com.Abstraction.Networking.ClientDataPackageRouter;
import com.Abstraction.Networking.Processors.ClientProcessor;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Interfaces.Starting;
import com.Abstraction.Util.Logging.LogManagerHelper;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClientNetworkHelper implements Starting {

    protected final AbstractClient client;

    protected final Socket socket;
    protected final Processable processor;
    protected final BaseDataPackageRouter packageRouter;
    protected final DatagramSocket datagramSocket;

    protected volatile boolean isWorking;

    /**
     * @param client         to delegate some operations
     * @param socket         must be not null and connected
     * @param datagramSocket if null mean full TCP connection
     */

    public ClientNetworkHelper(AbstractClient client, Socket socket, DatagramSocket datagramSocket) {
        this.client = client;
        this.socket = socket;
        processor = createProcessor();
        packageRouter = createPackageRouter();
        this.datagramSocket = datagramSocket;
    }


    @Override
    public boolean start(String name) {
        if (isWorking) return false;

        isWorking = true;
        new Thread(this::handleLoopTCP, name + " TCP").start();
        if (datagramSocket != null)
            new Thread(this::handleLoopUDP, name + " UDP").start();
        return true;
    }

    @Override
    public void close() {
        if (!isWorking)
            return;
        isWorking = false;
        processor.close();

        Algorithms.closeSocketThatCouldBeClosed(socket);
        Algorithms.closeSocketThatCouldBeClosed(datagramSocket);

        LogManagerHelper.getInstance().getClientLogger().logp(
                this.getClass().getName(), "close", "Connection closed");
    }

    public boolean isWorking() {
        return isWorking;
    }

    protected Processable createProcessor() {
        return new ClientProcessor(client.getModel(), client);
    }

    protected BaseDataPackageRouter createPackageRouter() {
        return new ClientDataPackageRouter();
    }

    private void handleLoopTCP() {
        Reader readerTCP = client.getModel().getMyself().getReaderTCP();
        while (isWorking) {
            try {
                if (!packageRouter.handleDataPackageRouting(readerTCP, processor)) {
                    //if router did route but processor didn't handle it
                    close();
                }
            } catch (IOException e) {
                //if router didn't route due to network failure
                if (isWorking) {
                    LogManagerHelper.getInstance().getClientLogger().logp(
                            this.getClass().getName(),
                            "handleLoopTCP", "Connection died dut to network failure");
                    client.notifyObservers(ACTIONS.CONNECTION_TO_SERVER_FAILED, null);
                    close();
                }
            }
        }
    }

    private void handleLoopUDP() {
        Reader readerUDP = client.getModel().getMyself().getReaderUDP();
        while (isWorking) {
            try {
                if (!packageRouter.handleDataPackageRouting(readerUDP, processor)) {
                    //if router did route but processor didn't handle it
                    close();
                }
            } catch (IOException e) {
                //if router didn't route due to network failure
                //tcp will handle all errors
            }
        }
    }

}
