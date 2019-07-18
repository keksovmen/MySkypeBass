package Bin.Networking;

import Bin.Audio.AudioClient;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Processors.Processable;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Readers.ReaderWithHandler;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.WHO;
import Bin.Networking.Writers.ClientWriter;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientController implements ErrorHandler {

    private Socket socket;
    private ClientWriter writer;
    private ReaderWithHandler reader;
    private final Processable processor;
    private BaseUser me;

    private final ErrorHandler mainErrorHandler;

    /**
     * Uses only for holder of network stuff
     * and handle networking
     */

    public ClientController(ErrorHandler mainErrorHandler) {
        this.mainErrorHandler = mainErrorHandler;
        processor = new ClientProcessor();
    }

    /**
     * Try to establish a TCP connection
     *
     * @param hostName ip address
     * @param port     to connect
     * @param name     your nickname to others
     * @return true if audio format is supported false otherwise
     */

    public boolean connect(final String name, final String hostName, final int port) {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostName, port), 7_000);
            writer = new ClientWriter(socket.getOutputStream(), mainErrorHandler);
            reader = new ReaderWithHandler(socket.getInputStream(), processor, mainErrorHandler);
            authenticate(name);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public boolean connect(final String name, final String hostName, final String port) {
        return connect(name, hostName, Integer.parseInt(port));
    }

    /**
     * Trying to authenticate first writes your name
     * second read audio format and checks it if supported
     * send can use speaker or not
     * third creates client user with unique id from the server
     * then starts a new thread for readings from socket
     *
     * @param name your nickname
     */

    private void authenticate(String name) throws IOException {
        writer.writeName(name);
        AbstractDataPackage read = reader.read();
        AudioFormat audioFormat = Server.parseAudioFormat(read.getDataAsString());
        //sets audio format and tell the server can speaker play format or not
        if (!AudioClient.getInstance().setAudioFormat(audioFormat)) {
            writer.writeDeny(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
        }
        writer.writeAccept(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
        me = new BaseUser(name, read.getHeader().getTo());
        AbstractDataPackagePool.returnPackage(read);
        reader.start("Client reader");
    }

    /**
     * Default action for disconnecting the user
     */

    public void disconnect() {
        reader.close();
        writer.writeDisconnect(me.getId());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public BaseUser getMe() {
        return me;
    }

    public ClientWriter getWriter() {
        return writer;
    }

    public Processable getProcessor() {
        return processor;
    }

    @Override
    public void errorCase() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            iterate();
        }
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{reader};
    }
}
