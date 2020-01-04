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
import com.Abstraction.Util.Resources.AbstractResources;

/**
 * Main factory that is used by Application
 */

public abstract class AbstractApplicationFactory {

    /**
     * From which you will initialise data packages for I/O
     *
     * @return object pool pattern
     */

    public AbstractDataPackagePool createPool() {
        return new DataPackagePool();
    }

    /**
     * Contain default audio methods
     *
     * @return helper that depends on platform
     */

    public abstract AudioHelper createAudioHelper();

    /**
     * @return resources manager that depend on platform
     */

    public abstract AbstractResources createResources();

    /**
     * @return model
     */

    public ClientModelBase createModel() {
        return new ClientModelBase();
    }

    /**
     * @param model which he will modelObservation
     * @return client that handle buttons
     */

    public abstract AbstractClient createClient(ChangeableModel model);

    /**
     * @return platform dependent GUI
     */

    public abstract CompositeComponent createGUI();

    /**
     * @param helpHandler when you send captured audio
     * @param factory     from which you will initialise platform dependent resources
     * @return platform dependent audio networkHelper
     */

    public abstract SimpleComponent createAudio(ButtonsHandler helpHandler, AudioFactory factory);

    /**
     * @return platform dependent audio factory
     */

    public abstract AudioFactory createAudioFactory();

}
