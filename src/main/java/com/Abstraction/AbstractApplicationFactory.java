package com.Abstraction;

import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.AbstractResources;

public abstract class AbstractApplicationFactory {

    public AbstractDataPackagePool createPool() {
        return new DataPackagePool();
    }

    public abstract AudioHelper createAudioHelper();

    public abstract AbstractResources createResources();

    public ClientModelBase createModel() {
        return new ClientModelBase();
    }

    public abstract AbstractClient createClient(ChangeableModel model);

    public abstract CompositeComponent createGUI();

    public abstract SimpleComponent createAudio(ButtonsHandler helpHandler, AudioFactory factory);

    public abstract AudioFactory createAudioFactory();

}
