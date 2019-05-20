package Bin.Networking;

import Bin.Networking.Readers.ServerReader;
import Bin.Networking.Writers.ServerWriter;
import Bin.Utility.ServerUser;

import java.io.IOException;
import java.net.Socket;

public class Controller {

    private ServerWriter writer;
    private ServerReader reader;
    private ServerUser me;
    private Socket socket;
    private Server server;

    public Controller(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        writer = new ServerWriter(socket.getOutputStream());
        reader = new ServerReader(socket.getInputStream(), this);
    }

    void start(){
        reader.start();
    }

    public void setUser(String name){
        me = new ServerUser(name, server.getIdAndIncrement(), this);
        server.addUser(me);

    }

    public ServerWriter getWriter() {
        return writer;
    }

    public ServerUser getMe() {
        return me;
    }

    public void disconnect() throws IOException {
        server.removeUser(me.getId());
        socket.close();
    }

    public void sendUpdateUsers(){
        server.sendUpdateUsers();
    }


}
