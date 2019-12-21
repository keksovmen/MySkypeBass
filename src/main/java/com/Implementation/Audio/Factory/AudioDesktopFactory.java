package com.Implementation.Audio.Factory;

import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Audio.Input.ChangeableInput;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.ChangeableOutput;
import com.Abstraction.Audio.Settings.BaseAudioSettings;
import com.Abstraction.Client.ButtonsHandler;
import com.Implementation.Audio.Input.Capture;
import com.Implementation.Audio.Output.DesktopAudioPlayer;

public class AudioDesktopFactory extends AudioFactory {

    @Override
    public AbstractAudioPlayer createPlayer() {
        return new DesktopAudioPlayer();
    }

    @Override
    public AbstractMicrophone createMicrophone(ButtonsHandler helpHandlerPredecessor) {
        return new Capture(helpHandlerPredecessor);
    }

    @Override
    public BaseAudioSettings createSettings(ChangeableInput input, ChangeableOutput output) {
        return new BaseAudioSettings(input, output);
    }
}
