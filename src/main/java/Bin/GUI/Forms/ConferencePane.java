package Bin.GUI.Forms;

import Bin.Main;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConferencePane {
    private JSpinner volume;
    private JButton muteButton;
    private JPanel centerPane;
    private JButton endCallButton;
    private JPanel mainPane;

    private Map<String, UserSettings> conferenceMembers;

    ConferencePane(Runnable endCall, Supplier<Boolean> mute) {
        conferenceMembers = new HashMap<>();

        endCallButton.addActionListener(e -> endCall.run());

        muteButton.addActionListener(e -> reactToMute(mute.get()));
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
