package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.ModelObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.History.History;
import com.Abstraction.Util.History.HistoryFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.Abstraction.Util.Logging.LoggerUtils.clientLogger;

/**
 * Handle adding new users or removing
 * Can change from here bass boost magnitude
 * or mute yourself
 */

class ConferencePane implements ModelObserver, LogicObserver, ButtonsHandler {
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

    private final Map<User, UserSettings> conferenceMembers;

    /**
     * Function to call when need to change volume lvl of a particular dude
     */

    private final BiConsumer<Integer, Integer> changeVolume;

    private final History<String> history;

    private final ButtonsHandler helpHandlerPredecessor;

    /**
     * Default constructor init
     * 1 - actions
     * 2 - spinner model
     * 3 - register actions
     * <p>
     * //     * @param actions all possible actions
     */

    ConferencePane(ButtonsHandler helpHandlerPredecessor, Consumer<String> closeTabAction) {
        conferenceMembers = new HashMap<>();

        $$$setupUI$$$();
        history = HistoryFactory.getStringHistory();

        this.helpHandlerPredecessor = helpHandlerPredecessor;

        bassChanger.setModel(new SpinnerNumberModel(0, 0, 100, 1));

        changeVolume = createVolumeChanger();

        endCallButton.addActionListener(e -> disconnectAction(closeTabAction));

        muteButton.addActionListener(e -> mute());

        bassChanger.addChangeListener(e -> changeBussBoost());

        messageGetter.addActionListener(e -> sendMessageAction());

        messageGetter.registerKeyboardAction(e -> onUp(),
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                JComponent.WHEN_FOCUSED);
        messageGetter.registerKeyboardAction(e -> onDown(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_FOCUSED);

        MultiplePurposePane.registerPopUp(messagesDisplay, messageGetter, this);
        MultiplePurposePane.registerPopUp(messageGetter, messageGetter, this);
    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        switch (action) {
            case INCOMING_MESSAGE: {
                if ((int) data[2] == 1) // check if it suppose to go in conversation chat
                    onMessage((User) data[0], (String) data[1]);
                return;
            }
            case EXITED_CONVERSATION: {
                clear();
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

    @Override
    public void modelObservation(UnEditableModel model) {
        clientLogger.entering(this.getClass().getName(), "update");
        Set<User> conversation = model.getConversation();

        Map<User, UserSettings> tmp = new HashMap<>();
        clientLogger.logp(Level.FINER, this.getClass().getName(), "update",
                "Changing amount of users in conversation from - " +
                        Arrays.toString(conferenceMembers.keySet().toArray())
                + ", to - " + Arrays.toString(conversation.toArray())
        );
        conferenceMembers.forEach((user, userSettings) -> {
            if (!conversation.contains(user))
                tmp.put(user, userSettings);
        });

        tmp.keySet().forEach(this::removeUser);
        tmp.clear();

        conversation.forEach(user -> {
            if (!conferenceMembers.containsKey(user))
                addUser(user);
        });

        clientLogger.logp(Level.FINER, this.getClass().getName(), "update",
                "Changed dudes result is - " + Arrays.toString(conferenceMembers.keySet().toArray()));
        repaint();
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //delegate
        helpHandlerPredecessor.handleRequest(button, data);
    }

    JPanel getMainPane() {
        return mainPane;
    }

    private void onMessage(User from, String message) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "onMessage",
                "Incoming message in to conversation, from - " + from);
        showMessage(from, message);
    }

    private void sendMessageAction() {
        String message = getMessage();
        if (!sendMessage(message))
            return;
        handleRequest(
                BUTTONS.SEND_MESSAGE,
                new Object[]{
                        message,
                        WHO.CONFERENCE.getCode()
                }
        );
    }

    private void disconnectAction(Consumer<String> closeTabAction) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "disconnectAction",
                "Pressed disconnect from conversation button");
        handleRequest(
                BUTTONS.EXIT_CONFERENCE,
                null
        );
        closeTabAction.accept(MultiplePurposePane.CONVERSATION_TAB_NAME);
        //Do some clearing here
        clear();
    }

    private BiConsumer<Integer, Integer> createVolumeChanger() {
        return (id, value) -> handleRequest(
                BUTTONS.VOLUME_CHANGED,
                new Object[]{
                        id,
                        value
                }
        );
    }

    /**
     * Adds new user in the conversation
     * each time creates new UserSettings if there is no cashed one
     * put him on gui but through GridBagConstraints because
     * they don't want to go beneath each other but from left to right
     */

    private void addUser(User dude) {
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

    private void removeUser(User dude) {
        UserSettings remove = conferenceMembers.remove(dude);
        if (remove != null) {
            settingsPane.remove(remove.getMainPane());
            repaint();
        }
    }

    /**
     * Define what text will be on mute button
     */

    private void mute() {
        handleRequest(
                BUTTONS.MUTE,
                null
        );
        if (muteButton.getText().equals("Mute")) {
            muteButton.setText("Un Mute");
        } else {
            muteButton.setText("Mute");
        }
    }

    private void changeBussBoost() {
        handleRequest(
                BUTTONS.INCREASE_BASS,
                new Object[]{
                        bassChanger.getValue()
                }
        );
    }

    private void showMessage(String from, String message) {
        messagesDisplay.append(from + " (" + FormatWorker.getTime() + "): " + message + "\n");
    }

    private void showMessage(User from, String message) {
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
        clientLogger.logp(Level.FINER, this.getClass().getName(), "clear",
                "Cleared all settings panes, set fields to default");
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

    private void repaint() {
        mainPane.revalidate();
        mainPane.repaint();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        settingsPane = new JPanel();
        settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.PAGE_AXIS));
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        mainPane.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(settingsPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        bassChanger = new JSpinner();
        panel1.add(bassChanger, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        muteButton = new JButton();
        muteButton.setText("Mute");
        panel1.add(muteButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPane.add(panel2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        endCallButton = new JButton();
        endCallButton.setText("Disconnect from call");
        panel2.add(endCallButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPane.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setHorizontalScrollBarPolicy(31);
        panel3.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        messagesDisplay = new JTextArea();
        messagesDisplay.setEditable(false);
        messagesDisplay.setLineWrap(true);
        messagesDisplay.setRows(6);
        messagesDisplay.setWrapStyleWord(true);
        scrollPane2.setViewportView(messagesDisplay);
        messageGetter = new JTextField();
        messageGetter.setToolTipText("Enter to send a message");
        panel3.add(messageGetter, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
