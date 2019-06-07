package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.MainFrameActions;
import Bin.Networking.Utility.BaseUser;

import javax.imageio.ImageIO;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Function;

public class MainFrame extends JFrame {

    private FirstSkin firstSkin;
    private SecondSkin secondSkin;

    private MainFrameActions actions;


    public MainFrame(MainFrameActions actions) throws NotInitialisedException {
        this.actions = actions;
        updateActions();

        firstSkin = new FirstSkin(actions);
        add(firstSkin.getPane());

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

        setVisible(true);

    }

    private void updateActions() throws NotInitialisedException {
        actions.updateConnect(connect(actions.connect()));
        actions.updateCreateServer(createServerFunction(actions.createServer()));
        actions.updateDisconnect(disconnect(actions.disconnect()));
    }

    private Function<String[], Boolean> connect(Function<String[], Boolean> connect) {
        return strings -> {
            Boolean aBoolean = connect.apply(strings);
            if (aBoolean == null) {
                errorCase();
                return false;
            }
            if (aBoolean) {
                remove(firstSkin.getPane());
                if (secondSkin == null) {
                    try {
                        secondSkin = new SecondSkin(actions.nameAndId().get(), actions);
                    } catch (NotInitialisedException e) {
                        e.printStackTrace();
                    }
                }
                add(secondSkin.getPane());
                revalidate();
                repaint();
            } else {
                showDialog("Audio format is not acceptable");
            }
            return true;
        };
    }

    private Function<String[], Boolean> createServerFunction(Function<String[], Boolean> createServer) {
        return strings -> {
            Boolean created = createServer.apply(strings);
            if (created == null) {
                showDialog("Port already in use, server wasn't created");
                return false;
            }
            if (created) {
                showDialog("Server created");
                return true;
            } else {
                showDialog("Server already created");
                return false;
            }
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

    private void showDialog(String text) {
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

    public void showIncomingCall(String who, String convInfo) {
        EventQueue.invokeLater(() -> secondSkin.callIncomingDialog(who, convInfo));
    }

    public void closeCall(String message) {
        EventQueue.invokeLater(() -> {
            secondSkin.closeCallDialog();
            JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void startConversation(String user, FloatControl control) {
        EventQueue.invokeLater(() -> secondSkin.conversationStart(user, control));
    }

    public void addToConv(String name, FloatControl control) {
        EventQueue.invokeLater(() -> secondSkin.addToConv(name, control));
    }

    public void removeFromConv(String name) {
        EventQueue.invokeLater(() -> secondSkin.removeFromConf(name));
    }

    public void closeConversation() {
        EventQueue.invokeLater(() -> secondSkin.stopConversation());
    }
}