package Bin.GUI.Forms;

import Bin.GUI.Interfaces.SecondSkinActions;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handles messaging, call pane
 * Can call and disconnect from here
 * Has pop up menu in usersList
 */

class SecondSkin implements ErrorHandler {

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
     * Need for messaging contain entries name - pane
     */

    private final Map<String, ThirdSkin> tabs;

    private final CallDialog callDialog;
    private ConferencePane conferencePane;

    /**
     * Available actions
     */

    private final SecondSkinActions actions;

    /**
     * Default constructor
     * Init firstly
     * 1 - update actions
     * 2 - set my name and id
     * 3 - register buttons action
     * 4 - create pop up menu for userList
     *
     * @param nameAndId your data
     * @param actions   available actions
     */

    SecondSkin(String nameAndId, SecondSkinActions actions) {
        this.actions = actions;
        updateActions();

        tabs = new HashMap<>();
        callDialog = new CallDialog(actions, mainPane);

        labelMe.setText(nameAndId);

        disconnectButton.addActionListener(e -> actions.disconnect().run());

        callButton.addActionListener(e -> callOutcomDialog(actions.callSomeOne()));

        callTable.addChangeListener(e -> deColored(callTable.getSelectedIndex()));

        registerPopUp();

    }

    /**
     * Upgrades already existed actions
     */

    private void updateActions() {
        actions.updateCloseTab(closeTabRun());
        actions.updateDisconnect(disconnect(actions.disconnect()));
        actions.updateEndCall(endCall(actions.endCall()));
    }

    private Runnable disconnect(Runnable disconnect) {
        return () -> {
            disconnect.run();
            clearSkin();
        };
    }

    private Runnable endCall(Runnable end) {
        return () -> {
            end.run();
            stopConversation();
        };
    }

    JPanel getPane() {
        return mainPane;
    }

    /**
     * Creates and register pop up menu on usersList
     * Consist of
     * Send message - when user is selected open ThirdSkin on callTable
     * Refresh - ask for users on the server
     */

    private void registerPopUp() {
        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessageMenu = new JMenuItem("Send Message");
        sendMessageMenu.addActionListener(e -> {
            if (!selected()) return;
            BaseUser selected = getSelected();
            String s = selected.toString();
            if (isExist(s)) return;
            if (contain(s)) {
                callTable.addTab(s, tabs.get(s).getMainPane());
            } else {
                callTable.addTab(s, createPane(s).getMainPane());
            }
        });
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> actions.callForUsers().run());
        popupMenu.add(sendMessageMenu);
        popupMenu.add(refresh);

        usersList.setComponentPopupMenu(popupMenu);
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

    private boolean isExist(String nameAndId) {
        return callTable.indexOfTab(nameAndId) != -1;
    }

    private boolean contain(String nameAndId) {
        return tabs.containsKey(nameAndId);
    }

    /**
     * Creates ThirdSkin object which has JPane
     *
     * @param name for who you create it
     * @return ready to use ThirdSkin
     */

    private ThirdSkin createPane(String name) {
        ThirdSkin thirdSkin = new ThirdSkin(name, actions);
        tabs.put(name, thirdSkin);
        return thirdSkin;
    }

    /**
     * Display a user message
     * if there is no ThirdSkin creates
     * otherwise uses cashed
     * or already displayed one
     *
     * @param from    message sent
     * @param message plain text
     */

    void showMessage(final BaseUser from, final String message) {
        String s = from.toString();
        if (isExist(s)) {
            tabs.get(s).showMessage(message, false);
        } else {
            if (contain(s)) {
                ThirdSkin thirdSkin = tabs.get(s);
                callTable.addTab(s, thirdSkin.getMainPane());
                thirdSkin.showMessage(message, false);
            } else {
                ThirdSkin pane = createPane(s);
                callTable.addTab(s, pane.getMainPane());
                pane.showMessage(message, false);
            }
        }
        colorForMessage(callTable.indexOfTab(s));
    }

    private Runnable closeTabRun() {
        return () -> {
            int selectedIndex = callTable.getSelectedIndex();
            if (selectedIndex == -1) return;
            callTable.removeTabAt(selectedIndex);
        };
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

    /**
     * Call a selected user
     * show CallDialog with appropriate actions
     *
     * @param call action that sendSound call to some one
     */

    private void callOutcomDialog(Consumer<BaseUser> call) {
        if (!selected()) {
            return;
        }
        BaseUser selected = getSelected();
        call.accept(selected);

        callDialog.showOutcoming(selected.toString());
    }

    /**
     * Show CallDialog when you get called
     *
     * @param who      calls you
     * @param convInfo contain baseUser.toString() + "\n" and so on
     */

    void callIncomingDialog(String who, String convInfo) {
        callDialog.showIncoming(who, convInfo);
    }

    /**
     * Just to dispose CallDialog
     */

    void closeCallDialog() {
        callDialog.dispose();
    }

    /**
     * Creates if necessary conferencePane
     * and add user who called you in
     *
     * @param user    who calls you
     * @param control volume for him
     */

    void conversationStart(String user, FloatControl control) {
        if (conferencePane == null) {
            conferencePane = new ConferencePane(actions);
        }
        conferencePane.addUser(user, control);
        callTable.addTab(CONVERSATION_TAB_NAME, conferencePane.getMainPane());
    }

    /**
     * Add some one on conferencePane
     *
     * @param name    who to add
     * @param control sound volume
     */

    void addToConv(String name, FloatControl control) {
        conferencePane.addUser(name, control);
    }

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

    private void clearSkin() {
        for (int i = 0; i < callTable.getTabCount(); i++) {
            callTable.removeTabAt(i);
        }
        tabs.clear();
        model.removeAllElements();
    }

    void setNameAndId(String nameAndId){
        labelMe.setText(nameAndId);
    }

    /**
     * Method for displaying message from conference
     *
     * @param message plain text
     * @param from BaseUser.toString()
     */

    void showConferenceMessage(String message, String from){
        conferencePane.showMessage(message, from);
        colorForMessage(callTable.indexOfTab(CONVERSATION_TAB_NAME));
    }

    @Override
    public void errorCase() {
        stopConversation();
        clearSkin();
        iterate();
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{callDialog, conferencePane};
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new DefaultListModel<>();
        usersList = new JList<>(model);
    }
//
}
