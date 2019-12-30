package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

import javax.swing.*;
import java.awt.*;

/**
 * Handle calls
 * Gives you an opportunity to cancel, deny and approve calls
 */

public class CallDialog extends JDialog implements LogicObserver, ButtonsHandler {


    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel nameTo;
    private JButton denyButton;
    private JLabel conversationInfo;
    private JLabel middleLabel;

    private BaseUser user;
    private String dudes;

    private final ButtonsHandler helpHandlerPredecessor;
    private final JComponent relativeTo;

    /**
     * Single constructor
     * Init and modelObservation actions
     * register listeners
     * And don't show the dialog for this purpose here is a method
     */

    public CallDialog(ButtonsHandler helpHandlerPredecessor, JComponent relativeTo) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        this.relativeTo = relativeTo;

        buttonOK.addActionListener(e -> onOk());

        denyButton.addActionListener(e -> onDeny());

        buttonCancel.addActionListener(e -> onCancel());

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        switch (action) {
            case CALL_DENIED: {
                dispose();
                return;
            }
            case CALL_ACCEPTED: {
                dispose();
                return;
            }
            case CALL_CANCELLED: {
//                setVisible(false);
                dispose();
                return;
            }
            case CONNECTION_TO_SERVER_FAILED: {
                dispose();
                return;
            }
            case BOTH_IN_CONVERSATION: {
                if (data[0].equals(user))
                    dispose();
                return;
            }
            case INCOMING_CALL: {
                showIncoming((BaseUser) data[0], (String) data[1]);
                return;
            }
            case OUT_CALL: {
                showOutcoming((BaseUser) data[0]);
                return;
            }
        }
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //delegate
        helpHandlerPredecessor.handleRequest(button, data);
    }

    private void onOk() {
        handleRequest(
                BUTTONS.CALL_ACCEPTED,
                new Object[]{
                        user,
                        dudes
                }
        );
        dispose();
    }

    private void onDeny() {
        handleRequest(
                BUTTONS.CALL_DENIED,
                new Object[]{
                        user,
                        dudes
                }
        );
        dispose();
    }

    private void onCancel() {
        handleRequest(
                BUTTONS.CALL_CANCELLED,
                new Object[]{
                        user,
                        dudes
                }
        );
        dispose();
    }

    /**
     * Call when you call some one
     * it make invisible unnecessarily gui elements
     * and show what you can see
     *
     * @param who you are calling
     */

    private void showOutcoming(BaseUser who) {
        user = who;
        dudes = "";

        setTitle("Calling");

        nameTo.setText(who.prettyString());

        nameTo.setVisible(true);
        buttonCancel.setVisible(true);

        middleLabel.setVisible(false);
        conversationInfo.setVisible(false);
        buttonOK.setVisible(false);
        denyButton.setVisible(false);

        setLocationRelativeTo(relativeTo);

        setVisible(true);
    }

    /**
     * Call when some one called you
     * Same purpose as showOutcoming()
     *
     * @param fromWho          who calls you
     * @param conversationInfo and conversation with him
     */

    private void showIncoming(BaseUser fromWho, String conversationInfo) {
        if (isShowing())
            setVisible(false);
        user = fromWho;
        dudes = conversationInfo;

        setTitle("Incoming");

        nameTo.setText(fromWho.prettyString());

        if (conversationInfo.length() == 0) {
            this.conversationInfo.setVisible(false);
            middleLabel.setVisible(false);
        } else {
            this.conversationInfo.setText(conversationInfo);
            this.conversationInfo.setVisible(true);
            middleLabel.setVisible(true);
        }


        nameTo.setVisible(true);
        buttonOK.setVisible(true);
        denyButton.setVisible(true);

        buttonCancel.setVisible(false);

        setLocationRelativeTo(relativeTo);

        setVisible(true);
    }

}
