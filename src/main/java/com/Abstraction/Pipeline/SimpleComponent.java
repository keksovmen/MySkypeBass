package com.Abstraction.Pipeline;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.Updater;

public interface SimpleComponent extends Updater, LogicObserver, ButtonsHandler {
}
