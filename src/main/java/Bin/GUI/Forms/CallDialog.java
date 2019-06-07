package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.CallDialogActions;
import Bin.Networking.ClientController;
import Bin.Networking.Utility.BaseUser;

import javax.swing.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class CallDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel nameTo;
    private JButton denyButton;
    private JLabel conversationInfo;

    private CallDialogActions actions;

    public CallDialog(CallDialogActions actions) {
        this.actions = actions;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> {
            try {
                onOK(actions.acceptCall());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        buttonCancel.addActionListener(e -> {
            try {
                onCancel(actions.cancelCall());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        denyButton.addActionListener(e -> {
            try {
                onDeny(actions.denyCall());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

//    private void onOK(Consumer<String> acceptCall) {
//        // add your code here
//        acceptCall.accept(nameTo.getText() + "\n" + conversationInfo.getText());
//        dispose();
//    }
//
//    private void onCancel(Consumer<String> cancel) {
//        // add your code here if necessary
//        cancel.accept(nameTo.getText());
//        dispose();
//    }
//
//    private void onDeny(Consumer<String> denyCall){
//        denyCall.accept(nameTo.getText());
//        dispose();
//    }

    private void onOK(Consumer<BaseUser[]> acceptCall) {
        // add your code here
        BaseUser whoCall = BaseUser.parse(nameTo.getText());
        BaseUser[] convUsers = ClientController.parseUsers(conversationInfo.getText());
        BaseUser[] result = new BaseUser[convUsers.length + 1];

        System.arraycopy(convUsers, 0, result, 1, convUsers.length);
        result[0] = whoCall;

        acceptCall.accept(result);
        dispose();
    }

    private void onCancel(Consumer<BaseUser> cancel) {
        // add your code here if necessary
        cancel.accept(BaseUser.parse(nameTo.getText()));
        dispose();
    }

    private void onDeny(Consumer<BaseUser> denyCall){
        denyCall.accept(BaseUser.parse(nameTo.getText()));
        dispose();
    }

    void showOutcoming(String who){
        setTitle("Calling");

        nameTo.setText(who);

        nameTo.setVisible(true);
        conversationInfo.setVisible(false);
        buttonOK.setVisible(false);
        denyButton.setVisible(false);
        buttonCancel.setVisible(true);

        setLocationRelativeTo(getRootPane());

        setVisible(true);
    }

    void showIncoming(String fromWho, String conversationInfo){
        setTitle("Incoming");

        nameTo.setText(fromWho);
        this.conversationInfo.setText(conversationInfo);

        nameTo.setVisible(true);
        this.conversationInfo.setVisible(true);
        buttonOK.setVisible(true);
        denyButton.setVisible(true);
        buttonCancel.setVisible(false);

        setLocationRelativeTo(getRootPane());

        setVisible(true);
    }

}
