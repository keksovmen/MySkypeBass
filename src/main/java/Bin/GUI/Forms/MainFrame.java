package Bin.GUI.Forms;

import Bin.GUI.Interfaces.MainFrameActions;
import Bin.Main;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;

import javax.imageio.ImageIO;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.function.Function;

/**
 * Uses as a wrapper of all others gui elements
 * Contains only which will appear firstly
 * Also it is JFrame
 */

public class MainFrame extends JFrame implements ErrorHandler {

    /**
     * Just wanted to try property maps
     * Contains some default string values such as:
     * name, ip, port, rate(mean sample rate).
     * Also consist with default one
     */

    static final Properties defaultStrings;

    /**
     * Will see it first
     * Just for connection and server creation
     */

    private FirstSkin firstSkin;

    /**
     * Will appear secondly
     * Handle all other stuff
     */

    private SecondSkin secondSkin;

    /**
     * Just to make the constructor easy to call
     * before it was like more than 12 function
     */

    private MainFrameActions actions;

    /*
      Static init of properties
     */

    static {
        Properties supaDefault = new Properties();
        supaDefault.setProperty("name", "");
        supaDefault.setProperty("ip", "127.0.0.1");
        supaDefault.setProperty("port", "8188");
        supaDefault.setProperty("rate", "44100");

        defaultStrings = new Properties(supaDefault);
        try {
            InputStream resourceAsStream = Main.class.getResourceAsStream("/properties/GuiDefaultStrings.properties");
            if (resourceAsStream != null) {
                defaultStrings.load(resourceAsStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor that initialise:
     * 1 - actions and update them
     * 2 - first skin
     * 3 - dimensions of window
     * 4 - title
     * 5 - icon
     *
     * @param actions all possible actions for GUIs
     */

    public MainFrame(MainFrameActions actions) {
        this.actions = actions;
        updateActions();

        firstSkin = new FirstSkin(actions);
        add(firstSkin.getPane());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width / 2 - 300, screenSize.height / 2 - 200,
                600, 400);

        setTitle("Skype but bass boosted");

        try {
            URL ricardoImg = getClass().getResource("/Images/ricardo.png");
            if (ricardoImg != null) {
                setIconImage(ImageIO.read(ricardoImg));
            }
        } catch (IOException e) {
//            e.printStackTrace();
            showDialog("Can't set the Icon");
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);

    }

    /**
     * Upgrade actions simply put some code over already existed
     */

    private void updateActions() {
        actions.updateConnect(connect(actions.connect()));
        actions.updateCreateServer(createServerFunction(actions.createServer()));
        actions.updateDisconnect(disconnect(actions.disconnect()));
    }

    /**
     * Upgrade action connect
     * add block and unblock the connect button when connecting
     * handle to display if it was unsuccessful or successful
     * null mean was an exception
     *
     * @param connect function to upgrade
     * @return 2 stage function
     */

    private Function<String[], Boolean> connect(Function<String[], Boolean> connect) {
        return strings -> {
            //block connect button
            firstSkin.blockConnectButton();
            Boolean aBoolean = connect.apply(strings);
            //release connect button
            firstSkin.releaseConnectButton();
            if (aBoolean == null) {
//                errorCase();
                showDialog("Server doesn't exist");
            } else if (aBoolean) {
                remove(firstSkin.getPane());
                //lazy init
                if (secondSkin == null) {
                    secondSkin = new SecondSkin(actions.nameAndId().get(), actions);
                }else{
                    secondSkin.setNameAndId(actions.nameAndId().get());
                }
                add(secondSkin.getPane());
                revalidate();
                repaint();
            } else {
                showDialog("Audio format is not acceptable");
            }
            return aBoolean;
        };
    }

    /**
     * Upgrade create server function
     * Simply adds some displaying info
     * null if was an exception
     *
     * @param createServer function to upgrade
     * @return 2 stage function
     */

    private Function<String[], Boolean> createServerFunction(Function<String[], Boolean> createServer) {
        return strings -> {
            Boolean created = createServer.apply(strings);
            if (created == null) {
                showDialog("Port already in use, server wasn't created");
            } else if (created) {
                showDialog("Server created");
            } else {
                showDialog("Server already created");
            }
            return created;
        };
    }

    /**
     * Upgrade disconnect function
     * add some removal of panes
     *
     * @param disconnect function to upgrade
     * @return 2 stage function
     */

    private Runnable disconnect(Runnable disconnect) {
        return () -> {
            disconnect.run();
            remove(secondSkin.getPane());
            add(firstSkin.getPane());
            revalidate();
            repaint();
        };

    }

    /**
     * Uses for showing some messages
     * even from other threads
     *
     * @param text to display
     */

    public void showDialog(String text) {
        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(this, text,
                "Message", JOptionPane.INFORMATION_MESSAGE));
    }

    /**
     * Gives users to display further
     * Works as wrapper
     *
     * @param users to display
     */

    public void updateUsers(BaseUser[] users) {
        EventQueue.invokeLater(() -> secondSkin.displayUsers(users));
    }

    /**
     * Call when need to display a message from some one
     *
     * @param from    user who write
     * @param message text to display
     */

    public void showMessage(final BaseUser from, final String message) {
        EventQueue.invokeLater(() -> secondSkin.showMessage(from, message));
    }

    /**
     * Call when need to indicate incoming call
     *
     * @param who      baseUser.toString() of caller
     * @param convInfo contains baseUser.toString() + "\n" + same for each user
     */

    public void showIncomingCall(String who, String convInfo) {
        EventQueue.invokeLater(() -> secondSkin.callIncomingDialog(who, convInfo));
    }

    /**
     * Dispatch the calling dialog and show message
     *
     * @param message to display
     */

    public void closeCall(String message) {
        EventQueue.invokeLater(() -> {
            secondSkin.closeCallDialog();
            JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Start displaying conversation pane
     *
     * @param user    with who
     * @param control for sound volume of the user
     */

    public void startConversation(String user, FloatControl control) {
        EventQueue.invokeLater(() -> secondSkin.conversationStart(user, control));
    }

    /**
     * Adds to the conversation a new user with volume control
     *
     * @param name    who to add
     * @param control sound volume
     */

    public void addToConv(String name, FloatControl control) {
        EventQueue.invokeLater(() -> secondSkin.addToConv(name, control));
    }

    /**
     * Remove from conversation
     *
     * @param name baseUser.toString();
     */

    public void removeFromConv(String name) {
        EventQueue.invokeLater(() -> secondSkin.removeFromConf(name));
    }

    /**
     * Close the conversation
     */

    public void closeConversation() {
        EventQueue.invokeLater(() -> secondSkin.stopConversation());
    }

    /**
     * Shows a message from conference
     *
     * @param message to show
     * @param from from who
     */

    public void showConferenceMessage(String message, String from){
        EventQueue.invokeLater(() -> secondSkin.showConferenceMessage(message, from));
    }

    @Override
    public void errorCase() {
        EventQueue.invokeLater(() -> {
            getContentPane().removeAll();
            add(firstSkin.getPane());
            JOptionPane.getRootFrame().dispose();
            JOptionPane.showMessageDialog(this,
                    "Disconnected from the server because of the connection failing",
                    "Error", JOptionPane.ERROR_MESSAGE);
            iterate();
            revalidate();
            repaint();
        });
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{secondSkin};
    }
}