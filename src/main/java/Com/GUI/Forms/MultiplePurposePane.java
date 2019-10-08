package Com.GUI.Forms;

import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.Model.BaseUnEditableModel;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.UpdaterAndHandler;
import Com.Util.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    static final String CONVERSATION_TAB_NAME = "Conversation";

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
     * Need for messaging isCashed.
     * Entries baseUser - pane
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
        conferencePane = new ConferencePane(whereToReportActions, this);

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

        usersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.isShiftDown()) {
                        onSendMessage();
                        e.consume();
                        return;
                    }
                    if (e.getClickCount() == 2) {
                        call(whereToReportActions);
                    }
                }
                e.consume();

            }
        });

    }

    @Override
    public void update(BaseUnEditableModel model) {
        this.model.clear();
        model.getUserMap().values().forEach(
                baseUser -> this.model.addElement(baseUser));
        //Go through tabs and set online or offline icons, except CONFERENCE
        changeIconsOfTabs();

        conferencePane.update(model);

        mainPane.revalidate();
        mainPane.repaint();

    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
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
            case EXITED_CONVERSATION: {
                onExitConversation();
                break;
            }
        }
        conferencePane.handle(action, from, stringData, bytesData, intData);
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

    private void onExitConversation() {
        closeTab(CONVERSATION_TAB_NAME);
    }

    private void onDisconnect() {
        removeAllTabs();
        tabs.clear();
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

    /* Other stuff */

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
        sendMessageMenu.addActionListener(e -> onSendMessage());

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

    private void onSendMessage() {
        if (!selected())
            return;
        BaseUser selected = getSelected();
        if (isShownAlready(selected))
            return;
        if (isCashed(selected)) {
            callTable.addTab(
                    selected.toString(),
                    Resources.getOnlineIcon(),
                    tabs.get(selected).getMainPane()
            );
        } else {
            callTable.addTab(
                    selected.toString(),
                    Resources.getOnlineIcon(),
                    createPane(selected).getMainPane());
        }
    }

    private void changeIconsOfTabs() {
        tabs.forEach((user, messagePane) -> {
            if (messagePane.isShown()) {
                if (!model.contains(user)) {
                    callTable.setIconAt(
                            callTable.indexOfTab(user.toString()),
                            Resources.getOfflineIcon()
                    );
                }
            }
        });
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

    private void showConversationPane() {
        callTable.addTab(
                CONVERSATION_TAB_NAME,
                Resources.getConversationIcon(),
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

    private void showMessage(final BaseUser from, final String message, boolean toConv) {
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
                    callTable.addTab(from.toString(), Resources.getOnlineIcon(), messagePane.getMainPane());
                    messagePane.showMessage(message, false);
                } else {
                    MessagePane pane = createPane(from);
                    callTable.addTab(from.toString(), Resources.getOnlineIcon(), pane.getMainPane());
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

    /**
     * Remove all tabs and clear all collections
     * Prepare for new server to connect
     */

    private void removeAllTabs() {
        for (int i = callTable.getTabCount() - 1; i >= 0; i--) {
            callTable.removeTabAt(i);
        }
        callTable.revalidate();
        callTable.repaint();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new DefaultListModel<>();
        usersList = new JList<>(model);
    }
}
