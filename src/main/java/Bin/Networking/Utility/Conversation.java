package Bin.Networking.Utility;


import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.ServerController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Conversation {

    private volatile List<ServerUser> users;

    public Conversation(ServerUser... user) {
        users = new CopyOnWriteArrayList<>();
        for (ServerUser serverUser : user) {
            serverUser.setConversation(this);
        }
        users.addAll(Arrays.asList(user));

    }

    public Conversation(ServerUser[] rightSide, ServerUser[] leftSide) {
        users = new CopyOnWriteArrayList<>();
        for (ServerUser right : rightSide) {
            right.setConversation(this);
            for (ServerUser left : leftSide) {
//                try {
                right.getController().getWriter().writeAddToConv(left.getId(), right.getId());
//                } catch (IOException e) {
//                    e.printStackTrace();
                //handle some how
//                }
            }
        }
        for (ServerUser left : leftSide) {
            left.setConversation(this);
            for (ServerUser right : rightSide) {
//                try {
                left.getController().getWriter().writeAddToConv(right.getId(), left.getId());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }

        users.addAll(Arrays.asList(rightSide));
        users.addAll(Arrays.asList(leftSide));

    }

    /*
    think of sync that shit
     */
    public void send(BaseDataPackage dataPackage, int from) {
        for (ServerUser user : users) {
            if (user.getId() != from) {
                ServerController controller = user.getController();
                try {
                    controller.getWriter().transferAudio(dataPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                    removeDude(user);
//                    System.out.println("removed");
                }
            }
        }
        DataPackagePool.returnPackage(dataPackage);
    }

    public synchronized void addDude(ServerUser exclusive, ServerUser... user) {
        for (ServerUser serverUserExist : users) {
            if (!serverUserExist.equals(exclusive)) {
//                try {
                for (ServerUser serverUserToAdd : user) {
                    serverUserExist.getController().getWriter().writeAddToConv(serverUserToAdd.getId(), serverUserExist.getId());
                    serverUserToAdd.setConversation(this);
                }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    removeDude(serverUserExist);
//                }
            }
        }
        users.addAll(Arrays.asList(user));
    }

    public synchronized void removeDude(ServerUser user) {
        user.setConversation(null);
        if (!users.remove(user)) {
            return;
        }
        for (ServerUser serverUser : users) {
//            try {
            serverUser.getController().getWriter().writeRemoveFromConv(user.getId(), serverUser.getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//                removeDude(serverUser);
//            }
        }
        if (users.size() == 1) {
            ServerUser lastUser = users.get(0);
//            try {
            lastUser.getController().getWriter().writeStopConv(lastUser.getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            lastUser.setConversation(null);
            users.clear();
        }
    }

    public synchronized boolean contains(ServerUser user) {
        return users.contains(user);
    }

    public synchronized ServerUser[] getAll() {
        return users.toArray(new ServerUser[0]);
    }

    public synchronized String getAllToString(ServerUser exclusive) {
        StringBuilder result = new StringBuilder();
        for (ServerUser user : users) {
            if (!user.equals(exclusive)) {
                result.append(user).append("\n");
            }
        }
        return result.toString();
    }
}
