package Com.GUI.Forms;

import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.Model.UnEditableModel;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.UpdaterAndHandler;
import Com.Util.Resources;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handles messaging, call pane
 * Can call and disconnect from here
 * Has pop up menu in usersList
 */

public class MultiplePurposePane implements UpdaterAndHandler, GUIDuty {

    /**
     * Name for conversation tab uses in search cases
     * so decided to make it as a variable
     */

    private static final String CONVERSATION_TAB_NAME = "Conversation";

    private JLabel labelMe;

    /**
     * Has pop up menu
     */

    private JList<BaseUser> usersList;
    private JButton callButton;
    private JButton disconnectButton;
    private JTabbedPane callTable;
    private JPanel mainPane;

    /**
     * Contain baseUser[] that on server
     */

    private DefaultListModel<BaseUser> model;

    /**
     * Need for messaging isCashed entries name - pane
     */

    private final Map<BaseUser, MessagePane> tabs;

    private final ConferencePane conferencePane;

    private final BiConsumer<String, BaseUser> sendAction;

    /**
     * Default constructor
     * Init firstly
     * 1 - update actions
     * 2 - set my name and id
     * 3 - register buttons action
     * 4 - create pop up menu for userList
     */

    public MultiplePurposePane(ActionableLogic whereToReportActions) {
        tabs = new HashMap<>();
        conferencePane = new ConferencePane(whereToReportActions);

        sendAction = ((s, user) -> whereToReportActions.act(
                BUTTONS.SEND_MESSAGE,
                null,
                s,
                user.getId()));

        callTable.addChangeListener(e -> deColored(callTable.getSelectedIndex()));

        disconnectButton.addActionListener(e -> disconnect(whereToReportActions));

        callButton.addActionListener(e -> call(whereToReportActions));
//
        registerPopUp(whereToReportActions);

    }

    @Override
    public void update(UnEditableModel model) {
        this.model.clear();
        model.getUserMap().values().forEach(
                baseUser -> this.model.addElement(baseUser));
        //Go through tabs and set online or offline icons, except CONFERENCE
        changeIconsOfTabs();

        mainPane.revalidate();
        mainPane.repaint();
    }

    @Override
    public void respond(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        switch (action) {
            case CONNECT_SUCCEEDED: {
                labelMe.setText(stringData);
                break;
            }
            case INCOMING_MESSAGE: {
                showMessage(from, stringData, intData == 1);
                break;
            }
            case DISCONNECTED: {
                onDisconnect();
                break;
            }
            case CALLED_BUT_BUSY: {
                onCalledButBusy(from);
                break;
            }
            case CALL_ACCEPTED: {
                onCallAccepted();
                break;
            }
            case BOTH_IN_CONVERSATION: {
                onBothInConv(from);
                break;
            }
        }
        conferencePane.respond(action, from, stringData, bytesData, intData);
    }

    @Override
    public void displayChanges(GUIActions action, Object data) {
        if (action.equals(GUIActions.CLOSE_MESSAGE_PANE)) {
            String tabName = (String) data;
            closeTab(tabName);
        }
    }

    /* Actions handlers */

    private void onCallAccepted() {
        showConversationPane();
    }

    private void onCalledButBusy(BaseUser who) {
        showMessage(who, "I called, but you had been calling already, call me back", false);
    }

    private void onBothInConv(BaseUser from) {
        showMessage(from, "I called, but You and I are in different conversations, call me later", false);
    }

    /* Actions */

    private void call(ActionableLogic actionableLogic) {
        if (!selected())
            return;
        actionableLogic.act(
                BUTTONS.CALL,
                getSelected(),
                null,
                -1
        );
    }

    private void disconnect(ActionableLogic actionableLogic) {
        actionableLogic.act(
                BUTTONS.DISCONNECT,
                null,
                null,
                -1
        );
    }

    public JPanel getPane() {
        return mainPane;
    }

    /**
     * Creates and register pop up menu on usersList
     * Consist of
     * Send message - when user is selected open MessagePane on callTable
     * Refresh - ask for users on the server
     */

    private void registerPopUp(ActionableLogic registration) {
        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessageMenu = new JMenuItem("Send Message");
        sendMessageMenu.addActionListener(e -> {
            if (!selected())
                return;
            BaseUser selected = getSelected();
//            String s = selected.toString();
            if (isShownAlready(selected))
                return;
            if (isCashed(selected)) {
                callTable.addTab(
                        selected.toString(),
                        Resources.onlineIcon,
                        tabs.get(selected).getMainPane()
                );
            } else {
                callTable.addTab(
                        selected.toString(),
                        Resources.onlineIcon,
                        createPane(selected).getMainPane());
            }
        });

        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e ->
                registration.act(
                        BUTTONS.ASC_FOR_USERS,
                        null,
                        null,
                        -1
                ));
        popupMenu.add(sendMessageMenu);
        popupMenu.add(refresh);

