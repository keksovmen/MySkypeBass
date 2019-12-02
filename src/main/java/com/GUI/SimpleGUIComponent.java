package com.GUI;

import com.Client.ButtonsHandler;
import com.Client.LogicObserver;

import javax.swing.*;

public interface SimpleGUIComponent extends LogicObserver, ButtonsHandler {

    /**
     * @return underlying JPanel
     */

    JPanel getPanel();
}
