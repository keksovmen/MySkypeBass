package Bin.GUI.Forms;

import Bin.GUI.Main;
import Bin.Networking.Utility.Annotations.AnnotationsProcessor;
//import Bin.Utility.Annotations.ListenerFor;
import Bin.Networking.Utility.BaseUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ThirdSkin {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea textArea1;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField textField;
//    private final BaseUser who;


    public ThirdSkin(BaseUser who, SecondSkin root) {
//        this.who = who;

        nameWho.setText(who.getName());


        sendButton.addActionListener(e -> sendMessage(who));

        textField.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
        closeButton.addActionListener(e -> root.closeMessageTab(who));

        AnnotationsProcessor.process(this);
    }

    JPanel getMainPane() {
        return mainPane;
    }

    void showMessage(String message){
        EventQueue.invokeLater(() -> {
            if(message.length() != 0) textArea1.append(nameWho.getText() + ": " + message + "\n");
        });
    }

//    @ListenerFor(source="sendButton")
    private void sendMessage(BaseUser who){
        String message = textField.getText();
        if (message.equals("")) return;
        if (Main.getInstance().writeMessage(message, who.getId()))
            EventQueue.invokeLater(() -> {
                textArea1.append("Me: " + message + "\n");
                textField.setText("");
            });
    }
}
