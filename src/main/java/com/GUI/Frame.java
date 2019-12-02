package com.GUI;

import com.Audio.AudioSupplier;
import com.Client.ButtonsHandler;
import com.GUI.Forms.AudioFormatStats;
import com.GUI.Forms.CallDialog;
import com.GUI.Forms.EntrancePane;
import com.GUI.Forms.MultiplePurposePane;
import com.Model.UnEditableModel;
import com.Pipeline.ACTIONS;
import com.Pipeline.BUTTONS;
import com.Pipeline.CompositeComponent;
import com.Util.Resources;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Frame implements CompositeComponent {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;


    private final List<ButtonsHandler> buttonsHandlers;

    private final JFrame frame;
    private final EntrancePane entrancePane;
    private final AudioFormatStats serverCreatePane;
    private final MultiplePurposePane purposePane;
    private final CallDialog callDialog;

    public Frame() {
        buttonsHandlers = new ArrayList<>();

        frame = new JFrame("Skype Bass"); // take it from property

        entrancePane = new EntrancePane(this, this::onServerPaneCreate);
        serverCreatePane = new AudioFormatStats(this, this::onServerCancel);
        purposePane = new MultiplePurposePane(this);
        callDialog = new CallDialog(this, frame.getRootPane());

        setSize();
        setIcon();

        frame.setContentPane(entrancePane.getPane());
        frame.setVisible(true);
    }

    @Override
    public void update(UnEditableModel model) {
        SwingUtilities.invokeLater(() -> purposePane.update(model));
    }

    @Override
    public void observe(ACTIONS action, Object[] data) {
        SwingUtilities.invokeLater(() -> { // there will be others thread not swing
            switch (action) {
                case CONNECT_FAILED: {
                    showErrorMessage("Not connected, check port or host name or internet connection");
                    break;
                }
                case AUDIO_FORMAT_NOT_ACCEPTED: {
                    showErrorMessage(
                            "Not connected, because you audio system can't handleRequest this format {" +
                                    " " + data[0] + " }");
                    break;
                }
                case WRONG_HOST_NAME_FORMAT: {
                    showInfoMessage("Wrong host name format - " + data[0]);
                    break;
                }
                case WRONG_PORT_FORMAT: {
                    showInfoMessage("Wrong port format - " + data[0]);
                    break;
                }
                case CONNECT_SUCCEEDED: {
                    onConnected();
                    break;
                }
                case WRONG_SAMPLE_RATE_FORMAT: {
                    showInfoMessage("Wrong sample rate format - " + data[0]);
                    break;
                }
                case WRONG_SAMPLE_SIZE_FORMAT: {
                    showInfoMessage("Wrong sample size format - " + data[0]);
                    break;
                }
                case SERVER_CREATED: {
                    onServerCreated();
                    break;
                }
                case PORT_ALREADY_BUSY: {
                    showErrorMessage("Port already in use - " + data[0]);
                    break;
                }
                case PORT_OUT_OF_RANGE: {
                    showErrorMessage("Port is out of range, must be in "
                            + data[0] + ". But yours is " + data[1]);
                    break;
                }
                case DISCONNECTED: {
                    onDisconnect();
                    break;
                }
                case CALL_DENIED: {
                    showMessage("Call was denied by " + data[0]);
                    break;
                }
                case CALL_CANCELLED: {
                    showMessage("Call was cancelled by " + data[0]);
                    break;
                }
                case CONNECTION_TO_SERVER_FAILED: {
                    showInfoMessage("Disconnected from server!");
                    break;
                }
                case INVALID_AUDIO_FORMAT: {
                    showErrorMessage((String) data[0]);
                    break;
                }
                case SERVER_CREATED_ALREADY:{
                    showInfoMessage("Server already created!");
                    break;
                }
            }

            entrancePane.observe(action, data);
            purposePane.observe(action, data);
            callDialog.observe(action, data);
        });
    }

    @Override
    public void attach(ButtonsHandler listener) {
        buttonsHandlers.add(listener);
    }

    @Override
    public void detach(ButtonsHandler listener) {
        buttonsHandlers.remove(listener);
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        buttonsHandlers.forEach(buttonsHandler -> buttonsHandler.handleRequest(button, data));
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
        frame.setIconImage(Resources.getMainIcon().getImage());
    }

    private JMenuBar produceMenuBar(ButtonsHandler registration) {
        JMenuBar menuBar = new JMenuBar();
        JMenu speakerMenu = new JMenu("Speaker");
        JMenu micMenu = new JMenu("Microphone");
        fillMenu(
                speakerMenu,
                AudioSupplier.getInstance().getSourceLines(),
                info -> registration.handleRequest(
                        BUTTONS.CHANGE_OUTPUT,
                        new Object[]{info}
                ),
                AudioSupplier.getInstance().getDefaultForOutput()
        );

        fillMenu(
                micMenu,
                AudioSupplier.getInstance().getTargetLines(),
                info -> registration.handleRequest(
                        BUTTONS.CHANGE_INPUT,
                        new Object[]{info}
                ),
                AudioSupplier.getInstance().getDefaultForInput()
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

//    private void onOutCall(BaseUser who) {
//        callDialog.showOutcoming(who, frame.getRootPane());
//    }
//
//    private void onIncomingCall(BaseUser who, String dudes) {
//        callDialog.showIncoming(who, dudes, frame.getRootPane());
//    }

    private void repaint() {
        frame.revalidate();
        frame.repaint();
    }
}
