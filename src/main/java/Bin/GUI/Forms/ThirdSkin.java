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

public class ThirdSkin {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageBoard;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField textField;

    private ThirdSkinActions actions;
    private int who;


    public ThirdSkin(String name, ThirdSkinActions actions) {
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

    void showMessage(String message, boolean me) {
        EventQueue.invokeLater(() -> {
            if (message.length() != 0)
                messageBoard.append((me ? "Me" : nameWho.getText()) + " (" + getTime() + "): " + message + "\n");
        });
    }

    private void sendMessage(BiConsumer<Integer, String> send) {
        String message = textField.getText();
        if (message.length() == 0) return;
        send.accept(who, message);
        showMessage(message, true);
        textField.setText("");
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
}
