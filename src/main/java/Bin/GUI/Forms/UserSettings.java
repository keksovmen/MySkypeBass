package Bin.GUI.Forms;

import javax.sound.sampled.FloatControl;
import javax.swing.*;

/**
 * Handle volume level of particular user
 * Contain his name and id too
 */

class UserSettings {
    private JSlider volumeLevel;
    private JLabel name;
    private JPanel mainPane;

    UserSettings(String name, FloatControl control) {
        this.name.setText(name);

        /*Checks if speaker is able to give you a volume setting*/
        if (control != null) {
            volumeLevel.setMaximum((int) control.getMaximum());
            volumeLevel.setMinimum((int) control.getMinimum());
            volumeLevel.setPaintTicks(true);

            volumeLevel.addChangeListener(e -> control.setValue(volumeLevel.getValue()));
        }

    }

    JPanel getMainPane() {
        return mainPane;
    }
}
