package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.ConferencePaneActions;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConferencePane {
    private JSpinner volume;
    private JButton muteButton;
    private JPanel centerPane;
    private JButton endCallButton;
    private JPanel mainPane;

    private Map<String, UserSettings> conferenceMembers;

    private ConferencePaneActions actions;


    ConferencePane(ConferencePaneActions actions) {
        this.actions = actions;
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
    }

    JPanel getMainPane() {
        return mainPane;
    }

    void addUser(String name, FloatControl soundControl){
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
    }

    void removeUser(String name){
        UserSettings remove = conferenceMembers.remove(name);
        if (remove != null){
            centerPane.remove(remove.getMainPane());
            centerPane.revalidate();
            centerPane.repaint();
        }
    }

    void clear(){
        centerPane.removeAll();
        conferenceMembers.clear();
    }

    private void reactToMute(boolean resultOfMute){
        if (resultOfMute){
            muteButton.setText("Un mute");
        }else {
            muteButton.setText("Mute");
        }
    }
}
