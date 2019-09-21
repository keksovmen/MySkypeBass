package Com.GUI.Forms;

import Com.Networking.Utility.ErrorHandler;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle adding new users or removing
 * Can change from here bass boost magnitude
 * or mute yourself
 */

class ConferencePane implements ErrorHandler {
    private JSpinner volume;
    private JButton muteButton;
    private JPanel centerPane;
    private JButton endCallButton;
    private JPanel mainPane;
    private JTextArea displayPlace;
    private JTextField sender;

    /**
     * Contains baseUser.toString() - UserSettings
     * need for add and removal of users
     */

    private final Map<String, UserSettings> conferenceMembers;

//    /**
//     * All possible actions
//     */

//    private final ConferencePaneActions actions;


    /**
     * Default constructor init
     * 1 - actions
     * 2 - spinner model
     * 3 - register actions
     *
//     * @param actions all possible actions
     */

    ConferencePane() {
//        this.actions = actions;
        volume.setModel(new SpinnerNumberModel(1d, 1d, 20d, 0.05d));

        conferenceMembers = new HashMap<>();

//        endCallButton.addActionListener(e -> actions.endCall().run());

//        muteButton.addActionListener(e -> reactToMute(actions.mute().get()));

//        volume.addChangeListener(e -> actions.changeMultiplier().accept((Double) volume.getValue()));

        sender.addActionListener(e -> sendMessage(getMessage()));
    }

    JPanel getMainPane() {
        return mainPane;
    }

    /**
     * Adds new user in the conversation
     * each time creates new UserSettings if there is no cashed one
     * put him on gui but through GridBagConstraints because
     * they don't want to go beneath each other but from left to right
     *
     * @param name         of user to add
     * @param soundControl his sound volume controller might be null if speaker doesn't work
     */

    void addUser(String name, FloatControl soundControl) {
        if (conferenceMembers.containsKey(name)) {
            return;
        }
        UserSettings userSettings = new UserSettings(name, soundControl);
        conferenceMembers.put(name, userSettings);

        int size = conferenceMembers.size();
        centerPane.add(userSettings.getMainPane(), new GridBagConstraints(0, size, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        centerPane.revalidate();
        centerPane.repaint();
    }

    /**
     * Removes a user
     * checks if so was existed at all
     *
     * @param name user to remove
     */

    void removeUser(String name) {
        UserSettings remove = conferenceMembers.remove(name);
        if (remove != null) {
            centerPane.remove(remove.getMainPane());
            centerPane.revalidate();
            centerPane.repaint();
        }
    }

    /**
     * Remove everything that possible
     */

    void clear() {
        centerPane.removeAll();
        conferenceMembers.clear();
        muteButton.setText("Mute");
        displayPlace.setText("");
        sender.setText("");
        volume.setValue(1d);
    }

    /**
     * Define what text will be on mute button
     *
     * @param resultOfMute action that provides boolean actual state value
     */

    private void reactToMute(boolean resultOfMute) {
        if (resultOfMute) {
            muteButton.setText("Un mute");
        } else {
            muteButton.setText("Mute");
        }
    }

    void showMessage(String message, String from) {
        displayPlace.append(from + " (" + MessagePane.getTime() + "): " + message + "\n");
    }

    private void sendMessage(String message) {
        if (message.length() == 0) {
            return;
        }
        showMessage(message, "Me");
//        actions.sendMessageToConference().accept(message);
    }

    private String getMessage() {
        String text = sender.getText();
        sender.setText("");
        return text;
    }

    boolean containPerson(String user) {
        return conferenceMembers.containsKey(user);
    }

    @Override
    public void errorCase() {
        clear();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }

}
