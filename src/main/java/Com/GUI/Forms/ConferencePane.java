package Com.GUI.Forms;

import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.WHO;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.ActionsHandler;
import Com.Pipeline.BUTTONS;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handle adding new users or removing
 * Can change from here bass boost magnitude
 * or mute yourself
 */

class ConferencePane implements ActionsHandler {
    private JSpinner volume;
    private JButton muteButton;
    private JPanel centerPane;
    private JButton endCallButton;
    private JPanel mainPane;
    private JTextArea displayPlace;
    private JTextField sender;

//    /**
//     * Contains baseUser.toString() - UserSettings
//     * need for add and removal of users
//     */

    private final Map<BaseUser, UserSettings> conferenceMembers;

//    /**
//     * All possible actions
//     */

//    private final ConferencePaneActions actions;

    private final BiConsumer<Integer, Integer> changeVolume;


    /**
     * Default constructor init
     * 1 - actions
     * 2 - spinner model
     * 3 - register actions
     * <p>
     * //     * @param actions all possible actions
     */

    ConferencePane(ActionableLogic whereToReportActions) {
//        this.actions = actions;
        conferenceMembers = new HashMap<>();

        volume.setModel(new SpinnerNumberModel(1d, 1d, 20d, 0.05d));
        changeVolume = createVolumeChanger(whereToReportActions);
//        conferenceMembers = new HashMap<>();

//        endCallButton.addActionListener(e -> actions.endCall().run());

//        muteButton.addActionListener(e -> reactToMute(actions.mute().get()));

//        volume.addChangeListener(e -> actions.changeMultiplier().accept((Double) volume.getValue()));

        sender.addActionListener(e -> {
            String message = getMessage();
            whereToReportActions.act(
                    BUTTONS.SEND_MESSAGE,
                    null,
                    message,
                    WHO.CONFERENCE.getCode()
            );
            sendMessage(message);
        });

    }

    @Override
    public void respond(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        switch (action) {
            case CALL_ACCEPTED: {
                onCallAccepted(from, stringData);
                return;
            }
            case INCOMING_MESSAGE: {
                if (intData == 1)
                    onMessage(from, stringData);
                return;
            }
        }
    }

    private void onCallAccepted(BaseUser from, String dudes) {
        addUser(from);
        for (BaseUser user : BaseUser.parseUsers(dudes)) {
            addUser(user);
        }
    }

    private void onMessage(BaseUser from, String message) {
        showMessage(from, message);
    }

    private BiConsumer<Integer, Integer> createVolumeChanger(ActionableLogic whereToReportActions) {
        return (id, value) -> whereToReportActions.act(
                BUTTONS.VOLUME_CHANGED,
                null,
                String.valueOf(id),
                value
        );
    }

    JPanel getMainPane() {
        return mainPane;
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
//        UserSettings remove = conferenceMembers.remove(name);
//        if (remove != null) {
//            centerPane.remove(remove.getMainPane());
//            centerPane.revalidate();
//            centerPane.repaint();
//        }
    }

    /**
     * Remove everything that possible
     */

    void clear() {
//        centerPane.removeAll();
//        conferenceMembers.clear();
//        muteButton.setText("Mute");
//        displayPlace.setText("");
//        sender.setText("");
//        volume.setValue(1d);
    }

    /**
     * Define what text will be on mute button
     *
     * @param resultOfMute action that provides boolean actual state value
     */

    private void reactToMute(boolean resultOfMute) {
//        if (resultOfMute) {
//            muteButton.setText("Un mute");
//        } else {
//            muteButton.setText("Mute");
//        }
    }

    private void showMessage(String from, String message) {
        displayPlace.append(from + " (" + MessagePane.getTime() + "): " + message + "\n");
    }

    private void showMessage(BaseUser from, String message) {
        showMessage(from.toString(), message);
    }

    private void sendMessage(String message) {
        if (message.length() == 0) {
            return;
        }
        showMessage("Me", message);

    }

    private String getMessage() {
        String text = sender.getText();
        sender.setText("");
        return text;
    }

//    boolean containPerson(String user) {
//        return conferenceMembers.containsKey(user);
//    }


}
