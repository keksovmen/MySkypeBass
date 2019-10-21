package com.GUI.Forms;

import com.Networking.Utility.BaseUser;
import com.Pipeline.ACTIONS;
import com.Pipeline.ActionableLogic;
import com.Pipeline.ActionsHandler;
import com.Pipeline.BUTTONS;

import javax.swing.*;

import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.clientLogger;

/**
 * Handle calls
 * Gives you an opportunity to cancel, deny and approve calls
 */

public class CallDialog extends JDialog implements ActionsHandler {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel nameTo;
    private JButton denyButton;
    private JLabel conversationInfo;
    private JLabel middleLabel;

    private BaseUser user;
    private String dudes;

    /**
     * Single constructor
     * Init and update actions
     * register listeners
     * And don't show the dialog for this purpose here is a method
     */

    public CallDialog(ActionableLogic whereToReportActions) {

        buttonOK.addActionListener(e -> onOk(whereToReportActions));

        denyButton.addActionListener(e -> onDeny(whereToReportActions));

        buttonCancel.addActionListener(e -> onCancel(whereToReportActions));

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
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
                dispose();
                return;
            }
            case CONNECTION_TO_SERVER_FAILED: {
                dispose();
                return;
            }
            case BOTH_IN_CONVERSATION: {
                if (from.equals(user))
                    dispose();
                return;
            }
        }
    }

    private void onOk(ActionableLogic whereToReportActions) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onOk",
                "Pressed accept call button with - " + user + ", dudes - " + dudes);
        whereToReportActions.act(
                BUTTONS.CALL_ACCEPTED,
                user,
                dudes,
                -1
        );
        dispose();
    }

    private void onDeny(ActionableLogic whereToReportActions) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onDeny",
                "Pressed deny call button with - " + user + ", dudes - " + dudes);
        whereToReportActions.act(
                BUTTONS.CALL_DENIED,
                user,
                dudes,
                -1
        );
        dispose();
    }

    private void onCancel(ActionableLogic whereToReportActions) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onCancel",
                "Pressed cancel call button with - " + user + ", dudes - " + dudes);
        whereToReportActions.act(
                BUTTONS.CALL_CANCELLED,
                user,
                dudes,
                -1
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

    public void showOutcoming(BaseUser who, JComponent relativeTo) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onOutCall",
                "Show call dialog box for out call to - " + who);
        user = who;
        dudes = "";

        setTitle("Calling");

        nameTo.setText(who.toString());

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

    public void showIncoming(BaseUser fromWho, String conversationInfo, JComponent relativeTo) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onOutCall",
                "Show call dialog box for input call акщь - " + fromWho +
                ", conversation - " + conversationInfo);
        user = fromWho;
        dudes = conversationInfo;

        setTitle("Incoming");

        nameTo.setText(fromWho.toString());

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
