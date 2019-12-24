package com.Implementation.GUI;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Util.Resources.Resources;
import com.Implementation.GUI.Forms.AudioFormatStats;
import com.Implementation.GUI.Forms.CallDialog;
import com.Implementation.GUI.Forms.EntrancePane;
import com.Implementation.GUI.Forms.MultiplePurposePane;
import com.Implementation.Util.DesktopResources;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

//    private JDialog infoDialog;

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
    public void modelObservation(UnEditableModel model) {
        SwingUtilities.invokeLater(() -> purposePane.modelObservation(model));
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
                            "Not connected, because your audio system can't handleDataPackageRouting this format {" +
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
                case SERVER_CREATED_ALREADY: {
                    showInfoMessage("Server already created!");
                    break;
                }
                case INCOMING_CALL: {
                    //need to remove Joption pane dialog cause blocking call dialog

                    break;
                }
            }

            callDialog.observe(action, data);
            entrancePane.observe(action, data);
            purposePane.observe(action, data);
//            callDialog.observe(action, data);
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
        frame.setIconImage(new ImageIcon(((DesktopResources) Resources.getInstance()).getMainIcon()).getImage());
    }

    private JMenuBar produceMenuBar(ButtonsHandler registration) {
        JMenuBar menuBar = new JMenuBar();
        JMenu speakerMenu = new JMenu("Speaker");
        JMenu micMenu = new JMenu("Microphone");
        fillMenu(
                speakerMenu,
                AudioSupplier.getInstance().getOutputLines(),
                info -> registration.handleRequest(
                        BUTTONS.CHANGE_OUTPUT,
                        new Object[]{info}
                ),
                AudioSupplier.getInstance().getDefaultForOutput()
        );

        fillMenu(
                micMenu,
                AudioSupplier.getInstance().getInputLines(),
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

    private <T> void fillMenu(JMenu menu, Map<T, String> data, Consumer<T> action, T defaultForSelect) {
        ButtonGroup group = new ButtonGroup();
        data.forEach((t, s) -> {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(s);
            button.addActionListener(e -> action.accept(t));
            menu.add(button);
            group.add(button);
            if (t.equals(defaultForSelect))
                button.setSelected(true);
        });
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

    /**
     * Need this little different because
     * When you get called and dude cancelled
     * it will display it, but if ModalityType is one (always focus on me)
     * you cant press anywhere and then you receive another call but it's buttons dos't work
     * simply can't be pressed except for one in focus can be handled with keyboard but mouse doesn't work on them
     * focking jedies!
     *
     * @param message to display
     */

    private void showMessage(String message) {
        JOptionPane jOptionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = jOptionPane.createDialog(frame,"Message");

        //Must be this one for less bags
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setVisible(true);
//        JOptionPane.showMessageDialog(
//                frame,
//                message,
//                "Message",
//                JOptionPane.PLAIN_MESSAGE
//        );
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

    private void repaint() {
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Needs for JFormattedTextField NumberFormatter.getFormat()
     * not appropriate because of @see AudioFormatStats at the end
     *
     * @return formatter for only digits
     */

    public static JFormattedTextField.AbstractFormatterFactory getFormatter() {
        return new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                return new JFormattedTextField.AbstractFormatter() {
                    private final int MAX_LENGTH_AS_STRING =
                            String.valueOf(Integer.MAX_VALUE).length() - 1; //-1 just in case ov largest value

                    @Override
                    public Object stringToValue(String text) {
                        String result = text.trim();
                        result = result.replaceAll("\\D", "");
                        int difference = result.length() - MAX_LENGTH_AS_STRING;
                        if (0 < difference)
                            result = result.substring(0, result.length() - difference);
                        return result.equals("") ? "" : Integer.parseInt(result);
                    }

                    @Override
                    public String valueToString(Object value) {
                        return String.valueOf(value);
                    }
                };
            }
        };
    }
}
