package Bin.GUI.Forms;

import Bin.GUI.Interfaces.CallDialogActions;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Handle calls
 * Gives you an opportunity to cancel, deny and approve calls
 */

class CallDialog extends JDialog implements ErrorHandler {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel nameTo;
    private JButton denyButton;
    private JLabel conversationInfo;

    /**
     * Corresponding actions
     */

    private final CallDialogActions actions;

    /**
     * To display in center of it
     */

    private final JComponent relativeTo;

    /**
     * Single constructor
     * Init and update actions
     * register listeners
     * And don't show the dialog for this purpose here is a method
     *
     * @param actions which you have
     * @param relativeTo to center showing
     */

    CallDialog(CallDialogActions actions, JComponent relativeTo) {
        this.actions = actions;
        updateActions();

        this.relativeTo = relativeTo;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK(actions.acceptCall()));

        buttonCancel.addActionListener(e -> onCancel(actions.cancelCall()));

        denyButton.addActionListener(e -> onDeny(actions.denyCall()));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

    /**
     * Simply adds dispose of this dialog action
     */

    private void updateActions() {
        actions.updateAcceptCall(onAcceptCall(actions.acceptCall()));
        actions.updateCancelCall(onSomethingCall(actions.cancelCall()));
        actions.updateDenyCall(onSomethingCall(actions.denyCall()));
    }

    private Consumer<BaseUser[]> onAcceptCall(Consumer<BaseUser[]> onAcceptCall){
        return users -> {
            onAcceptCall.accept(users);
            dispose();
        };
    }

    private Consumer<BaseUser> onSomethingCall(Consumer<BaseUser> onSomethingCall){
        return users -> {
            onSomethingCall.accept(users);
            dispose();
        };
    }

    /**
     * When you accept an incoming call
     * firstly see how supposed to look like BaseUser[] array in Main
     * Just get users from 2 labels
     * and put them in 1 array
     * first in it is who called than others
     *
     * @param acceptCall action to call when pressed the button
     */

    private void onOK(Consumer<BaseUser[]> acceptCall) {
        BaseUser whoCall = BaseUser.parse(nameTo.getText());
        BaseUser[] convUsers = BaseUser.parseUsers(conversationInfo.getText());
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

    private void onDeny(Consumer<BaseUser> denyCall) {
        denyCall.accept(BaseUser.parse(nameTo.getText()));
        dispose();
    }

    /**
     * Call when you call some one
     * it make invisible unnecessarily gui elements
     * and show what you can see
     * @param who you are calling
     */

    void showOutcoming(String who) {
        setTitle("Calling");

        nameTo.setText(who);

        nameTo.setVisible(true);
        conversationInfo.setVisible(false);
        buttonOK.setVisible(false);
        denyButton.setVisible(false);
        buttonCancel.setVisible(true);

        setLocationRelativeTo(relativeTo);

        setVisible(true);
    }

    /**
     * Call when some one called you
     * Same purpose as showOutcoming()
     *
     * @param fromWho who calls you
     * @param conversationInfo and conversation with him
     */

    void showIncoming(String fromWho, String conversationInfo) {
        setTitle("Incoming");

        nameTo.setText(fromWho);
        this.conversationInfo.setText(conversationInfo);

        nameTo.setVisible(true);
        this.conversationInfo.setVisible(true);
        buttonOK.setVisible(true);
        denyButton.setVisible(true);
        buttonCancel.setVisible(false);

        setLocationRelativeTo(relativeTo);

        setVisible(true);
    }

    @Override
    public void errorCase() {
        dispose();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }
}
