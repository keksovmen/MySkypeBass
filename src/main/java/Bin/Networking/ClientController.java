package Bin.Networking;

import Bin.Audio.AudioClient;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Processors.Processable;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.DataPackagePool;
import Bin.Networking.Readers.BaseReader;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.WHO;
import Bin.Networking.Writers.ClientWriter;
import Bin.Util.FormatWorker;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientController implements ErrorHandler {

    private Socket socket;
    private ClientWriter writer;
    private BaseReader reader;
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

    public boolean connect(final String name, final String hostName,
                           final int port, final int bufferSize) throws IOException {
        if (socket != null &&
                !socket.isClosed()) {
            throw new IllegalStateException("Client's socket is already opened. " +
                    "Close it before connecting again");
        }

        socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(hostName, port), 7_000);
            writer = new ClientWriter(socket.getOutputStream(), bufferSize);
            reader = new BaseReader(socket.getInputStream(), bufferSize);
//            reader = new ReaderWithHandler(socket.getInputStream(), processor, mainErrorHandler);
            if (!authenticate(name)){
                socket.close();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw e;
        }
        return true;
    }

    public boolean connect(final String name, final String hostName,
                           final String port, final String bufferSize) throws IOException {
        return connect(
                name,
                hostName,
                Integer.parseInt(port),
                Integer.parseInt(bufferSize)
        );
    }

    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private boolean authenticate(String name) throws IOException {
        writer.writeName(name);

        AbstractDataPackage read = reader.read();
        AudioFormat audioFormat = FormatWorker.parseAudioFormat(read.getDataAsString());
        DataPackagePool.returnPackage(read);

        //sets audio format and tell the server can speaker play format or not
        if (!AudioClient.getInstance().setAudioFormat(audioFormat)) {
            writer.writeDeny(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
            return false;
        }
        writer.writeAccept(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());

        read = reader.read();
        me = new BaseUser(name, read.getHeader().getTo());
        AbstractDataPackagePool.returnPackage(read);

        return true;
    }

    /**
     * Default action for disconnecting the user
     */

    public void disconnect() {
//        reader.close();
        try {
            writer.writeDisconnect(me.getId());
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
        return new ErrorHandler[]{};
    }
}
