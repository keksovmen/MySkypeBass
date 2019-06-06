package Bin.GUI.Forms;

import Bin.Networking.Utility.BaseUser;

import javax.imageio.ImageIO;
import javax.sound.sampled.FloatControl;
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

    public MainFrame(Function<String[], Boolean> connect, Function<String[], Boolean> createServer, Supplier<String> nameAndId,
                     Runnable disconnect, Runnable callForUsers, BiConsumer<Integer, String> sendMessage,
                     Consumer<BaseUser> call, Consumer<BaseUser> callCancel, Consumer<BaseUser[]> callAccept, Consumer<BaseUser> callDeny) {

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

        firstSkin = new FirstSkin(connect(connect, nameAndId, disconnect(disconnect), callForUsers, sendMessage,
                call, callCancel, callAccept, callDeny), createServer(createServer));

        add(firstSkin.getPane());

        setVisible(true);

    }

    private Consumer<String[]> connect(Function<String[], Boolean> connect, Supplier<String> nameAndId,
                                       Runnable disconnect, Runnable callForUsers, BiConsumer<Integer, String> sendMessage,
                                       Consumer<BaseUser> call, Consumer<BaseUser> callCancel, Consumer<BaseUser[]> callAccept, Consumer<BaseUser> callDeny) {
        return strings -> {
            Boolean aBoolean = connect.apply(strings);
            if (aBoolean == null) {
                errorCase();
                return;
            }
            if (aBoolean) {
                remove(firstSkin.getPane());
                if (secondSkin == null) {
                    secondSkin = new SecondSkin(nameAndId.get(), disconnect, callForUsers, sendMessage, call, callCancel, callAccept, callDeny);
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

    public void showIncomingCall(String who, String convInfo){
        EventQueue.invokeLater(() -> secondSkin.callIncomingDialog(who, convInfo));
    }

    public void closeCall(String message){
        EventQueue.invokeLater(() -> {
            secondSkin.closeCallDialog();
            JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void startConversation(Runnable end, Supplier<Boolean> mute, String user, FloatControl control){
        EventQueue.invokeLater(() -> secondSkin.conversationStart(end, mute, user, control));
    }

    public void addToConv(String name, FloatControl control){
        EventQueue.invokeLater(() -> secondSkin.addToConv(name, control));
    }

    public void removeFromConv(String name){
        EventQueue.invokeLater(() -> secondSkin.removeFromConf(name));
    }

    public void closeConversation(){
        EventQueue.invokeLater(() -> secondSkin.stopConversation());
    }
}