package com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.Audio.BaseAudio;
import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Resources.AbstractResources;
import com.Implementation.Audio.Factory.AudioDesktopFactory;
import com.Implementation.Audio.Helpers.SimpleHelper;
import com.Implementation.Client.Client;
import com.Implementation.GUI.Frame;
import com.Implementation.Util.DesktopResources;
import com.Implementation.Util.Logging.DesktopLogManager;

public class DesktopApplicationFactory extends AbstractApplicationFactory {

    @Override
    public AudioHelper createAudioHelper() {
        return new SimpleHelper();
    }

    @Override
    public AbstractResources createResources() {
        return new DesktopResources();
    }

    @Override
    public AbstractClient createClient(ChangeableModel model) {
        return new Client(model);
    }

    @Override
    public CompositeComponent createGUI() {
        return new Frame();
    }

    @Override
    public SimpleComponent createAudio(ButtonsHandler helpHandler, AudioFactory factory) {
        return new BaseAudio(helpHandler, factory);
    }

    @Override
    public AudioFactory createAudioFactory() {
        return new AudioDesktopFactory();
    }

    @Override
    public LogManagerHelper createLogManager() {
        return new DesktopLogManager();
    }
}
