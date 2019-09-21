package Com;

import Com.GUI.Frame;
import Com.Model.ClientModel;
import Com.Model.Registration;
import Com.Networking.ClientController;
import Com.Networking.Processors.ClientProcessor;
import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Server;
import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.WHO;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.CivilDuty;
import Com.Pipeline.WarDuty;
import Com.Util.FormatWorker;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a client
 * Include Networking, Audio, GUI
 */

public class Client implements Registration<CivilDuty>, CivilDuty, WarDuty {

//    private InitialDataStorage

    private Server server;

    private final ClientModel model;
    private final ClientProcessor processor;
    private final ClientController controller;

    private final List<CivilDuty> civilDutyList;

    //Here goes Audio part and GUI
    private Frame frame;


    public Client() {
        model = new ClientModel();
        processor = new ClientProcessor();
        controller = new ClientController(processor, model);
        civilDutyList = new ArrayList<>();

        try {
            SwingUtilities.invokeAndWait(() -> frame = new Frame());
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }

        init();
    }

    private void init() {
//        processor.getOnUsers().setListener();
        //Register listeners here
        processor.getOnUsers().setListener(this::onUsers);
        processor.getOnAddUserToList().setListener(this::onAddUserToList);
        processor.getOnRemoveUserFromList().setListener(this::onRemoveUserFromList);
        processor.getOnMessage().setListener(this::onIncomingMessage);
        model.registerListener(frame);
        registerListener(frame);
        frame.registerListener(this);
    }

    @Override
    public boolean registerListener(CivilDuty listener) {
        return civilDutyList.add(listener);
    }

    @Override
    public boolean removeListener(CivilDuty listener) {
        return civilDutyList.remove(listener);
    }

    @Override
    public void fight(BUTTONS button, Object plainData, String stringData, int integerData) {
        switch (button){ //there probably will be swing thread
            case CONNECT:{
                processor.execute(() -> onConnect(plainData));
                return;
            }
            case CREATE_SERVER:{
                onServerCreate(plainData);
                return;
            }
            case ASC_FOR_USERS:{
//                System.out.println("ASK");
                onUsersRequest();
                return;
            }
            case SEND_MESSAGE:{
                onMessageSend(integerData, stringData);
                return;
            }

        }
    }

    @Override
    public void respond (ACTIONS action,
                                 BaseUser from,
                                 String stringData,
                                 byte[] bytesData,
                                 int intData) {
        civilDutyList.forEach(
                civilDuty -> civilDuty.respond(
                        action,
                        from,
                        stringData,
                        bytesData,
                        intData
                )
        );

    }

    /* Action for UI here */

    void onConnect(Object plainData){
        String[] data = (String[]) plainData;

        String name = data[0];
        if (FormatWorker.checkZeroLength(name))
            name = System.getProperty("user.name"); // or get from property map

        String hostName = data[1];
        if (FormatWorker.checkZeroLength(hostName))
            hostName = "127.0.0.1"; // or get from property
        if (!FormatWorker.isHostNameCorrect(hostName)) {
            stringRespond(ACTIONS.WRONG_HOST_NAME_FORMAT, hostName);
            return;
        }

        String port = data[2];
        if (FormatWorker.checkZeroLength(port))
            port = "8188"; // or get from property
        if (!checkPort(port))
            return;
        onConnect(hostName, Integer.parseInt(port), name);
    }

    //should be called by Swing thread when press connect button
    void onConnect(String hostName, int port, String name) {
        model.setMe(new BaseUser(name, WHO.NO_NAME.getCode()));
        boolean connect = controller.connect(hostName, port, 8192);//Load buffer size from properties
        if (!connect) {
            //tell gui to show that can't connect to the server
            plainRespond(ACTIONS.CONNECT_FAILED);
            return;
        }
        boolean audioFormatAccepted = controller.start("Client Reader");
        if (!audioFormatAccepted) {
            //tell gui to show that audio format can't be set
            stringRespond(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED,
                    "HERE GOES AUDIO FORMAT AS STRING");
            return;
        }
        stringRespond(ACTIONS.AUDIO_FORMAT_ACCEPTED,
                "HERE GOES AUDIO FORMAT AS STRING");

        stringRespond(ACTIONS.CONNECT_SUCCEEDED, model.getMe().toString());
    }

