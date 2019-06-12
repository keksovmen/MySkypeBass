package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.ThirdSkinActions;
import Bin.Networking.Utility.BaseUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.function.BiConsumer;

/**
 * Handle messaging part with some one
 * can send message and close self tab
 */

class ThirdSkin {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageBoard;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField textField;

    /**
     * Possible actions
     */

    private ThirdSkinActions actions;

    /**
     * Id of the user who you write
     */

    private int who;

    /**
     * Default constructor init
     * 1 - actions
     * 2 - who to talk
     * 3 - register handlers
     *
     * @param name    baseUser.toString() with who you talk
     * @param actions all your actions
     */

    ThirdSkin(String name, ThirdSkinActions actions) {
        this.actions = actions;
        who = BaseUser.parse(name).getId();

        nameWho.setText(name);

        sendButton.addActionListener(e -> {
            try {
                sendMessage(actions.sendMessage());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        textField.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        closeButton.addActionListener(e -> {
            try {
                actions.closeTab().run();
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });
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
        EventQueue.invokeLater(() -> {
            if (message.length() != 0)
                messageBoard.append((me ? "Me" : nameWho.getText()) + " (" + getTime() + "): " + message + "\n");
        });
    }

    /**
     * Action for sending message
     * can't send if there is empty string
     * also clear textField where was your message
     *
     * @param send function to call when need to send
     */

    private void sendMessage(BiConsumer<Integer, String> send) {
        String message = textField.getText();
        if (message.length() == 0) {
            return;
        }
        send.accept(who, message);
        showMessage(message, true);
        textField.setText("");
    }

    /**
     * Return simple string for time in messages
     *
     * @return time in format like 13:41
     */

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
}
