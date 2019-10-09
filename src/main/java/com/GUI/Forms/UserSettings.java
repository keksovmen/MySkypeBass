package com.GUI.Forms;

import com.Networking.Utility.BaseUser;

import javax.swing.*;
import java.util.function.BiConsumer;

/**
 * Handle volume level of particular user
 * Contain his name and id too
 */

class UserSettings {
    private JSlider volumeLevel;
    private JLabel name;
    private JPanel mainPane;

    UserSettings(BaseUser user, BiConsumer<Integer, Integer> changeVolume) {
        this.name.setText(user.toString());

        volumeLevel.addChangeListener(e -> changeVolume.accept(user.getId(), volumeLevel.getValue()));
    }

    JPanel getMainPane() {
        return mainPane;
    }
}
