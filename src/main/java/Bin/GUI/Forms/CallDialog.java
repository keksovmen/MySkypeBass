package Bin.GUI.Forms;

import Bin.Main;
import Bin.Networking.Utility.ClientUser;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CallDialog {
    private JDialog dialog;
    private JLabel label;
    private JPanel contentPane;
    private JButton acceptButton;
    private JButton denyButton;
    private JButton cancelButton;

    private ClientUser who;
    private boolean shown;
    enum CALL {OUT, IN}
    private CALL state;


    public CallDialog(MainFrame mainFrame) {
        dialog = new JDialog(mainFrame, "Call", true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        label = new JLabel();
        contentPane = new JPanel();
//        contentPane.add(label);
        acceptButton = new JButton("Accept");
        denyButton = new JButton("Deny");
        cancelButton = new JButton("Cancel");
        dialog.setContentPane(contentPane);
        dialog.setLocation(mainFrame.getX() + 30, mainFrame.getY() + 30);
        dialog.setSize(200, 150);

        cancelButton.addActionListener(e -> onCancel());
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        denyButton.addActionListener(e -> onDeny());
        acceptButton.addActionListener(e -> onAccept());
    }

    synchronized boolean showCallingDialog(ClientUser who){
        if (shown) return false;
        shown = true;
        state = CALL.OUT;
        this.who = who;
        label.setText("Calling to " + who);
        contentPane.add(label);
        contentPane.add(cancelButton);
        dialog.setVisible(true);
        return true;
    }

    synchronized boolean showCallDialog(ClientUser who){
        if (shown) return false;
        shown = true;
        state = CALL.IN;
        this.who = who;
        label.setText("Call from " + who);
        contentPane.add(label);
        contentPane.add(acceptButton);
        contentPane.add(denyButton);
        dialog.setVisible(true);
        return true;
    }

    void dispose(){
        dialog.setVisible(false);
        shown = false;
        contentPane.removeAll();

    }

    public CALL getType(){
        return state;
    }

    private void onAccept(){
        //Main.accept();
//        Main.getInstance().acceptCall(who);
        dispose();
    }

    private void onDeny(){
        //Main.deny
//        Main.getInstance().denyCall(who);
        dispose();
    }

    private void onCancel(){
        //Main.cancel
//        Main.getInstance().cancelCall(who);
        dispose();
    }

    public ClientUser getWho() {
        return who;
    }
}
