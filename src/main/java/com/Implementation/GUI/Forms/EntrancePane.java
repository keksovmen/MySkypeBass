package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Implementation.GUI.Frame;
import com.Abstraction.Util.Resources;

import javax.swing.*;
import java.awt.*;

/**
 * You will see it first
 * Handle connect to a server
 * and call some thing to create a server
 * Have some parameters in properties
 */

public class EntrancePane implements LogicObserver, ButtonsHandler {


    private JTextField nameField;
    private JTextField ipField;
    private JFormattedTextField portField;
    private JButton connectButton;
    private JButton createButton;
    private JPanel mainPane;

    private final ButtonsHandler helpHandlerPredecessor;


    public EntrancePane(ButtonsHandler helpHandlerPredecessor, Runnable showServerPane) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;

        connectButton.addActionListener(e -> onConnect());

        createButton.addActionListener(e -> showServerPane.run());

        nameField.setText(Resources.getInstance().getDefaultName());
        ipField.setText(Resources.getInstance().getDefaultIP());
        portField.setText(Resources.getInstance().getDefaultPort());
    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        if (action.equals(ACTIONS.CONNECT_FAILED) ||
                action.equals(ACTIONS.PORT_OUT_OF_RANGE) ||
                action.equals(ACTIONS.WRONG_PORT_FORMAT) ||
                action.equals(ACTIONS.WRONG_HOST_NAME_FORMAT) ||
                action.equals(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED)
        ) {
            releaseConnectButton();
            mainPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        } else if (action.equals(ACTIONS.CONNECT_SUCCEEDED)) {
            mainPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        } else if (action.equals(ACTIONS.SERVER_CREATED)) {
            blockCreateServerButton();

        } else if (action.equals(ACTIONS.CONNECTION_TO_SERVER_FAILED) ||
                action.equals(ACTIONS.DISCONNECTED)
        ) {
            releaseConnectButton();
        }
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //delegate
        helpHandlerPredecessor.handleRequest(button, data);
    }

    /**
     * Lazy initialisation
     * and display server creation dialog
     */

    public JPanel getPane() {
        return mainPane;
    }

    /**
     * Gets name from the field or get system user name name
     * I hope it will work properly, because problems with encoding
     * with security and OS
     *
     * @return name
     */

    private String getMyName() {
        return nameField.getText().trim();
    }

    /**
     * Test if ip is acceptable if not return localhost
     *
     * @return ip
     */

    private String getIp() {
        return ipField.getText().trim();
    }

    /**
     * Test if is acceptable
     *
     * @return port or default
     */

    private String getPort() {
        return portField.getText().trim();
    }


    /**
     * For making button disabled or enabled when connecting
     */

    private void blockConnectButton() {
        connectButton.setEnabled(false);
    }

    private void releaseConnectButton() {
        connectButton.setEnabled(true);
    }

    private void blockCreateServerButton() {
        createButton.setEnabled(false);
    }

    private void releaseCreateServerButton() {
        createButton.setEnabled(true);
    }

    private void onConnect() {
        handleRequest(
                BUTTONS.CONNECT,
                new Object[]{getMyName(), getIp(), getPort()}
        );
        blockConnectButton();
        mainPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        portField = new JFormattedTextField(Frame.getFormatter());
    }
}
