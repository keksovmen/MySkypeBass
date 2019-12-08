package com.Abstraction.Audio.Factory;

import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Audio.Input.ChangeableInput;
import com.Abstraction.Audio.Settings.BaseAudioSettings;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.ChangeableOutput;
import com.Abstraction.Client.ButtonsHandler;

public abstract class AudioFactory {



    public abstract AbstractAudioPlayer createPlayer();

    public abstract AbstractMicrophone createMicrophone(ButtonsHandler helpHandlerPredecessor);

    public abstract BaseAudioSettings createSettings(ChangeableInput input, ChangeableOutput output);


}
