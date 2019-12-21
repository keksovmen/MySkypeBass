package com.Implementation.GUI;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;

import javax.swing.*;

public interface SimpleGUIComponent extends LogicObserver, ButtonsHandler {

    /**
     * @return underlying JPanel
     */

    JPanel getPanel();
}