        usersList.setComponentPopupMenu(popupMenu);
    }

    private void changeIconsOfTabs() {
        tabs.forEach((user, messagePane) -> {
            if (messagePane.isShown()) {
                if (!model.contains(user)) {
                    callTable.setIconAt(
                            callTable.indexOfTab(user.toString()),
                            Resources.offlineIcon
                    );
                }
            }
        });
    }

    /**
     * Display all users
     * there can be no users mean server is empty
     *
     * @param users income users
     */

    void displayUsers(BaseUser[] users) {
        model.removeAllElements();
        for (BaseUser user : users) {
            model.addElement(user);
        }
    }

    private boolean selected() {
        return usersList.getSelectedIndex() != -1;
    }

    private BaseUser getSelected() {
        return model.get(usersList.getSelectedIndex());
    }

    private boolean isShownAlready(String data) {
        return callTable.indexOfTab(data) != -1;
    }

    private boolean isShownAlready(BaseUser user) {
        return isShownAlready(user.toString());
    }

    private boolean isCashed(BaseUser user) {
        return tabs.containsKey(user);
    }

//    private boolean isConversationPaneShown(){
//        return callTable.indexOfTab(CONVERSATION_TAB_NAME) != -1;
//    }

    private void showConversationPane() {
        callTable.addTab(
                CONVERSATION_TAB_NAME,
                Resources.conversationIcon,
                conferencePane.getMainPane());
        callTable.revalidate();
        callTable.repaint();
    }

    /**
     * Creates MessagePane object which has JPane
     *
     * @return ready to use MessagePane
     */

    private MessagePane createPane(BaseUser user) {
        MessagePane messagePane = new MessagePane(user, sendAction, this);
        tabs.put(user, messagePane);
        return messagePane;
    }

    /**
     * Display a user message
     * if there is no MessagePane creates
     * otherwise uses cashed
     * or already displayed one
     *
     * @param from    message sent
     * @param message plain text
     */

    public void showMessage(final BaseUser from, final String message, boolean toConv) {
        if (from == null) // do nothing when dude is not present in model
            return;
        String tabName;
        if (toConv) {
            tabName = CONVERSATION_TAB_NAME;
        } else {
            if (isShownAlready(from)) {
                tabs.get(from).showMessage(message, false);
            } else {
                if (isCashed(from)) {
                    MessagePane messagePane = tabs.get(from);
                    callTable.addTab(from.toString(), Resources.onlineIcon, messagePane.getMainPane());
                    messagePane.showMessage(message, false);
                } else {
                    MessagePane pane = createPane(from);
                    callTable.addTab(from.toString(), Resources.onlineIcon, pane.getMainPane());
                    pane.showMessage(message, false);
                }
            }
            tabName = from.toString();
        }
        colorForMessage(callTable.indexOfTab(tabName));
    }


    private void closeTab(String name) {
        int indexOfTab = callTable.indexOfTab(name);
        if (indexOfTab == -1)
            return;
        callTable.removeTabAt(indexOfTab);
    }

    /**
     * Paint a tab which has received a message and not in focus
     * it red
     *
     * @param indexOfTab wich to paint
     */

    private void colorForMessage(final int indexOfTab) {
        if (callTable.getSelectedIndex() == indexOfTab) {
            return;
        }
        callTable.setBackgroundAt(indexOfTab, Color.RED);
    }

    /**
     * Remove color from the tab when focused
     *
     * @param indexOfTab focused tab
     */

    private void deColored(final int indexOfTab) {
        if (indexOfTab == -1 || !callTable.getBackgroundAt(indexOfTab).equals(Color.RED)) return;
        callTable.setBackgroundAt(indexOfTab, null);
    }

    private void onDisconnect() {
        removeAllTabs();
        tabs.clear();
    }

//    /**
//     * Creates if necessary conferencePane
//     * and add user who called you in
//     *
//     * @param user    who calls you
//     * @param control volume for him
//     */

//    void conversationStart(String user, FloatControl control) {
//        if (conferencePane == null) {
////            conferencePane = new ConferencePane();
//        }
//        conferencePane.addUser(user, control);
//        callTable.addTab(CONVERSATION_TAB_NAME, conferencePane.getMainPane());
//    }

//    /**
//     * Add some one on conferencePane
//     *
//     * @param name    who to add
//     * @param control sound volume
//     */

//    void addToConv(String name, FloatControl control) {
//        conferencePane.addUser(name, control);
//    }

    /**
     * Remove fro conferencePane
     *
     * @param name to remove
     */

    void removeFromConf(String name) {
        conferencePane.removeUser(name);
    }

    /**
     * Removes conference tab when is over
     */

    void stopConversation() {        // CODE SEND_DISCONNECT_FROM_CONV
        if (conferencePane != null) {
            int i = callTable.indexOfTab(CONVERSATION_TAB_NAME);
            conferencePane.clear();
            if (i != -1) {
                callTable.removeTabAt(i);
            }
            callTable.revalidate();
        }
    }

    /**
     * Remove all tabs and clear all collections
     * Prepare for new server to connect
     */

    private void removeAllTabs() {
        for (int i = 0; i < callTable.getTabCount(); i++) {
            callTable.removeTabAt(i);
        }
//        tabs.clear();
//        model.removeAllElements();
    }

//    /**
//     * Method for displaying message from conference
//     *
//     * @param message plain text
//     * @param from    BaseUser.toString()
//     */
//
//    void showConferenceMessage(String message, String from) {
//        conferencePane.showMessage(message, from);
//        colorForMessage(callTable.indexOfTab(CONVERSATION_TAB_NAME));
//    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new DefaultListModel<>();
        usersList = new JList<>(model);
    }
//
}
