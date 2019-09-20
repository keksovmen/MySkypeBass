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
        if (name.length() == 0)
            name = System.getProperty("user.name"); // or get from property map
        String hostName = data[1];
        if (hostName.length() == 0)
            hostName = "127.0.0.1"; // or get from property
        if (!FormatWorker.isHostNameCorrect(hostName)) {
            respond(ACTIONS.WRONG_HOST_NAME_FORMAT, null, hostName, null, -1);
            return;
        }
        String port = data[2];
        if (port.length() == 0)
            port = "8188"; // or get from property
        if (!FormatWorker.verifyPort(port)) {
            respond(ACTIONS.WRONG_PORT_FORMAT, null, port, null, -1);
            return;
        }
        onConnect(hostName, Integer.parseInt(port), name);
    }

    //should be called by Swing thread when press connect button
    void onConnect(String hostName, int port, String name) {
        model.setMe(new BaseUser(name, WHO.NO_NAME.getCode()));
        boolean connect = controller.connect(hostName, port, 8192);//Load buffer size from properties
        if (!connect) {
            //tell gui to show that can't connect to the server
            respond(ACTIONS.CONNECT_FAILED, null, null, null, -1);
            return;
        }
        boolean audioFormatAccepted = controller.start("Client Reader");
        if (!audioFormatAccepted) {
            //tell gui to show that audio format can't be set
            respond(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED, null,
                    "HERE GOES AUDIO FORMAT AS STRING", null, -1);
            return;
        }
        respond(ACTIONS.AUDIO_FORMAT_ACCEPTED, null,
                "HERE GOES AUDIO FORMAT AS STRING", null, -1);
        respond(ACTIONS.CONNECT_SUCCEEDED, null, null, null, -1);

        //connect
        //notify frame about your connection was it succeed or not
    }

    private void onServerCreate(Object plainData){
        String[] data = (String[]) plainData;
        String port = data[0];
        if (port.length() == 0)
            port = "8188"; // or get from property map
        if (!FormatWorker.verifyPort(port)){
            respond(ACTIONS.WRONG_PORT_FORMAT, null, port, null, -1);
            return;
        }
        String sampleRate = data[1];
        if (sampleRate.length() == 0 || !FormatWorker.verifyOnlyDigits(sampleRate)){
            respond(ACTIONS.WRONG_SAMPLE_RATE_FORMAT, null, sampleRate, null, -1);
            return;
        }
        String sampleSize = data[2];
        if (sampleSize.length() == 0 || !FormatWorker.verifyOnlyDigits(sampleSize)){
            respond(ACTIONS.WRONG_SAMPLE_SIZE_FORMAT, null, sampleSize, null, -1);
            return;
        }
        onServerCreate(port, sampleRate, sampleSize);
    }

    private void onServerCreate(String port, String sampleRate, String sampleSize){
        try {
            server = Server.getFromStrings(port, sampleRate, sampleSize);
        } catch (IOException e) {
            server = null;
            respond(ACTIONS.PORT_ALREADY_BUSY, null, port, null, -1);
            return;
//            e.printStackTrace();
        }
        boolean start = this.server.start("Server");
        if (start) {
            respond(ACTIONS.SERVER_CREATED, null, null, null, -1);
        }else {
            respond(ACTIONS.SERVER_CREATED_ALREADY, null, null, null, -1);
        }


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


}
