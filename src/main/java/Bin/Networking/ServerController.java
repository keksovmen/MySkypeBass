package Bin.Networking;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Readers.ServerReader;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ServerWriter;
import Bin.Networking.Utility.ServerUser;

import java.io.IOException;
import java.net.Socket;

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

    void start(){
        try {
            if (authenticate())
                reader.start();
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    private boolean authenticate() throws IOException {
        BaseDataPackage dataPackage = reader.read();
        String name = dataPackage.getDataAsString();
        setUser(name);

        final int id = me.getId();

        writer.writeAudioFormat(id, server.getAudioFormat());
        DataPackagePool.returnPackage(dataPackage);
        dataPackage = reader.read();
        if (dataPackage.getHeader().getCode() != BaseWriter.CODE.SEND_APPROVE){
            writer.writeDisconnect(id);
            disconnect();
            DataPackagePool.returnPackage(dataPackage);
            return false;
        }
        DataPackagePool.returnPackage(dataPackage);

        writer.writeId(id);
        writer.writeUsers(id, server.getUsers(id));
        server.addUser(me);
        server.sendUpdateUsers();

        return true;
    }

    private void setUser(String name){
        me = new ServerUser(name, server.getIdAndIncrement(), this);
    }

    public ServerWriter getWriter() {
        return writer;
    }

    public int getId() {
        return me.getId();
    }

    public void disconnect() {
        server.removeUser(me.getId());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            server.sendUpdateUsers();
        }
    }

}
