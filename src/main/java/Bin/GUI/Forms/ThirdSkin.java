package Bin.GUI.Forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class ThirdSkin {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageBoard;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField textField;
//    private final BaseUser who;


    public ThirdSkin(String name, Consumer<String> sendMessage, Runnable closeTab) {
//        this.who = who;

        nameWho.setText(name);
//
//
        sendButton.addActionListener(e -> sendMessage(sendMessage));
//
        textField.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        closeButton.addActionListener(e -> closeTab.run());
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

    private void sendMessage(Consumer<String> send) {
        String message = textField.getText();
        if (message.length() == 0) return;
        send.accept(message);
        showMessage(message, true);
        textField.setText("");
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
}
