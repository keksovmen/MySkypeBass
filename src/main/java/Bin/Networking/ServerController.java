package Bin.Networking;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Readers.ServerReader;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ServerWriter;
import Bin.Networking.Utility.ServerUser;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerController {

    private ServerWriter writer;
    private ServerReader reader;
    private ServerUser me;
    private Socket socket;
    private Server server;

    public ServerController(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        writer = new ServerWriter(socket.getOutputStream());
        reader = new ServerReader(socket.getInputStream(), this);
    }

    /**
     * Start authenticate procedure
     * if success starts a new thread to handle the user
     * otherwise disconnect him
     */

    void start() {
        try {
            if (authenticate())
                launch();
//                reader.start();
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    private void launch() {
        reader.addListener(createUsersRequestListener(this));
        reader.addListener(createTransferHandler(this));
        reader.start();
    }

    private static Consumer<BaseDataPackage> createUsersRequestListener(ServerController controller) {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                try {
                    controller.writer.writeUsers(controller.me.getId(), controller.server.getUsers(controller.me.getId()));
                } catch (IOException e) {
                    e.printStackTrace();
                    controller.disconnect();
                }
            }
        };
    }

    private static Consumer<BaseDataPackage> createTransferHandler(ServerController controller){
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getTo() != BaseWriter.WHO.SERVER.getCode()){
                ServerController controllerReceiver = controller.server.getController(baseDataPackage.getHeader().getTo());
                try {
                    controllerReceiver.writer.transferData(baseDataPackage);//test it
                } catch (IOException e) {
                    e.printStackTrace();
                    controllerReceiver.disconnect();
                }
            }
        };
    }

    /**
     * Trying to register a new user for the server
     * first read name from the user
     * second writes audio format
     * third gets true or false on the audio format
     * than add user or disconnect him
     * after write all users on server to him
     * and notify all other users
     *
     * @return true if only audio format is accepted
     * @throws IOException if connection get ruined or disconnected
     */

    private boolean authenticate() throws IOException {
        BaseDataPackage dataPackage = reader.read();
        String name = dataPackage.getDataAsString();
        setUser(name);

        final int id = me.getId();

        writer.writeAudioFormat(id, server.getAudioFormat());
        DataPackagePool.returnPackage(dataPackage);
        dataPackage = reader.read();
        if (dataPackage.getHeader().getCode() != BaseWriter.CODE.SEND_APPROVE) {
            writer.writeDisconnect(id);
            disconnect();
            DataPackagePool.returnPackage(dataPackage);
            return false;
        }
        DataPackagePool.returnPackage(dataPackage);

//        writer.writeId(id);
//        writer.writeUsers(id, server.getUsers(id));
        server.addUser(me);
        server.sendUpdateUsers();

        return true;
    }

    public void disconnect() {
        server.removeUser(me.getId());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.sendUpdateUsers();
        }
    }

    private void setUser(String name) {
        me = new ServerUser(name, server.getIdAndIncrement(), this);
    }

    public ServerWriter getWriter() {
        return writer;
    }

    public int getId() {
        return me.getId();
    }

}
