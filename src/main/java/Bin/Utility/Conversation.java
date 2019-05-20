package Bin.Utility;


import Bin.Networking.Controller;
import Bin.Networking.DataParser.Package.BaseDataPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Conversation {

    private ArrayList<ServerUser> users;

    public Conversation(ServerUser... user) {
        users = new ArrayList<>();
        users.addAll(Arrays.asList(user));
    }

    /*
    think of sync that shit
     */
    public void send(BaseDataPackage dataPackage, int from){
        ServerUser[] local;
        synchronized (this) {
            local = users.toArray(new ServerUser[0]);
        }

        Stream.of(local).forEach(serverUser -> {
            if (serverUser.getId() != from){
//                try {
                Controller controller = serverUser.getController();
                if (controller != null)
                    controller.getWriter().transferData(dataPackage);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    public synchronized void addDude(ServerUser user){
        users.add(user);
    }

    public synchronized void removeDude(ServerUser user){
        users.remove(user);
    }

    public synchronized boolean contains(ServerUser user){
        return users.contains(user);
    }
}
