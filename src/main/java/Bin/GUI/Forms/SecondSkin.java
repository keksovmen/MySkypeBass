package Bin.GUI.Forms;

import Bin.Networking.ClientController;
import Bin.Networking.Utility.BaseUser;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SecondSkin {

    private static final String CONVERSATION_TAB_NAME = "Conversation";

    private JLabel labelMe;
    private JList<BaseUser> usersList;
    private JButton callButton;
    private JButton disconnectButton;
    private JTabbedPane callTable;
    private JPanel mainPane;
    private DefaultListModel<BaseUser> model;
    private Map<String, ThirdSkin> tabs;

    private BiConsumer<Integer, String> sendMessage;

    private CallDialog callDialog;
    private ConferencePane conferencePane;
//    private ConversationPane conversationPane;
//    private Dialog callingDialog;

    public SecondSkin(String nameAndId, Runnable disconnect, Runnable callForUsers, BiConsumer<Integer,
            String> sendMessage, Consumer<BaseUser> call, Consumer<BaseUser> onCancel,
                      Consumer<BaseUser[]> onAccept, Consumer<BaseUser> onDeny) {

        this.sendMessage = sendMessage;

        tabs = new HashMap<>();

        labelMe.setText(nameAndId);

        disconnectButton.addActionListener(e -> disconnect.run());

        callButton.addActionListener(e -> callOutcomDialog(call));

        callTable.addChangeListener(e -> decolored(callTable.getSelectedIndex()));

        createPopupMenu(callForUsers, sendMessage);

        callDialog = new CallDialog(onSomethingCall(onCancel), onAcceptCall(onAccept), onSomethingCall(onDeny));


    }

    JPanel getPane() {
        return mainPane;
    }

    private void createPopupMenu(Runnable callForUsers, BiConsumer<Integer, String> sendMessage) {
        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessageMenu = new JMenuItem("Send Message");
        sendMessageMenu.addActionListener(e -> EventQueue.invokeLater(() -> {
            if (!selected()) return;
            BaseUser selected = getSelected();
            String s = selected.toString();
            if (isExist(s)) return;
            if (contain(s))
                callTable.addTab(s, tabs.get(s).getMainPane());
            else
                callTable.addTab(s, createPane(s, (message) -> sendMessage.accept(selected.getId(), message)).getMainPane());
        }));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> callForUsers.run());
        popupMenu.add(sendMessageMenu);
        popupMenu.add(refresh);

        usersList.setComponentPopupMenu(popupMenu);
    }

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

    private ThirdSkin createPane(String name, Consumer<String> sendMessage) {
        ThirdSkin thirdSkin = new ThirdSkin(name, sendMessage, this::closeTab);
        tabs.put(name, thirdSkin);
        return thirdSkin;
    }

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
                ThirdSkin pane = createPane(s, letter -> sendMessage.accept(from.getId(), letter));
                callTable.addTab(s, pane.getMainPane());
                pane.showMessage(message, false);
            }
        }
        colorForMessage(callTable.indexOfTab(s));
    }

    private void closeTab() {
        int selectedIndex = callTable.getSelectedIndex();
        if (selectedIndex == -1) return;
        callTable.removeTabAt(selectedIndex);
    }

    private void colorForMessage(final int indexOfTab) {
        if (callTable.getSelectedIndex() == indexOfTab) return;
        callTable.setBackgroundAt(indexOfTab, Color.RED);
    }

    private void decolored(final int indexOfTab) {
        if (indexOfTab == -1 || !callTable.getBackgroundAt(indexOfTab).equals(Color.RED)) return;
        callTable.setBackgroundAt(indexOfTab, null);
    }

    private void callOutcomDialog(Consumer<BaseUser> call) {
        if (!selected()) return;
        BaseUser selected = getSelected();
        call.accept(selected);

        callDialog.showOutcoming(selected.toString());
    }

    void callIncomingDialog(String who, String convInfo) {
        callDialog.showIncoming(who, convInfo);
    }

    private Consumer<String> onSomethingCall(Consumer<BaseUser> onSomethingCall) {
        return s -> onSomethingCall.accept(BaseUser.parse(s));
    }

    private Consumer<String> onAcceptCall(Consumer<BaseUser[]> onSomethingCall) {
        return s -> onSomethingCall.accept(ClientController.parseUsers(s));
    }

    void closeCallDialog() {
        callDialog.dispose();
    }

    void conversationStart(Runnable end, Supplier<Boolean> mute, String user, FloatControl control) {
        if (conferencePane == null) {
            conferencePane = new ConferencePane(endCall(end), mute);
        }
        conferencePane.addUser(user, control);
        callTable.addTab(CONVERSATION_TAB_NAME, conferencePane.getMainPane());
    }

    void addToConv(String name, FloatControl control) {
        conferencePane.addUser(name, control);
    }

    void removeFromConf(String name) {
        conferencePane.removeUser(name);
    }

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

    private Runnable endCall(Runnable end) {
        return () -> {
            end.run();
            int i = callTable.indexOfTab(CONVERSATION_TAB_NAME);
            if (i != -1) {
                callTable.removeTabAt(i);
                callTable.revalidate();
            }
        };
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new DefaultListModel<>();
        usersList = new JList<>(model);
    }
//
}
