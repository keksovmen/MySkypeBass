package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Model.ModelObserver;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Collection.Track;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources.Resources;
import com.Implementation.Util.DesktopResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles messaging, call pane
 * Can call and disconnect from here
 * Has pop up menu in usersList
 */

public class MultiplePurposePane implements ModelObserver, LogicObserver, ButtonsHandler {

    /**
     * Name for conversation tab uses in search cases
     * so decided to make it as a variable
     */

    protected static final String CONVERSATION_TAB_NAME = "Conversation";

    private JTextArea labelMe;

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

    private final ButtonsHandler helpHandlerPredecessor;


    /**
     * Default constructor
     * Init firstly
     * 1 - modelObservation actions
     * 2 - set my name and id
     * 3 - register buttons action
     * 4 - create pop up menu for userList
     */

    public MultiplePurposePane(ButtonsHandler helpHandlerPredecessor) {
        tabs = new HashMap<>();
        conferencePane = new ConferencePane(this, this::closeTab);

        this.helpHandlerPredecessor = helpHandlerPredecessor;

        setListeners();


    }

    @Override
    public void modelObservation(UnEditableModel model) {
        this.model.clear();
        model.getUserMap().values().forEach(
                baseUser -> this.model.addElement(baseUser));
        //Go through tabs and set online or offline icons, except CONFERENCE
        changeIconsOfTabs();

        conferencePane.modelObservation(model);

        mainPane.revalidate();
        mainPane.repaint();

    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        switch (action) {
            case CONNECT_SUCCEEDED: {
                labelMe.setText((String) data[0]);
                break;
            }
            case INCOMING_MESSAGE: {
                showMessage((BaseUser) data[0], (String) data[1], (int) data[2] == 1);
                break;
            }
            case DISCONNECTED: {
                onDisconnect();
                break;
            }
            case CALLED_BUT_BUSY: {
                onCalledButBusy((BaseUser) data[0]);
                break;
            }
            case CALL_ACCEPTED: {
                onCallAccepted();
                break;
            }
            case BOTH_IN_CONVERSATION: {
                onBothInConv((BaseUser) data[0]);
                break;
            }
            case EXITED_CONVERSATION: {
                onExitConversation();
                break;
            }
        }
        conferencePane.observe(action, data);
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //delegate
        helpHandlerPredecessor.handleRequest(button, data);
    }

    private void setListeners() {
        callTable.addChangeListener(e -> deColored(callTable.getSelectedIndex()));

        disconnectButton.addActionListener(e -> disconnect());

        callButton.addActionListener(e -> call());
//
        registerPopUp();

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
                        call();
                    }
                }
                e.consume();

            }
        });
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

    private void call() {
        if (!selected())
            return;
        handleRequest(
                BUTTONS.CALL,
                new Object[]{getSelected()}
        );
    }

    private void disconnect() {
        handleRequest(
                BUTTONS.DISCONNECT,
                null
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

    private void registerPopUp() {
        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessageMenu = new JMenuItem("Send Message");
        sendMessageMenu.addActionListener(e -> onSendMessage());

        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e ->
                handleRequest(
                        BUTTONS.ASC_FOR_USERS, null
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
                    selected.prettyString(),
                    new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()),
                    tabs.get(selected).getMainPane()
            );
        } else {
            callTable.addTab(
                    selected.prettyString(),
                    new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()),
                    createPane(selected).getMainPane());
        }
    }

    private void changeIconsOfTabs() {
        tabs.forEach((user, messagePane) -> {
            if (messagePane.isShown()) {
                if (!model.contains(user)) {
                    callTable.setIconAt(
                            callTable.indexOfTab(user.prettyString()),
                            new ImageIcon(((DesktopResources) Resources.getInstance()).getOfflineIcon())
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
        return isShownAlready(user.prettyString());
    }

    private boolean isCashed(BaseUser user) {
        return tabs.containsKey(user);
    }

    private void showConversationPane() {
        callTable.addTab(
                CONVERSATION_TAB_NAME,
                new ImageIcon(((DesktopResources) Resources.getInstance()).getConversationIcon()),
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
        MessagePane messagePane = new MessagePane(user, () -> closeTab(user.prettyString()), this);
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
                    callTable.addTab(from.prettyString(), new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()), messagePane.getMainPane());
                    messagePane.showMessage(message, false);
                } else {
                    MessagePane pane = createPane(from);
                    callTable.addTab(from.prettyString(), new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()), pane.getMainPane());
                    pane.showMessage(message, false);
                }
            }
            tabName = from.prettyString();
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
        usersList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((BaseUser)value).prettyString(), index, isSelected, cellHasFocus);
            }

        });
    }

    /**
     * Put pop up menu on a given component
     * That will give you view on all possible sounds and preview them
     *
     * @param component      where to register
     * @param textField      where put meta symbols to send
     * @param buttonsHandler helpHandler to preview sound
     */

    public static void registerPopUp(JComponent component, JTextField textField, ButtonsHandler buttonsHandler) {
        JPopupMenu popupMenu = new JPopupMenu("Sounds");
        Map<Integer, Track> tracks = Resources.getInstance().getNotificationTracks();

        for (int i = 0; i < tracks.size(); i++) {
            JMenuItem menuItem = new JMenuItem(tracks.get(i).description);
            int j = i;
            menuItem.addActionListener(e -> {
                if (e.getModifiers() == InputEvent.META_MASK) {
                    buttonsHandler.handleRequest(BUTTONS.PREVIEW_SOUND, new Object[]{FormatWorker.asMessageMeta(j), j});
                } else {
                    textField.setText(textField.getText() + FormatWorker.asMessageMeta(j));
                }
            });
            popupMenu.add(menuItem);
        }
        component.setComponentPopupMenu(popupMenu);
    }
}
