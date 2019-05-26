package Bin.GUI.Forms;

import Bin.Networking.Utility.BaseUser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MainFrame extends JFrame {

    private FirstSkin firstSkin;
    private SecondSkin secondSkin;
//    private CallDialog callDialog;
//    private ConversationPane conversationPane;

    public MainFrame(Function<String[], Boolean> connect, Function<String[], Boolean> createServer, Supplier<String> nameAndId,
                     Runnable disconnect, Runnable callForUsers, BiConsumer<Integer, String> sendMessage) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width / 2 - 200, screenSize.height / 2 - 200,
                400, 400);

        setTitle("Skype but bass boosted");

        try {

            setIconImage(ImageIO.read(getClass().getResource("/Images/ricardo.png")));
        } catch (IOException e) {
            e.printStackTrace();
            showDialog("Can't set the Icon");
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        firstSkin = new FirstSkin(connect(connect, nameAndId, disconnect(disconnect), callForUsers, sendMessage), createServer(createServer));

        add(firstSkin.getPane());

        setVisible(true);

    }

    private Consumer<String[]> connect(Function<String[], Boolean> connect, Supplier<String> nameAndId,
                                       Runnable disconnect, Runnable callForUsers, BiConsumer<Integer, String> sendMessage) {
        return strings -> {
            Boolean aBoolean = connect.apply(strings);
            if (aBoolean == null) {
                errorCase();
                return;
            }
            if (aBoolean) {
                remove(firstSkin.getPane());
                if (secondSkin == null) {
                    secondSkin = new SecondSkin(nameAndId.get(), disconnect, callForUsers, sendMessage);
                }
                add(secondSkin.getPane());
                revalidate();
//                    repaint();
            } else {
                showDialog("Audio format is not acceptable");
            }
        };
    }

    private Consumer<String[]> createServer(Function<String[], Boolean> createServer) {
        return strings -> {
            Boolean created = createServer.apply(strings);
            if (created == null) {
                showDialog("Port already in use, server wasn't created");
                return;
            }
            if (created)
                showDialog("Server created");
            else
                showDialog("Server already created");
        };
    }

    private Runnable disconnect(Runnable disconnect) {
        return () -> {
            disconnect.run();
            remove(secondSkin.getPane());
            add(firstSkin.getPane());
            revalidate();
            repaint();
        };

    }

    public void showDialog(String text) {
        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(this, text, "Message", JOptionPane.INFORMATION_MESSAGE));
    }

    public void errorCase() {
        EventQueue.invokeLater(() -> {
            removeAll();
            add(firstSkin.getPane());
            JOptionPane.showMessageDialog(this, "Disconnected from the server because of network failing", "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public void updateUsers(BaseUser[] users) {
        EventQueue.invokeLater(() -> secondSkin.displayUsers(users));
    }

    public void showMessage(final BaseUser from, final String message) {
        EventQueue.invokeLater(() -> secondSkin.showMessage(from, message));
    }
}
//    public void updateList() {
//        secondSkin.updateList();
//    }

//    public void showMessage(String message, int from) {
//        secondSkin.showMessagePane(Main.getInstance().getUserById(from), message);
//        /*
//         * Add here audio playback as a message indicator
//         */
//    }

//    public void showCallingDialog(ClientUser who) {
//        EventQueue.invokeLater(() -> {
//            if (!callDialog.showCallingDialog(who)
//                    && callDialog.getType().equals(CallDialog.CALL.IN)
//                    && callDialog.getWho().equals(who)) {
//                Main.accept();
//
//            }
//        });
//    }

//    public void showCallDialog(int from) {
//        EventQueue.invokeLater(() -> {
//            ClientUser who = Main.getInstance().getUserById(from);
//            if (!callDialog.showCallDialog(who)) {
//                if (callDialog.getType().equals(CallDialog.CALL.OUT)) {
//                    if (callDialog.getWho().equals(who)) {
//                        Main.accept();
//
//                    } else showMessage("Call me", from);
//                } else showMessage("Call me", from);
//            }
//        });
//    }

//    private void closeDialog(String whatToSay) {
//        EventQueue.invokeLater(() -> {
//            callDialog.dispose();
//            if (whatToSay != null)
//                showDialog(whatToSay);
//        });
//    }

//    public void denyReceived() {
//        closeDialog("Call denied");
//    }
//
//    public void cancelReceived() {
//        closeDialog("Dude cancelled the call");
//    }
//
//    public void acceptReceived(int from) {
//        closeDialog(null);
//        ClientUser userById = Main.getInstance().getUserById(from);
//        AudioClient.getInstance().add(from);
//        addUserToConversation(userById);
//        Main.getInstance().getAudioCapture().start();
//        addUserToConversation(Main.getInstance().createAudioCapture(), Main.getInstance().getUserById(from));
//    }
//
//    public void addUserToConversation(ClientUser who) {
//        if (conversationPane == null) {
//            conversationPane = new ConversationPane();
//        }
//        conversationPane.add(who.getId());
//        secondSkin.showConference(conversationPane.getMainPane());
//
//    }
//
//    public void addUserToConversation(AudioCapture audioCapture,ClientUser user){
//        if (conversationPane == null)
//            conversationPane = new ConversationPane(audioCapture);
//        conversationPane.add(user.getId());
//        secondSkin.updateConference(conversationPane);
//    }

//    public void showCalling(int from){
//        EventQueue.invokeLater(() -> {
//            BaseUser user = Main.getInstance().getUserById(from);
//            if (secondSkin.getCallingDialog() != null && secondSkin.getCallingDialog().isVisible()) {
//                showMessage("Call me", from);
//                /*
//                * handle here
//                 */
//                try {
//                    Main.getInstance().getWriter().writeDeny(Main.getInstance().getMeId(), from);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                int call = JOptionPane.showConfirmDialog(this, "Call from " + user.getName(), "Call", JOptionPane.YES_NO_OPTION);
//                if (call == JOptionPane.YES_OPTION){
//                    /*
//                    * start messaging
//                     */
//                }else {
//                    /*
//                    * handle here
//                     */
//                    try {
//                        Main.getInstance().getWriter().writeDeny(Main.getInstance().getMeId(), from);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        });
//    }

//    public void dennyReceived(){
//        EventQueue.invokeLater(() -> secondSkin.getCallingDialog().dispose());
//    }

//
//}
//