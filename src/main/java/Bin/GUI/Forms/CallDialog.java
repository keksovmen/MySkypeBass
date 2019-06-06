package Bin.GUI.Forms;

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

    public CallDialog(Consumer<String> cancelCall, Consumer<String> acceptCall, Consumer<String> denyCall) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK(acceptCall));

        buttonCancel.addActionListener(e -> onCancel(cancelCall));

        denyButton.addActionListener(e -> onDeny(denyCall));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                onCancel(cancelCall);
//            }
//        });

        // call onCancel() on ESCAPE
//        contentPane.registerKeyboardAction(e -> onCancel(cancelCall), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
    }

    private void onOK(Consumer<String> acceptCall) {
        // add your code here
        acceptCall.accept(nameTo.getText() + "\n" + conversationInfo.getText());
        dispose();
    }

    private void onCancel(Consumer<String> cancel) {
        // add your code here if necessary
        cancel.accept(nameTo.getText());
        dispose();
    }

    private void onDeny(Consumer<String> denyCall){
        denyCall.accept(nameTo.getText());
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
