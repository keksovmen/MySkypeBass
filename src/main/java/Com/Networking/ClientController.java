package Com.Networking;

import Com.Audio.AudioClient;
import Com.Model.ClientModel;
import Com.Networking.Processors.ClientProcessor;
import Com.Networking.Processors.Processable;
import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.DataPackagePool;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.WHO;
import Com.Networking.Writers.ClientWriter;
import Com.Util.FormatWorker;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientController extends BaseController {

    //    private Socket socket;
    private final ClientProcessor processor;
//    private BaseUser me;
    private final ClientModel model;
    private ClientWriter writer;

//    private final ErrorHandler mainErrorHandler;

    /**
     * Uses only for holder of network stuff
     * and handle networking
     */

    public ClientController(ClientProcessor processor, ClientModel model) {
//        this.mainErrorHandler = mainErrorHandler;
        this.processor = processor;
        this.model = model;
    }

    /**
     * Try to establish a TCP connection
     *
     * @param hostName   ip address
     * @param port       to connect
     * @param bufferSize buffer size for reader and writer
     * @return true if connected to the server
     */

    public boolean connect(final String hostName,
                           final int port, final int bufferSize) {
        if (socket != null &&
                !socket.isClosed()) {
            throw new IllegalStateException("Client's socket is already opened. " +
                    "Close it before connecting again");
        }

        if (!FormatWorker.isHostNameCorrect(hostName))
            throw new IllegalArgumentException(
                    "Host name is in wrong format - " + hostName);

        socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(hostName, port), 7_000); // timeOut as property
            writer = new ClientWriter(socket.getOutputStream(), bufferSize);
            reader = new BaseReader(socket.getInputStream(), bufferSize);
        } catch (IOException e) {
//            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            return false;
        }

//        me = new BaseUser(name, WHO.NO_NAME.getCode());//just want base user to be immutable

        return true;
    }

    public boolean connect(final String hostName,
                           final String port, final String bufferSize) {
        return connect(
                hostName,
                Integer.parseInt(port),
                Integer.parseInt(bufferSize)
        );
    }

    /**
     * Trying to authenticate first writes your name
     * second read audio format and checks it if supported
     * send can use it or not
     * third creates client user with unique id from the server
     */

    @Override
    boolean authenticate() {
        try {
            writer.writeName(model.getMe().getName());

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
            model.setMe(new BaseUser(
                    model.getMe().getName(),
                    read.getHeader().getTo()
            ));
            AbstractDataPackagePool.returnPackage(read);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * You don't want to return package because it is processed in another thread
     *
     * @throws IOException if network fails
     */

    @Override
    void mainLoopAction() throws IOException {
        processor.process(reader.read());
    }

    @Override
    void dataInitialisation() {
        //Add all your possible action handler
//        Processor processor = new Processor();
//        processor.getOnUsers().setListener(Handlers.onUsers(this));
//        processor.getOnAddUserToList().setListener(Handlers.onAddUserToList(this));
//        processor.getOnRemoveUserFromList().setListener(Handlers.onRemoveUserFromList(this));
//        this.processor = processor;
    }

    @Override
    void cleanUp() {
        processor.close();
    }

    @Override
    Processable getProcessor() {
        return processor;
    }

    //    @Override
//    void mainLoopAction() throws IOException {
//
//    }

//    /**
//     * Default action for disconnecting the user
//     */

    //    public void disconnect() {
//        try {
//            writer.writeDisconnect(me.getId());
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
    public BaseUser getMe() {
        return model.getMe();
    }

//    public Registration getModel() {
//        return model;
//    }

    //
//    public ClientWriter getWriter() {
//        return writer;
//    }
}
