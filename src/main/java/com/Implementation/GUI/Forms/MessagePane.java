package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.History.History;
import com.Abstraction.Util.History.HistoryFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Handle messaging part with some one
 * can sendSound message and close self tab
 */

class MessagePane implements ButtonsHandler {


    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageDisplay;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField messageGetter;

    private boolean isShown;

    private final History<String> history;

    private final ButtonsHandler helpHandlerPredecessor;

    /**
     * Default constructor init
     * 1 - actions
     * 2 - who to talk
     * 3 - register handlers
     * <p>
     * //     * @param name    baseUser.toString() with who you talk
     * //     * @param actions all your actions
     */

    MessagePane(User forWho, Runnable closeTabAction, ButtonsHandler helpHandlerPredecessor) {
        nameWho.setText(forWho.toString());

        sendButton.addActionListener(e -> sendMessage(forWho));

        closeButton.addActionListener(e -> {
            closeTabAction.run();
            isShown = false;
        });

        messageGetter.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        isShown = true;

        history = HistoryFactory.getStringHistory();

        this.helpHandlerPredecessor = helpHandlerPredecessor;

        messageGetter.registerKeyboardAction(e -> onUp(),
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                JComponent.WHEN_FOCUSED);
        messageGetter.registerKeyboardAction(e -> onDown(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_FOCUSED);

        MultiplePurposePane.registerPopUp(messageDisplay, messageGetter, this);
        MultiplePurposePane.registerPopUp(messageGetter, messageGetter, this);
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //delegate
        helpHandlerPredecessor.handleRequest(button, data);
    }

    JPanel getMainPane() {
        return mainPane;
    }


    /**
     * Displays message when you write or receive it
     * Also include who write and time
     *
     * @param message to display
     * @param me      define who write it
     */

    void showMessage(String message, boolean me) {
        if (message.length() != 0)
            messageDisplay.append((me ? "Me" : nameWho.getText()) +
                    " (" + FormatWorker.getTime() + "): " + message + "\n");
        isShown = true;
    }

    /**
     * Action for sending message
     * can't sendSound if there is empty string
     * also clear messageGetter where was your message
     */

    private void sendMessage(User user) {
        String message = messageGetter.getText();
        if (message.length() == 0) {
            return;
        }
        handleRequest(BUTTONS.SEND_MESSAGE,
                new Object[]{message, user.getId()});
        history.push(message);
        showMessage(message, true);
        messageGetter.setText("");
    }

    public boolean isShown() {
        return isShown;
    }

    private void onUp() {
        messageGetter.setText(history.getNext());
    }

    private void onDown() {
        messageGetter.setText("");
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayoutManager(4, 3, new Insets(3, 3, 3, 3), -1, -1));
        nameWho = new JLabel();
        nameWho.setText("Label");
        mainPane.add(nameWho, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        mainPane.add(sendButton, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        mainPane.add(closeButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPane.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        mainPane.add(scrollPane1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        messageDisplay = new JTextArea();
        messageDisplay.setDisabledTextColor(new Color(-4473925));
        messageDisplay.setEditable(false);
        messageDisplay.setEnabled(true);
        messageDisplay.setLineWrap(true);
        messageDisplay.setWrapStyleWord(true);
        scrollPane1.setViewportView(messageDisplay);
        messageGetter = new JTextField();
        mainPane.add(messageGetter, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
