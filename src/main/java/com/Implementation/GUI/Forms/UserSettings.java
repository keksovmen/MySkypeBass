package com.Implementation.GUI.Forms;

import com.Abstraction.Networking.Utility.Users.User;

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

    UserSettings(User user, BiConsumer<Integer, Integer> changeVolume) {
        this.name.setText(user.toString());

        volumeLevel.addChangeListener(e -> changeVolume.accept(user.getId(), volumeLevel.getValue()));
    }

    JPanel getMainPane() {
        return mainPane;
    }
}
