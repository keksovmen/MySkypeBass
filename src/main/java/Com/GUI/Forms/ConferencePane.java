package Com.GUI.Forms;

import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.WHO;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.ActionsHandler;
import Com.Pipeline.BUTTONS;
import Com.Util.FormatWorker;
import Com.Util.History.History;
import Com.Util.History.HistoryFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handle adding new users or removing
 * Can change from here bass boost magnitude
 * or mute yourself
 */

class ConferencePane implements ActionsHandler {
    private JSpinner bassChanger;
    private JButton muteButton;
    private JPanel settingsPane;
    private JButton endCallButton;
    private JPanel mainPane;
    private JTextArea messagesDisplay;
    private JTextField messageGetter;

    /**
     * Contains baseUser - UserSettings
     * need for add and removal of users
     */

    private final Map<BaseUser, UserSettings> conferenceMembers;

    /**
     * Function to call when need to change volume lvl of a particular dude
     */

    private final BiConsumer<Integer, Integer> changeVolume;

    private final History<String> history;

    /**
     * Default constructor init
     * 1 - actions
     * 2 - spinner model
     * 3 - register actions
     * <p>
     * //     * @param actions all possible actions
     */

    ConferencePane(ActionableLogic whereToReportActions, GUIDuty closeAction) {
        conferenceMembers = new HashMap<>();

        history = HistoryFactory.getStringHistory();

        bassChanger.setModel(new SpinnerNumberModel(1, 1, 100, 1));

        changeVolume = createVolumeChanger(whereToReportActions);

        endCallButton.addActionListener(e -> disconnectAction(whereToReportActions, closeAction));

        muteButton.addActionListener(e -> mute(whereToReportActions));

        bassChanger.addChangeListener(e -> changeBussBoost(whereToReportActions));

        messageGetter.addActionListener(e -> sendMessageAction(whereToReportActions));

        messageGetter.registerKeyboardAction(e -> onUp(),
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                JComponent.WHEN_FOCUSED);
        messageGetter.registerKeyboardAction(e -> onDown(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_FOCUSED);
    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        switch (action) {
            case INCOMING_MESSAGE: {
                if (intData == 1) // check if it suppose to go in conversation chat
                    onMessage(from, stringData);
                return;
            }
            case EXITED_CONVERSATION: {
                clear();
                return;
            }
            case REMOVE_DUDE_FROM_CONVERSATION: {
                removeUser(from);
                return;
            }
            case ADD_DUDE_TO_CONVERSATION: {
                addUser(from);
                return;
            }
            case DISCONNECTED: {
                clear();
                return;
            }
            case CONNECTION_TO_SERVER_FAILED: {
                clear();
                return;
            }
        }
    }

    JPanel getMainPane() {
        return mainPane;
    }

    private void onMessage(BaseUser from, String message) {
        showMessage(from, message);
    }

    private void sendMessageAction(ActionableLogic whereToReportActions) {
        String message = getMessage();
        if (!sendMessage(message))
            return;
        whereToReportActions.act(
                BUTTONS.SEND_MESSAGE,
                null,
                message,
                WHO.CONFERENCE.getCode()
        );
    }

    private void disconnectAction(ActionableLogic whereToReportActions, GUIDuty guiDuty) {
        whereToReportActions.act(
                BUTTONS.EXIT_CONFERENCE,
                null,
                null,
                -1
        );
        guiDuty.displayChanges(
                GUIActions.CLOSE_MESSAGE_PANE,
                MultiplePurposePane.CONVERSATION_TAB_NAME
        );
        //Do some clearing here
        clear();
    }

    private BiConsumer<Integer, Integer> createVolumeChanger(ActionableLogic whereToReportActions) {
        return (id, value) -> whereToReportActions.act(
                BUTTONS.VOLUME_CHANGED,
                null,
                String.valueOf(id),
                value
        );
    }

    /**
     * Adds new user in the conversation
     * each time creates new UserSettings if there is no cashed one
     * put him on gui but through GridBagConstraints because
     * they don't want to go beneath each other but from left to right
     */

    private void addUser(BaseUser dude) {
        if (conferenceMembers.containsKey(dude)) {//should't happen
            return;
        }

        UserSettings userSettings = new UserSettings(dude, changeVolume);
        conferenceMembers.put(dude, userSettings);

        settingsPane.add(userSettings.getMainPane());
        repaint();
    }

    /**
     * Removes a user
     * checks if so was existed at all
     *
     * @param dude user to remove
     */

    private void removeUser(BaseUser dude) {
        UserSettings remove = conferenceMembers.remove(dude);
        if (remove != null) {
            settingsPane.remove(remove.getMainPane());
            repaint();
        }
    }

    /**
     * Define what text will be on mute button
     */

    private void mute(ActionableLogic whereToRegisterActions) {
        whereToRegisterActions.act(
                BUTTONS.MUTE,
                null,
                null,
                -1
        );
        if (muteButton.getText().equals("Mute")) {
            muteButton.setText("Un Mute");
        } else {
            muteButton.setText("Mute");
        }
    }

    private void changeBussBoost(ActionableLogic whereToRegisterActions) {
        whereToRegisterActions.act(
                BUTTONS.INCREASE_BASS,
                null,
                null,
                (Integer) bassChanger.getValue()
        );
    }

    private void showMessage(String from, String message) {
        messagesDisplay.append(from + " (" + FormatWorker.getTime() + "): " + message + "\n");
    }

    private void showMessage(BaseUser from, String message) {
        showMessage(from.toString(), message);
    }

    private boolean sendMessage(String message) {
        if (message.length() == 0) {
            return false;
        }
        history.push(message);
        showMessage("Me", message);
        return true;

    }

    private String getMessage() {
        String text = messageGetter.getText();
        messageGetter.setText("");
        return text;
    }

    /**
     * Remove everything that possible
     */

    private void clear() {
        settingsPane.removeAll();
        conferenceMembers.clear();
        muteButton.setText("Mute");
        messagesDisplay.setText("");
        messageGetter.setText("");
        bassChanger.setValue(1);
        repaint();
    }

    private void onUp() {
        messageGetter.setText(history.getNext());
    }

    private void onDown() {
        messageGetter.setText("");
    }

    private void repaint(){
        mainPane.revalidate();
        mainPane.repaint();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        settingsPane = new JPanel();
        settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.PAGE_AXIS));
    }

}
