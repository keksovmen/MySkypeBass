package com.Abstraction;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.Resources.Resources;
import com.Abstraction.Util.Logging.LoggerUtils;


/**
 * Represent basic application initialisation
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;

public class Application {

    /**
     * From which you will get platform dependent resources
     */

    protected final AbstractApplicationFactory factory;

    private ClientModelBase model;
    private AbstractClient client;
    private CompositeComponent gui;
    private SimpleComponent audio;

    public Application(AbstractApplicationFactory factory) {
        this.factory = factory;
    }

    /**
     * Initialise enums, make sure they unique
     */

    protected void uniqueChecks() {
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        logInitialisation();
    }

    protected void logInitialisation(){
        try {
            Path parent = LoggerUtils.clientFilePath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectory(parent);
            }
            LogManager.getLogManager().readConfiguration(Application.class.getResourceAsStream("/properties/logging.properties"));
            LoggerUtils.initLoggers();
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Logging initialisation failure");
        }
    }

    /**
     * Singletons initialisation
     */

    protected void singletonInitialisation() {
        AbstractDataPackagePool.init(factory.createPool());
        AudioSupplier.setHelper(factory.createAudioHelper());
        Resources.setInstance(factory.createResources());
    }

    /**
     * Initialisation of all components
     * and connecting them together
     */

    protected void createAndCommutateComponents() {
        model = factory.createModel();
        client = factory.createClient(model);
        gui = factory.createGUI();
        audio = factory.createAudio(client, factory.createAudioFactory());

        model.attach(gui);
        model.attach(audio);

        client.attach(gui);
        client.attach(audio);

        gui.attach(client);
        gui.attach(audio);
    }

    /**
     * Lazy start of the application
     */

    public void start() {
        uniqueChecks();
        singletonInitialisation();
        createAndCommutateComponents();
    }

}
