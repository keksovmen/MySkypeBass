package Com;

import Com.Model.ClientModel;
import Com.Networking.ClientController;
import Com.Networking.Processors.ClientProcessor;
import Com.Networking.Server;

/**
 * Represent a client
 * Include Networking, Audio, GUI
 */

public class Client {

    private Server server;

    private final ClientModel model;
    private final ClientProcessor processor;
    private final ClientController controller;

    //Here goes Audio part and GUI

    public Client() {
        model = new ClientModel();
        processor = new ClientProcessor();
        controller = new ClientController(processor, model);
    }

    private void initi(){
//        processor.getOnUsers().setListener();
        //Register listeners here
    }
}