    private void onServerCreate(Object plainData){
        String[] data = (String[]) plainData;
        String port = data[0];
        if (FormatWorker.checkZeroLength(port))
            port = "8188"; // or get from property map
        if (!checkPort(port))
            return;

        String sampleRate = data[1];
        if (FormatWorker.checkZeroLength(sampleRate)
                || !FormatWorker.verifyOnlyDigits(sampleRate)){
            stringRespond(ACTIONS.WRONG_SAMPLE_RATE_FORMAT, sampleRate);
            return;
        }

        String sampleSize = data[2];
        if (FormatWorker.checkZeroLength(sampleSize)
                || !FormatWorker.verifyOnlyDigits(sampleSize)){
            stringRespond(ACTIONS.WRONG_SAMPLE_SIZE_FORMAT, sampleSize);
            return;
        }

        onServerCreate(port, sampleRate, sampleSize);
    }

    private void onServerCreate(String port, String sampleRate, String sampleSize){
        try {
            server = Server.getFromStrings(port, sampleRate, sampleSize);
        } catch (IOException e) {
            server = null;
            stringRespond(ACTIONS.PORT_ALREADY_BUSY, port);
            return;
        }
        boolean start = this.server.start("Server");
        if (start) {
            plainRespond(ACTIONS.SERVER_CREATED);
        }else {
            plainRespond(ACTIONS.SERVER_CREATED_ALREADY);
        }
    }

    private boolean checkPort(String port){
        if (!FormatWorker.verifyPortFormat(port)) {
            stringRespond(ACTIONS.WRONG_PORT_FORMAT, port);
            return false;
        }
        int portAsInt = Integer.parseInt(port);
        if (!FormatWorker.portInRange(portAsInt)){
            respond(
                    ACTIONS.PORT_OUT_OF_RANGE,
                    null,
                    "0 < port < " + 0xFFFF,
                    null,
                    portAsInt
            );
            return false;
        }
        return true;
    }

    private void onUsersRequest(){
        try {
            controller.getWriter().writeUsersRequest(model.getMe().getId());
        } catch (IOException e) {
            onNetworkException();
        }
    }

    private void plainRespond(ACTIONS action){
        respond(action, null, null, null, -1);
    }

    private void stringRespond(ACTIONS action, String data){
        respond(action, null, data, null, -1);
    }

    private void onMessageSend(int to, String message){
        try {
            controller.getWriter().writeMessage(model.getMe().getId(), to, message);
        } catch (IOException e) {
//            e.printStackTrace();//disconnected act
            onNetworkException();
//            plainRespond(ACTIONS.CONNECTION_SERVER_FAILED);

        }
    }

    private void onNetworkException(){
        plainRespond(ACTIONS.CONNECTION_SERVER_FAILED);
        controller.close();
        processor.close();
    }

    /* Listeners on receive here */

    void onUsers(AbstractDataPackage dataPackage) {
        String users = dataPackage.getDataAsString();
        model.updateModel(BaseUser.parseUsers(users));
    }

    void onAddUserToList(AbstractDataPackage dataPackage) {
        String user = dataPackage.getDataAsString();
        model.addToModel(BaseUser.parse(user));
    }

    void onRemoveUserFromList(AbstractDataPackage dataPackage) {
        int user = dataPackage.getDataAsInt();
        model.removeFromModel(user);
    }

    void onIncomingMessage(AbstractDataPackage dataPackage){
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        respond(
                ACTIONS.INCOMING_MESSAGE,
                sender,
                dataPackage.getDataAsString(),
                null,
                -1
        );
    }

    /* Other stuff here */


}
