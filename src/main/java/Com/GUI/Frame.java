package Com.GUI;

import Com.GUI.Forms.AudioFormatStats;
import Com.GUI.Forms.EntrancePane;
import Com.Model.Registration;
import Com.Model.UnEditableModel;
import Com.Model.Updater;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.CivilDuty;
import Com.Pipeline.WarDuty;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Frame implements Updater, CivilDuty, Registration<WarDuty>, WarDuty {

    private final List<WarDuty> warDutyList;

    private final JFrame frame;
    private final EntrancePane entrancePane;
    private final AudioFormatStats serverCreatePane;

    public Frame() {
        warDutyList = new ArrayList<>();

        frame = new JFrame("Skype Bass");

        entrancePane = new EntrancePane(this);
        serverCreatePane = new AudioFormatStats(this);

        setSize();
        setIcon();

        frame.setContentPane(entrancePane.getPane());

        frame.setVisible(true);
    }

    @Override
    public void update(UnEditableModel model) {

    }

    @Override
    public void respond(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        SwingUtilities.invokeLater(() -> { // there will be others thread not swing

            switch (action) {
                case CONNECT_FAILED: {
                    showErrorMessage("Not connected, check port or host name or internet connection");
                    break;
                }
                case AUDIO_FORMAT_NOT_ACCEPTED:{
                    showErrorMessage(
                            "Not connected, because you audio system can't handle this format {" +
                                    " " + stringData + " }");
                    break;
                }
                case WRONG_HOST_NAME_FORMAT:{
                    showInfoMessage("Wrong host name format - " + stringData);
                    break;
                }
                case WRONG_PORT_FORMAT:{
                    showInfoMessage("Wrong port format - " + stringData);
                    break;
                }
                case CONNECT_SUCCEEDED:{
                    showMessage("Connected!");
                    //Change pane to new one
                    break;
                }
                case WRONG_SAMPLE_RATE_FORMAT:{
                    showInfoMessage("Wrong sample rate format - " + stringData);
                    break;
                }
                case WRONG_SAMPLE_SIZE_FORMAT:{
                    showInfoMessage("Wrong sample size format - " + stringData);
                    break;
                }
                case SERVER_CREATED:{
                    onServerCancel();
                    break;
                }
            }

            entrancePane.respond(action, from, stringData, bytesData, intData);
        });
    }

    @Override
    public boolean registerListener(WarDuty listener) {
        return warDutyList.add(listener);
    }

    @Override
    public boolean removeListener(WarDuty listener) {
        return warDutyList.remove(listener);
    }

    @Override
    public void fight(BUTTONS button, Object plainData, String stringData, int integerData) {
        switch (button){ //handle panel changes
            case CREATE_SERVER_PANE:{
                onServerPaneCreate();
                return;
            }
            case CANCEL_SERVER_CREATION:{
                onServerCancel();
                return;
            }
            case CONNECT:{

            }
        }
        warDutyList.forEach(warDuty -> warDuty.fight(button, plainData, stringData, integerData));
    }

    private void setSize() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(
                screenSize.width / 2 - 200,
                screenSize.height / 2 - 150,
                400,
                300);
    }

    private void setIcon() {
        try {
            InputStream ricardo = getClass().getResourceAsStream("/Images/ricardo.png");
            if (ricardo == null) {
                JOptionPane.showMessageDialog(frame, "Can't find the icon");
                return;
            }
            frame.setIconImage(ImageIO.read(ricardo));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Can't set the icon");
        }
    }

    private void showErrorMessage(String message){
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
                );
    }

    private void showInfoMessage(String message){
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showMessage(String message){
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Message",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void onServerPaneCreate(){
        frame.remove(entrancePane.getPane());
        frame.setContentPane(serverCreatePane.getMainPane());
        repaint();
    }

    private void onServerCancel(){
        frame.remove(serverCreatePane.getMainPane());
        frame.setContentPane(entrancePane.getPane());
        repaint();
    }

    private void onServerCreated(){
        onServerCancel();
    }

    private void repaint(){
        frame.revalidate();
        frame.repaint();
    }
}
