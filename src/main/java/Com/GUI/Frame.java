package Com.GUI;

import Com.Audio.AudioSupplier;
import Com.GUI.Forms.ActionHolder.GUIActions;
import Com.GUI.Forms.ActionHolder.GUIDuty;
import Com.GUI.Forms.AudioFormatStats;
import Com.GUI.Forms.CallDialog;
import Com.GUI.Forms.EntrancePane;
import Com.GUI.Forms.MultiplePurposePane;
import Com.Model.Registration;
import Com.Model.UnEditableModel;
import Com.Networking.Utility.BaseUser;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.BUTTONS;
import Com.Pipeline.UpdaterAndHandler;
import Com.Util.Resources;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Frame implements UpdaterAndHandler, Registration<ActionableLogic>, ActionableLogic, GUIDuty {

    public static final int WIDTH = 600;
    public static final int HEIGHT = 450;


    private final List<ActionableLogic> actionableLogicList;

    private final JFrame frame;
    private final EntrancePane entrancePane;
    private final AudioFormatStats serverCreatePane;
    private final MultiplePurposePane purposePane;
    private final CallDialog callDialog;

    public Frame() {
        actionableLogicList = new ArrayList<>();

        frame = new JFrame("Skype Bass"); // take it from property

        entrancePane = new EntrancePane(this, this);
        serverCreatePane = new AudioFormatStats(this, this);
        purposePane = new MultiplePurposePane(this);
        callDialog = new CallDialog(this);

        setSize();
        setIcon();

        frame.setContentPane(entrancePane.getPane());
//        frame.setJMenuBar(produceMenuBar(this));
        frame.setVisible(true);
    }

    @Override
    public void update(UnEditableModel model) {
        SwingUtilities.invokeLater(() -> {
            purposePane.update(model);
        });
    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
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
                }
                case DISCONNECTED: {
                    onDisconnect();
                    break;
                }
                case OUT_CALL: {
                    onOutCall(from);
                    break;
                }
                case INCOMING_CALL: {
                    onIncomingCall(from, stringData);
                    break;
                }
                case CALL_DENIED: {
                    showMessage("Call was denied by " + from.toString());
                    break;
                }
                case CALL_CANCELLED: {
                    showMessage("Call was cancelled by " + from.toString());
                    break;
                }
                case CONNECTION_TO_SERVER_FAILED: {
//                    onDisconnect();
                    showInfoMessage("Disconnected from server!");
                    break;
                }
            }

            entrancePane.handle(action, from, stringData, bytesData, intData);
            purposePane.handle(action, from, stringData, bytesData, intData);
            callDialog.handle(action, from, stringData, bytesData, intData);
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
        actionableLogicList.forEach(warDuty -> warDuty.act(button, plainData, stringData, integerData));
    }

    @Override
    public void displayChanges(GUIActions action, Object data) {
        switch (action) {
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
        frame.setIconImage(Resources.ricardo.getImage());
    }

    private JMenuBar produceMenuBar(ActionableLogic registration) {
        JMenuBar menuBar = new JMenuBar();
        JMenu speakerMenu = new JMenu("Speaker");
        JMenu micMenu = new JMenu("Microphone");
        fillMenu(
                speakerMenu,
                AudioSupplier.getSourceLines(),
                info -> registration.act(
                        BUTTONS.CHANGE_OUTPUT,
                        info,
                        null,
                        -1
                ),
                AudioSupplier.getDefaultForOutput()
        );

        fillMenu(
                micMenu,
                AudioSupplier.getTargetLines(),
                info -> registration.act(
                        BUTTONS.CHANGE_INPUT,
                        info,
                        null,
                        -1
                ),
                AudioSupplier.getDefaultForInput()
        );

        menuBar.add(speakerMenu);
        menuBar.add(micMenu);
        return menuBar;
    }

    private <T> void fillMenu(JMenu menu, List<T> data, Consumer<T> action, T defaultForSelect) {
        ButtonGroup group = new ButtonGroup();
        for (T datum : data) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(datum.toString());
            button.addActionListener(e -> action.accept(datum));
            menu.add(button);
            group.add(button);
            if (datum.equals(defaultForSelect))
                button.setSelected(true);
        }
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
        frame.setContentPane(serverCreatePane.getMainPane());
        repaint();
    }

    private void onServerCancel() {
        frame.setContentPane(entrancePane.getPane());
        repaint();
    }

    private void onServerCreated() {
        onServerCancel();
    }

    private void onConnected() {
        frame.setContentPane(purposePane.getPane());
        frame.setJMenuBar(produceMenuBar(this));
        repaint();
    }

    private void onDisconnect() {
        frame.setContentPane(entrancePane.getPane());
        frame.setJMenuBar(null);
        repaint();
    }

    private void onOutCall(BaseUser who) {
        callDialog.showOutcoming(who, frame.getRootPane());
    }

    private void onIncomingCall(BaseUser who, String dudes) {
        callDialog.showIncoming(who, dudes, frame.getRootPane());
    }

    private void repaint() {
        frame.revalidate();
        frame.repaint();
    }
}
