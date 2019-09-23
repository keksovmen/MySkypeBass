package Com.GUI.Forms;

import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.ResponsibleGUI;

import javax.swing.*;

/**
 * Handle calls
 * Gives you an opportunity to cancel, deny and approve calls
 */

public class CallDialog extends JDialog implements ResponsibleGUI {
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

        buttonOK.addActionListener(e -> {
            whereToReportActions.act(
                    BUTTONS.CALL_ACCEPTED,
                    user,
                    dudes,
                    -1
            );
            dispose();
        });

        denyButton.addActionListener(e -> {
            whereToReportActions.act(
                    BUTTONS.CALL_DENIED,
                    user,
                    null,
                    -1
            );
            dispose();
        });

        buttonCancel.addActionListener(e -> {
            whereToReportActions.act(
                    BUTTONS.CALL_CANCELLED,
                    user,
                    null,
                    -1
            );
            dispose();
        });

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

    @Override
    public void respond(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        switch (action){
            case CALL_DENIED:{
                dispose();
                return;
            }
            case CALL_ACCEPTED:{
                dispose();
                return;
            }
            case CALL_CANCELLED:{
                dispose();
                return;
            }
            case CONNECTION_TO_SERVER_FAILED:{
                dispose();
                return;
            }
        }
    }

    /**
     * Call when you call some one
     * it make invisible unnecessarily gui elements
     * and show what you can see
     *
     * @param who you are calling
     */

    public void showOutcoming(BaseUser who, JComponent relativeTo) {
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
