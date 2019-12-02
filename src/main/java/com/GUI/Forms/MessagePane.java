package com.GUI.Forms;

import com.Client.ButtonsHandler;
import com.Networking.Utility.Users.BaseUser;
import com.Pipeline.BUTTONS;
import com.Util.Algorithms;
import com.Util.FormatWorker;
import com.Util.History.History;
import com.Util.History.HistoryFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

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

    MessagePane(BaseUser forWho, Runnable closeTabAction, ButtonsHandler helpHandlerPredecessor) {
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

        Algorithms.registerPopUp(messageDisplay, messageGetter, this);
        Algorithms.registerPopUp(messageGetter, messageGetter, this);
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

    private void sendMessage(BaseUser user) {
        String message = messageGetter.getText();
        if (message.length() == 0) {
            return;
        }
        handleRequest(BUTTONS.SEND_MESSAGE,
                new Object[]{message, user});
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


}
