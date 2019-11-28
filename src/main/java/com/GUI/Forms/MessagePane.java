package com.GUI.Forms;

import com.Client.ButtonsHandler;
import com.Networking.Utility.Users.BaseUser;
import com.Pipeline.BUTTONS;
import com.Util.FormatWorker;
import com.Util.History.History;
import com.Util.History.HistoryFactory;
import com.Util.Resources;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handle messaging part with some one
 * can sendSound message and close self tab
 */

class MessagePane {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageBoard;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField messageGetter;

    private boolean isShown;

    private final History<String> history;

    /**
     * Default constructor init
     * 1 - actions
     * 2 - who to talk
     * 3 - register handlers
     * <p>
     * //     * @param name    baseUser.toString() with who you talk
     * //     * @param actions all your actions
     */

    MessagePane(BaseUser forWho, BiConsumer<String, BaseUser> sendMessage, Consumer<String> closeTabAction, ButtonsHandler actionHandler) {
        nameWho.setText(forWho.toString());

        sendButton.addActionListener(e -> this.sendMessage(sendMessage, forWho));

        messageGetter.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        closeButton.addActionListener(e -> {
            closeTabAction.accept(forWho.toString());
            isShown = false;
        });

        isShown = true;

        history = HistoryFactory.getStringHistory();

        messageGetter.registerKeyboardAction(e -> onUp(),
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                JComponent.WHEN_FOCUSED);
        messageGetter.registerKeyboardAction(e -> onDown(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_FOCUSED);

        registerPopUp(messageBoard, messageGetter, actionHandler);
        registerPopUp(messageGetter, messageGetter, actionHandler);
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
            messageBoard.append((me ? "Me" : nameWho.getText()) +
                    " (" + FormatWorker.getTime() + "): " + message + "\n");
        isShown = true;
    }

    /**
     * Action for sending message
     * can't sendSound if there is empty string
     * also clear messageGetter where was your message
     *
     * @param send function to call when need to sendSound
     */

    private void sendMessage(BiConsumer<String, BaseUser> send, BaseUser user) {
        String message = messageGetter.getText();
        if (message.length() == 0) {
            return;
        }
        send.accept(message, user);
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

    static void registerPopUp(JComponent component, JTextField textField, ButtonsHandler actionHandler) {
        JPopupMenu popupMenu = new JPopupMenu("Sounds");
        List<String> getDescriptions = Resources.getDescriptions();

        for (int i = 0; i < getDescriptions.size(); i++) {
            JMenuItem menuItem = new JMenuItem(getDescriptions.get(i));
            int j = i;
            menuItem.addActionListener(e -> {
                if (e.getModifiers() == InputEvent.META_MASK) {
                    actionHandler.handleRequest(BUTTONS.PREVIEW_SOUND, new Object[]{FormatWorker.asMessageMeta(j), j});
                } else {
                    textField.setText(textField.getText() + FormatWorker.asMessageMeta(j));
                }
            });
            popupMenu.add(menuItem);
        }
        component.setComponentPopupMenu(popupMenu);
    }

}
