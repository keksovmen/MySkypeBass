package com.Pipeline;

import com.Client.ButtonsHandler;
import com.Client.LogicObserver;
import com.Model.Updater;

public interface SimpleComponent extends Updater, LogicObserver, ButtonsHandler {
}
