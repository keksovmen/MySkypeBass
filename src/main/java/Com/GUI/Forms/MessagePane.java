package Com.GUI.Forms;

import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.Networking.Utility.BaseUser;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.function.BiConsumer;

/**
 * Handle messaging part with some one
 * can sendSound message and close self tab
 */

class MessagePane {
    private JPanel mainPane;
    private JLabel nameWho;
    private JTextArea messageBoard;
    private JButton sendButton;
    private JButton closeButton;
    private JTextField textField;

    private boolean isShown;
//    /**
//     * Possible actions
//     */
//
//    private final ThirdSkinActions actions;

//    /**
//     * Id of the user who you write
//     */
//
//    private final int who;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    /**
     * Default constructor init
     * 1 - actions
     * 2 - who to talk
     * 3 - register handlers
     * <p>
     * //     * @param name    baseUser.toString() with who you talk
     * //     * @param actions all your actions
     */

    MessagePane(BaseUser forWho, BiConsumer<String, BaseUser> sendMessage, GUIDuty actions) {
        nameWho.setText(forWho.toString());

        sendButton.addActionListener(e -> this.sendMessage(sendMessage, forWho));

        textField.registerKeyboardAction(e -> sendButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        closeButton.addActionListener(e -> {
            actions.displayChanges(GUIActions.CLOSE_MESSAGE_PANE, forWho.toString());
            isShown = false;
        });

        isShown = true;
    }

    JPanel getMainPane() {
        return mainPane;
    }

    /**
     * Displays message when you write or receive it
     * Also include who write and time
     *
     * @param message to display
     * @param me      define who write it
     */

    void showMessage(String message, boolean me) {
        if (message.length() != 0)
            messageBoard.append((me ? "Me" : nameWho.getText()) + " (" + getTime() + "): " + message + "\n");
        isShown = true;
    }

    /**
     * Action for sending message
     * can't sendSound if there is empty string
     * also clear textField where was your message
     *
     * @param send function to call when need to sendSound
     */

    private void sendMessage(BiConsumer<String, BaseUser> send, BaseUser user) {
        String message = textField.getText();
        if (message.length() == 0) {
            return;
        }
        send.accept(message, user);
        showMessage(message, true);
        textField.setText("");
    }

    /**
     * Return simple string for time in messages
     *
     * @return time in format like 13:41
     */

    static String getTime() {
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    public boolean isShown() {
        return isShown;
    }

}
