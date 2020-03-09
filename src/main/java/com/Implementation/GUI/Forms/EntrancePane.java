package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Resources.Resources;
import com.Implementation.GUI.Frame;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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
    private final Runnable showServerPane;


    public EntrancePane(ButtonsHandler helpHandlerPredecessor, Runnable showServerPane) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;

        $$$setupUI$$$();
        connectButton.addActionListener(e -> onConnect());

        createButton.addActionListener(e -> showServerPane.run());

        nameField.setText(Resources.getInstance().getDefaultName());
        ipField.setText(Resources.getInstance().getDefaultIP());
        portField.setText(Resources.getInstance().getDefaultPort());

        this.showServerPane = showServerPane;
    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        switch (action) {
            case CONNECT_FAILED:
            case PORT_OUT_OF_RANGE:
            case WRONG_HOST_NAME_FORMAT:
            case WRONG_PORT_FORMAT:
            case AUDIO_FORMAT_NOT_ACCEPTED:
                releaseConnectButton();
                mainPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                break;
            case CONNECT_SUCCEEDED:
                mainPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                break;
            case SERVER_CREATED:
                changeCreateServerButtonToStopServer();
                break;
            case CONNECTION_TO_SERVER_FAILED:
            case DISCONNECTED:
                releaseConnectButton();
                break;
            case SERVER_CLOSED:
                changeStopServerButtonToCreateServer();
                break;
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

    private void changeCreateServerButtonToStopServer() {
        createButton.setText("Stop server");
        clearServerButton();
        createButton.addActionListener(e -> helpHandlerPredecessor.handleRequest(BUTTONS.STOP_SERVER, null));
    }

    private void changeStopServerButtonToCreateServer() {
        createButton.setText("Create");
        clearServerButton();
        createButton.addActionListener(e -> showServerPane.run());
    }

    private void onConnect() {
        handleRequest(
                BUTTONS.CONNECT,
                new Object[]{getMyName(), getIp(), getPort()}
        );
        blockConnectButton();
        mainPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    private void clearServerButton() {
        for (ActionListener listener : createButton.getActionListeners()) {
            createButton.removeActionListener(listener);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        portField = new JFormattedTextField(Frame.getFormatter());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayoutManager(8, 2, new Insets(3, 3, 3, 3), -1, -1));
        mainPane.setFocusable(true);
        mainPane.setVisible(true);
        nameField = new JTextField();
        mainPane.add(nameField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        ipField = new JTextField();
        ipField.setText("");
        ipField.setToolTipText("format is xxx.xxx.xxx.xxx");
        mainPane.add(ipField, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        mainPane.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("IP");
        mainPane.add(label2, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portField.setText("");
        mainPane.add(portField, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Port");
        mainPane.add(label3, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectButton = new JButton();
        connectButton.setText("Connect");
        mainPane.add(connectButton, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Create");
        mainPane.add(createButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPane.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPane.add(spacer2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
