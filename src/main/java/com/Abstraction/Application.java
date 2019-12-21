package com.Abstraction;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.Resources;

public class Application {

    protected final AbstractApplicationFactory factory;

    public Application(AbstractApplicationFactory factory) {
        this.factory = factory;
    }

    protected void uniqueChecks(){
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
    }

    protected void singletonInitialisation(){
        AbstractDataPackagePool.init(factory.createPool());
        AudioSupplier.setHelper(factory.createAudioHelper());
        Resources.setInstance(factory.createResources());
    }

    protected void createAndCommutateComponents(){
        ClientModelBase model = factory.createModel();
        AbstractClient client = factory.createClient(model);
        CompositeComponent gui = factory.createGUI();
        SimpleComponent audio = factory.createAudio(client, factory.createAudioFactory());

        model.attach(gui);
        model.attach(audio);

        client.attach(gui);
        client.attach(audio);

        gui.attach(client);
        gui.attach(audio);
    }

    public void start(){
        uniqueChecks();
        singletonInitialisation();
        createAndCommutateComponents();
    }

}
