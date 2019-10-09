package com.GUI.Forms;

import com.GUI.Forms.ActionHolder.GUIActions;
import com.GUI.Forms.ActionHolder.GUIDuty;
import com.Networking.Utility.BaseUser;
import com.Pipeline.ACTIONS;
import com.Pipeline.ActionableLogic;
import com.Pipeline.ActionsHandler;
import com.Pipeline.BUTTONS;
import com.Util.FormatWorker;
import com.Util.Resources;

import javax.swing.*;
import java.awt.*;

/**
 * You will see it first
 * Handle connect to a server
 * and call some thing to create a server
 * Have some parameters in properties
 */

public class EntrancePane implements ActionsHandler {
    private JTextField nameField;
    private JTextField ipField;
    private JFormattedTextField portField;
    private JButton connectButton;
    private JButton createButton;
    private JPanel mainPane;


    public EntrancePane(ActionableLogic actionsForLogic, GUIDuty actionForGui) {
        connectButton.addActionListener(e -> {
            actionsForLogic.act(
                    BUTTONS.CONNECT,
                    new String[]{getMyName(), getIp(), getPort()},
                    null,
                    -1
            );
            blockConnectButton();
            mainPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        });

        createButton.addActionListener(e ->
                actionForGui.displayChanges(
                        GUIActions.CREATE_SERVER_PANE,
                        null
                ));

        nameField.setText(Resources.getDefaultName());
        ipField.setText(Resources.getDefaultIP());
        portField.setText(Resources.getDefaultPort());
    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        if (action.equals(ACTIONS.CONNECT_FAILED) ||
                action.equals(ACTIONS.PORT_OUT_OF_RANGE) ||
                action.equals(ACTIONS.WRONG_PORT_FORMAT)
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
        portField = new JFormattedTextField(FormatWorker.getFormatter());
    }
}
