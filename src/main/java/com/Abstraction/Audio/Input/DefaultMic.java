package com.Abstraction.Audio.Input;

import com.Abstraction.Util.Interfaces.Initialising;
import com.Abstraction.Util.Interfaces.Starting;

/**
 * Represent default microphone
 * That will on {@link Starting#start(String)} launch new thread
 * To capture and send audio data somewhere
 */

public interface DefaultMic extends Starting, Initialising {

}
