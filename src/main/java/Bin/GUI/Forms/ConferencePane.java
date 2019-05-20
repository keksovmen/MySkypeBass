package Bin.GUI.Forms;

import Bin.GUI.Main;

import javax.swing.*;

public class ConferencePane {
    private JSpinner volume;
    private JButton muteButton;
    private JPanel centerPane;
    private JButton endCallButton;
    private JPanel mainPane;

    ConferencePane() {
        volume.addChangeListener(e -> Main.getInstance().getAudioCapture().change((Integer) volume.getModel().getValue()));

        muteButton.addActionListener(e -> {
            Main.getInstance().getAudioCapture().mute();
            if (muteButton.getText().equals("Mute"))
                muteButton.setText("Un Mute");
            else muteButton.setText("Mute");
        });

        endCallButton.addActionListener(e -> Main.getInstance().endCall());
    }

    JPanel getCenterPane() {
        return centerPane;
    }

    JPanel getMainPane() {
        return mainPane;
    }
}
