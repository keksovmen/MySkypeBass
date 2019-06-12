package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.ConferencePaneActions;
import Bin.Networking.Utility.ErrorHandler;

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

    /**
     * Contains baseUser.toString() - UserSettings
     * need for add and removal of users
     */

    private Map<String, UserSettings> conferenceMembers;

    /**
     * All possible actions
     */

    private ConferencePaneActions actions;


    /**
     * Default constructor init
     * 1 - actions
     * 2 - spinner model
     * 3 - register actions
     *
     * @param actions all possible actions
     */

    ConferencePane(ConferencePaneActions actions) {
        this.actions = actions;
        volume.setModel(new SpinnerNumberModel(1d, 1d, 10d, 0.05d));

        conferenceMembers = new HashMap<>();

        endCallButton.addActionListener(e -> {
            try {
                actions.endCall().run();
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        muteButton.addActionListener(e -> {
            try {
                reactToMute(actions.mute().get());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        volume.addChangeListener(e -> {
            try {
                actions.changeMultiplier().accept((Double) volume.getValue());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });
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
     * @param soundControl his sound volume controller
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

    @Override
    public void errorCase() {
        clear();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }

}
