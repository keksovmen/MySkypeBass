package com.Implementation.GUI.Forms;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.ModelObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Collection.Track;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources.Resources;
import com.Implementation.Util.DesktopResources;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

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

    private JList<User> usersList;
    private JButton callButton;
    private JButton disconnectButton;
    private JTabbedPane callTable;
    private JPanel mainPane;

    /**
     * Contain baseUser[] that on server
     */

    private DefaultListModel<User> model;

    /**
     * Need for messaging isCashed.
     * Entries baseUser - pane
     */

    private final Map<User, MessagePane> tabs;

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
        $$$setupUI$$$();
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
                showMessage((User) data[0], (String) data[1], (int) data[2] == 1);
                break;
            }
            case DISCONNECTED: {
                onDisconnect();
                break;
            }
            case CALLED_BUT_BUSY: {
                onCalledButBusy((User) data[0]);
                break;
            }
            case CALL_ACCEPTED: {
                onCallAccepted();
                break;
            }
            case BOTH_IN_CONVERSATION: {
                onBothInConv((User) data[0]);
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

    private void onCalledButBusy(User who) {
        showMessage(who, "I called, but you had been calling already, call me back", false);
    }

    private void onBothInConv(User from) {
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
        User selected = getSelected();
        if (isShownAlready(selected))
            return;
        if (isCashed(selected)) {
            callTable.addTab(
                    selected.toString(),
                    new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()),
                    tabs.get(selected).getMainPane()
            );
        } else {
            callTable.addTab(
                    selected.toString(),
                    new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()),
                    createPane(selected).getMainPane());
        }
    }

    private void changeIconsOfTabs() {
        tabs.forEach((user, messagePane) -> {
            if (messagePane.isShown()) {
                if (!model.contains(user)) {
                    callTable.setIconAt(
                            callTable.indexOfTab(user.toString()),
                            new ImageIcon(((DesktopResources) Resources.getInstance()).getOfflineIcon())
                    );
                }
            }
        });
    }


    private boolean selected() {
        return usersList.getSelectedIndex() != -1;
    }

    private User getSelected() {
        return model.get(usersList.getSelectedIndex());
    }

    private boolean isShownAlready(String data) {
        return callTable.indexOfTab(data) != -1;
    }

    private boolean isShownAlready(User user) {
        return isShownAlready(user.toString());
    }

    private boolean isCashed(User user) {
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

    private MessagePane createPane(User user) {
        MessagePane messagePane = new MessagePane(user, () -> closeTab(user.toString()), this);
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

    private void showMessage(final User from, final String message, boolean toConv) {
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
                    callTable.addTab(from.toString(), new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()), messagePane.getMainPane());
                    messagePane.showMessage(message, false);
                } else {
                    MessagePane pane = createPane(from);
                    callTable.addTab(from.toString(), new ImageIcon(((DesktopResources) Resources.getInstance()).getOnlineIcon()), pane.getMainPane());
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
     * @param indexOfTab which to paint
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
        usersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, value.toString(), index, isSelected, cellHasFocus);
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayoutManager(1, 2, new Insets(3, 3, 3, 3), -1, -1));
        mainPane.setVisible(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 2, new Insets(3, 3, 3, 3), -1, -1));
        mainPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(250, -1), null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-11842741)), null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        usersList.setSelectionMode(0);
        scrollPane1.setViewportView(usersList);
        final JLabel label1 = new JLabel();
        label1.setIcon(new ImageIcon(getClass().getResource("/Images/online.png")));
        label1.setText("Online users:");
        panel1.add(label1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setHorizontalScrollBarPolicy(32);
        scrollPane2.setOpaque(false);
        scrollPane2.setVerticalScrollBarPolicy(20);
        scrollPane2.setVisible(true);
        panel1.add(scrollPane2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelMe = new JTextArea();
        labelMe.setEditable(false);
        labelMe.setEnabled(true);
        labelMe.setLineWrap(false);
        labelMe.setOpaque(false);
        labelMe.setText("Your name and unique id");
        labelMe.setWrapStyleWord(false);
        scrollPane2.setViewportView(labelMe);
        final JLabel label2 = new JLabel();
        label2.setText("Name - Id:");
        label2.setVisible(true);
        panel1.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        callButton = new JButton();
        callButton.setText("Call");
        panel2.add(callButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
        disconnectButton = new JButton();
        disconnectButton.setText("Disconnect");
        panel2.add(disconnectButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setVisible(true);
        mainPane.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, -1), null, 0, false));
        callTable = new JTabbedPane();
        panel3.add(callTable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
