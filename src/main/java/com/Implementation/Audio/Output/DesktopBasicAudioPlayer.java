package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.BasicAudioPlayer;

public class DesktopBasicAudioPlayer extends BasicAudioPlayer {

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new CallNotificator();
    }
}
