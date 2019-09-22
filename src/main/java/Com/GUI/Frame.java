package Com.GUI;

import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.GUI.Forms.AudioFormatStats;
import Com.GUI.Forms.EntrancePane;
import Com.GUI.Forms.MultiplePurposePane;
import Com.Model.Registration;
import Com.Model.UnEditableModel;
import Com.Model.Updater;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.ResponsibleGUI;
import Com.Pipeline.ActionableLogic;
import Com.Util.Resources;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Frame implements Updater, ResponsibleGUI, Registration<ActionableLogic>, ActionableLogic, GUIDuty {

    public static final int WIDTH = 500;
    public static final int HEIGHT = 300;


    private final List<ActionableLogic> actionableLogicList;

    private final JFrame frame;
    private final EntrancePane entrancePane;
    private final AudioFormatStats serverCreatePane;
    private final MultiplePurposePane purposePane;

    public Frame() {
        actionableLogicList = new ArrayList<>();

        frame = new JFrame("Skype Bass"); // take it from property

        entrancePane = new EntrancePane(this, this);
        serverCreatePane = new AudioFormatStats(this, this);
        purposePane = new MultiplePurposePane(this);

        setSize();
        setIcon();

        frame.setContentPane(entrancePane.getPane());

        frame.setVisible(true);
    }

    @Override
    public void update(UnEditableModel model) {
        SwingUtilities.invokeLater(() -> {
            purposePane.update(model);
        });
    }

    @Override
    public void respond(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        SwingUtilities.invokeLater(() -> { // there will be others thread not swing
            switch (action) {
                case CONNECT_FAILED: {
                    showErrorMessage("Not connected, check port or host name or internet connection");
                    break;
                }
                case AUDIO_FORMAT_NOT_ACCEPTED: {
                    showErrorMessage(
                            "Not connected, because you audio system can't handle this format {" +
                                    " " + stringData + " }");
                    break;
                }
                case WRONG_HOST_NAME_FORMAT: {
                    showInfoMessage("Wrong host name format - " + stringData);
                    break;
                }
                case WRONG_PORT_FORMAT: {
                    showInfoMessage("Wrong port format - " + stringData);
                    break;
                }
                case CONNECT_SUCCEEDED: {
//                    showMessage("Connected!");
                    //Change pane to new one
                    onConnected();
                    break;
                }
                case WRONG_SAMPLE_RATE_FORMAT: {
                    showInfoMessage("Wrong sample rate format - " + stringData);
                    break;
                }
                case WRONG_SAMPLE_SIZE_FORMAT: {
                    showInfoMessage("Wrong sample size format - " + stringData);
                    break;
                }
                case SERVER_CREATED: {
                    onServerCreated();
                    break;
                }
                case PORT_ALREADY_BUSY: {
                    showErrorMessage("Port already in use - " + stringData);
                    break;
                }
                case PORT_OUT_OF_RANGE: {
                    showErrorMessage("Port is out of range, must be in "
                            + stringData + ". But yours is " + intData);
                    break;
                }case DISCONNECTED:{
                    onDisconnect();
                    break;
                }
            }

            entrancePane.respond(action, from, stringData, bytesData, intData);
            purposePane.respond(action, from, stringData, bytesData, intData);
        });
    }

    @Override
    public boolean registerListener(ActionableLogic listener) {
        return actionableLogicList.add(listener);
    }

    @Override
    public boolean removeListener(ActionableLogic listener) {
        return actionableLogicList.remove(listener);
    }

    @Override
    public void act(BUTTONS button, Object plainData, String stringData, int integerData) {
//        switch (button) { //handle panel changes
//        }
        actionableLogicList.forEach(warDuty -> warDuty.act(button, plainData, stringData, integerData));
    }

    @Override
    public void displayChanges(GUIActions action, Object data) {
        switch (action){
            case CREATE_SERVER_PANE: {
                onServerPaneCreate();
                return;
            }
            case CANCEL_SERVER_CREATION: {
                onServerCancel();
                return;
            }
        }
    }

    private void setSize() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(
                screenSize.width / 2 - WIDTH / 2,
                screenSize.height / 2 - HEIGHT / 2,
                WIDTH,
                HEIGHT);
    }

    private void setIcon() {
//        try {
//            InputStream ricardo = getClass().getResourceAsStream("/Images/ricardo.png");
//            if (ricardo == null) {
//                JOptionPane.showMessageDialog(frame, "Can't find the icon");
//                return;
//            }
//            frame.setIconImage(ImageIO.read(ricardo));
        frame.setIconImage(Resources.ricardo.getImage());
//        } catch (IOException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(frame, "Can't set the icon");
//        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Message",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void onServerPaneCreate() {
//        frame.remove(frame.getContentPane());
//        removeContentPane();
        frame.setContentPane(serverCreatePane.getMainPane());
        repaint();
    }

    private void onServerCancel() {
//        frame.remove(serverCreatePane.getMainPane());
//        frame.remove(frame.getContentPane());
//        removeContentPane();
        frame.setContentPane(entrancePane.getPane());
        repaint();
    }

    private void onServerCreated() {
        onServerCancel();
    }

    private void onConnected() {
//        frame.getContentPane().removeAll();
        frame.setContentPane(purposePane.getPane());
        repaint();
    }

    private void onDisconnect(){
        frame.setContentPane(entrancePane.getPane());
        repaint();
    }

    private void repaint() {
        frame.revalidate();
        frame.repaint();
    }
}
