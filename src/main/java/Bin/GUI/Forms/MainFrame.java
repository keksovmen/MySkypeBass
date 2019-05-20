package Bin.GUI.Forms;

import Bin.Audio.AudioClient;
import Bin.GUI.Main;
import Bin.Utility.ClientUser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    private FirstSkin firstSkin;
    private SecondSkin secondSkin;
    private CallDialog callDialog;
    private ConversationPane conversationPane;

    public MainFrame() throws HeadlessException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width / 2 - 200, screenSize.height / 2 - 200,
                400, 400);

        setTitle("Skype but bass boosted");

        try {
            setIconImage(ImageIO.read(getClass().getResource("/Images/ricardo.png")));
        } catch (IOException e) {
            e.printStackTrace();
            showDialog("Can set the Icon");
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        firstSkin = new FirstSkin(this);
        secondSkin = new SecondSkin(this);
        callDialog = new CallDialog(this);

        add(firstSkin.getPane());

        setVisible(true);

    }

    public static void main(String[] args) {
//        MainFrame firstLayer = new MainFrame();
        EventQueue.invokeLater(() -> Main.getInstance());

    }

    public String getMyName(){
        return firstSkin.getMyName();
    }

    public void changeFirstToSecond(){
        EventQueue.invokeLater(() -> {
            remove(firstSkin.getPane());
            add(secondSkin.getPane());
            repaint();
            revalidate();
        });
    }

    public void changeSecondToFirst(){
        EventQueue.invokeLater(() -> {
            remove(secondSkin.getPane());
            add(firstSkin.getPane());
            repaint();
            revalidate();
        });
    }

    public void setUserName(String text){
        secondSkin.relable(text);
    }

    public void showDialog(String text){
        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(this, text, "Message", JOptionPane.INFORMATION_MESSAGE));
    }

    public void updateList(){
        secondSkin.updateList();
    }

    public void showMessage(String message, int from){
        secondSkin.showMessagePane(Main.getInstance().getUserById(from), message);
        /*
        * Add here audio playback as a message indicator
         */
    }

    public void showCallingDialog(ClientUser who){
        EventQueue.invokeLater(() -> {
            if (!callDialog.showCallingDialog(who)
                    && callDialog.getType().equals(CallDialog.CALL.IN)
                    && callDialog.getWho().equals(who)){
                //Main.accept();

            }
        });
    }

    public void showCallDialog(int from){
        EventQueue.invokeLater(() -> {
            ClientUser who = Main.getInstance().getUserById(from);
            if (!callDialog.showCallDialog(who)){
                if (callDialog.getType().equals(CallDialog.CALL.OUT)){
                    if (callDialog.getWho().equals(who)){
                        //Main.accept();

                    }else showMessage("Call me", from);
                }else showMessage("Call me", from);
            }
        });
    }

    private void closeDialog(String whatToSay){
        EventQueue.invokeLater(() -> {
            callDialog.dispose();
            if (whatToSay != null)
                showDialog(whatToSay);
        });
    }

    public void denyReceived(){
        closeDialog("Call denied");
    }

    public void cancelReceived(){
        closeDialog("Dude cancelled the call");
    }

    public void acceptReceived(int from){
        closeDialog(null);
        ClientUser userById = Main.getInstance().getUserById(from);
        AudioClient.getInstance().add(from);
        addUserToConversation(userById);
        Main.getInstance().getAudioCapture().start();
//        addUserToConversation(Main.getInstance().createAudioCapture(), Main.getInstance().getUserById(from));
    }

    public void addUserToConversation(ClientUser who){
        if (conversationPane == null){
            conversationPane = new ConversationPane();
        }
        conversationPane.add(who.getId());
        secondSkin.showConference(conversationPane.getMainPane());

    }

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
//                    Main.getInstance().getWriter().writeDenay(Main.getInstance().getMeId(), from);
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
//                        Main.getInstance().getWriter().writeDenay(Main.getInstance().getMeId(), from);
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


}
