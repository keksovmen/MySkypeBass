package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.SecondSkinActions;
import Bin.Networking.Utility.BaseUser;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    private CallDialog callDialog;
    private ConferencePane conferencePane;

    private SecondSkinActions actions;



    public SecondSkin(String nameAndId, SecondSkinActions actions) throws NotInitialisedException {
        this.actions = actions;
        updateActions();
        tabs = new HashMap<>();
        callDialog = new CallDialog(actions);

        labelMe.setText(nameAndId);

        disconnectButton.addActionListener(e -> {
            try {
                actions.disconnect().run();
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        callButton.addActionListener(e -> {
            try {
                callOutcomDialog(actions.callSomeOne());
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        callTable.addChangeListener(e -> decolored(callTable.getSelectedIndex()));

        createPopupMenu();

    }

    private void updateActions(){
        actions.updateCloseTab(closeTabRun());
    }

    JPanel getPane() {
        return mainPane;
    }

    private void createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessageMenu = new JMenuItem("Send Message");
        sendMessageMenu.addActionListener(e -> EventQueue.invokeLater(() -> {
            if (!selected()) return;
            BaseUser selected = getSelected();
            String s = selected.toString();
            if (isExist(s)) return;
            if (contain(s)) {
                callTable.addTab(s, tabs.get(s).getMainPane());
            }
            else {
                callTable.addTab(s, createPane(s).getMainPane());
            }
        }));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> {
            try {
                actions.callForUsers().run();
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });
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

    private ThirdSkin createPane(String name) {
        ThirdSkin thirdSkin = new ThirdSkin(name, actions);
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

    private void colorForMessage(final int indexOfTab) {
        if (callTable.getSelectedIndex() == indexOfTab) return;
        callTable.setBackgroundAt(indexOfTab, Color.RED);
    }

    private void decolored(final int indexOfTab) {
        if (indexOfTab == -1 || !callTable.getBackgroundAt(indexOfTab).equals(Color.RED)) return;
        callTable.setBackgroundAt(indexOfTab, null);
    }

    private void callOutcomDialog(Consumer<BaseUser> call) {
        if (!selected()) {
            return;
        }
        BaseUser selected = getSelected();
        call.accept(selected);

        callDialog.showOutcoming(selected.toString());
    }

    void callIncomingDialog(String who, String convInfo) {
        callDialog.showIncoming(who, convInfo);
    }

    void closeCallDialog() {
        callDialog.dispose();
    }

    void conversationStart(String user, FloatControl control) {
        if (conferencePane == null) {
            conferencePane = new ConferencePane(actions);
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new DefaultListModel<>();
        usersList = new JList<>(model);
    }
//
}
