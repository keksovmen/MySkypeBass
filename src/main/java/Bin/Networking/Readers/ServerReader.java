package Bin.Networking.Readers;

import Bin.Networking.ServerController;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ServerWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerReader extends BaseReader {

    //    private ServerWriter writer;
    private ServerController controller;
    //    private ServerWriter writer;
    private List<Consumer<BaseDataPackage>> handlers;


    public ServerReader(InputStream inputStream, ServerController controller) {
        super(inputStream);
        this.controller = controller;
//        writer = controller.getWriter();
        handlers = new ArrayList<>();
    }


    @Override
    public void process() throws IOException {
        BaseDataPackage dataPackage = read();
        handlers.forEach(consumer -> consumer.accept(dataPackage));
//        switch (dataPackage.getHeader().getCode()){
//            case SEND_SOUND:{
//
//            }
//            case SEND_USERS:{
//
//            }
    }

    public void addListener(Consumer<BaseDataPackage> consumer) {
        handlers.add(consumer);
    }

//        System.out.println(dataPackage);
////        System.out.println("Server package = " + dataPackage.toString());
//        if (dataPackage.getTo() == BaseWriter.SERVER){
////            case BaseWriter.SERVER : {
//            switch (dataPackage.getInstruction()){
//                case BaseWriter.SEND_NAME : {
//                    controller.setUser(dataPackage.getDataAsString());
//                    controller.getWriter().writeId(controller.getMe().getId());
//                    controller.getWriter().writeAudioFormat(controller.getMe().getId(), Server.getInstance().getAudioFormat());
//                    Server.getInstance().sendUpdateUsers();
////                        controller.getWriter().writeUsers(controller.getMe().getId());
//                    break;
//                }
//
//                case BaseWriter.SEND_USERS : {
//                    controller.getWriter().writeUsers(dataPackage.getFrom());
////                        System.out.println("sended users to " + dataPackage.getFrom());
//                    break;
//                }
//
//                case BaseWriter.SEND_DISCONNECT : {
//                    controller.disconnect();
//                    throw new IOException();
//                }
//            }
//        }else if (dataPackage.getTo() == BaseWriter.CONFERENCE){
//            controller.getMe().getConversation().send(dataPackage, dataPackage.getFrom());
//        }else {
//            if (dataPackage.getInstruction() == BaseWriter.SEND_APPROVE){
////                if (Server.getInstance().getController())
//                ServerUser dude = Server.getInstance().getController(dataPackage.getTo()).getMe();
//                if (dude == null) {
//                    //send return and quit the method
//                    return;
//                }
//                if (controller.getMe().getConversation() == null
//                        && dude.getConversation() == null){
//                    Conversation conversation = new Conversation(controller.getMe(), dude);
//                    controller.getMe().setConversation(conversation);
//                    dude.setConversation(conversation);
//                }else if (controller.getMe().getConversation() == null){
//                    controller.getMe().setConversation(dude.getConversation());
//                    dude.getConversation().addDude(controller.getMe());
//                }else if (dude.getConversation() == null){
//                    dude.setConversation(controller.getMe().getConversation());
//                    controller.getMe().getConversation().addDude(dude);
//                }//think about combine both conversations
//
//            }
////            System.out.println("//////////////////////////");
//            ServerController controller = Server.getInstance().getController(dataPackage.getTo());
//            if (controller != null)
//                controller.getWriter().transferData(dataPackage);
//        }
//
//
////        }
//        DataPackage.returnObject(dataPackage);
//    }

    @Override
    public boolean start() {
        new Thread(() -> {
            while (work) {
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    work = false;
                    controller.disconnect();
                }
            }
        }, "Server Reader + " + controller.getId()).start();
        return true;
    }


}
