package Bin.GUI.Forms;

import Bin.GUI.Main;
import Bin.Utility.BaseUser;
import Bin.Utility.ClientUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SecondSkin {
    private JLabel labelMe;
    private JList<ClientUser> usersList;
    private JButton callButton;
    private JButton disconnectButton;
    private JTabbedPane callTable;
    private JPanel secondPane;
    private JPanel mainPane;
    private DefaultListModel<ClientUser> model;
//    private ConversationPane conversationPane;
//    private Dialog callingDialog;

    public SecondSkin(MainFrame mainFrame) {

        callButton.addActionListener(e -> {
                if (usersList.getSelectedIndex() == -1) return;
                Main.getInstance().call(model.get(usersList.getSelectedIndex()));
        });
        disconnectButton.addActionListener(e -> Main.getInstance().disconnect());

        JPopupMenu popupMenu = new JPopupMenu("Utility");
        JMenuItem sendMessage = new JMenuItem("Send Message");
        sendMessage.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (usersList.getSelectedIndex() == -1) return;
                ClientUser user = model.get(usersList.getSelectedIndex());
                showMessagePane(user, "");
            }
        });
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.getInstance().writeRefresh();
            }
        });
        popupMenu.add(sendMessage);
        popupMenu.add(refresh);
        usersList.setComponentPopupMenu(popupMenu);

        model = new DefaultListModel<>();
        usersList.setModel(model);

    }

    JPanel getPane() {
        return mainPane;
    }

    void relable(String text){
        EventQueue.invokeLater(() -> labelMe.setText(text));
    }

    void updateList(){
        EventQueue.invokeLater(() -> {
            model.clear();
            Main.getInstance().getUsers().forEach(baseUser -> model.addElement(baseUser));
            mainPane.revalidate();
        });
    }

    void showMessagePane(ClientUser user, String message){
        EventQueue.invokeLater(() -> {
            if (!secondPane.isVisible()) secondPane.setVisible(true);
            if (user.getThirdSkin() == null) user.setThirdSkin(new ThirdSkin(user, this));
            if (callTable.indexOfTab(user.toString()) == -1) callTable.addTab(user.toString(), user.getThirdSkin().getMainPane());
            user.getThirdSkin().showMessage(message);
        });
    }

    void closeMessageTab(BaseUser user){
        EventQueue.invokeLater(() ->{
            callTable.removeTabAt(callTable.indexOfTab(user.toString()));
            if (callTable.getTabCount() == 0)
                secondPane.setVisible(false);

        });
    }

    void showConference(JPanel panel){
        EventQueue.invokeLater(() -> {
        if (!secondPane.isVisible()) secondPane.setVisible(true);
        if (callTable.indexOfTab("Conference") == -1) callTable.addTab("Conference", panel);
//        mainPane.revalidate();
    });
    }
//    void updateConference(ConversationPane conversationPane){
//        EventQueue.invokeLater(() -> {
//            if (!secondPane.isVisible()) secondPane.setVisible(true);
//            if (callTable.indexOfTab("Conversation") == -1)
//                callTable.addTab("Conversation", conversationPane.getMainPane());
//
////        if (conversationPane == null)
////            conversationPane = new ConversationPane();
//        });
//    }

//    public Dialog getCallingDialog() {
//        return callingDialog;
//    }
}
