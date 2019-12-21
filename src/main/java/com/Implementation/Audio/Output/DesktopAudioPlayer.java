package com.Implementation.Audio.Output;

import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.AudioPlayer;

public class DesktopAudioPlayer extends AudioPlayer {

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new CallNotificator();
    }
}
