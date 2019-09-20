package Com.GUI.Forms;

import Com.Pipeline.BUTTONS;
import Com.Pipeline.WarDuty;
import Com.Util.FormatWorker;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Properties;

/**
 * Dialog where you get info for server creating
 * Defines audio format stats
 * port for the socket
 */

public class AudioFormatStats {
    private JPanel mainPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton a8000RadioButton;
    private JRadioButton a16000RadioButton;
    private JRadioButton a44100RadioButton;
    private JRadioButton a48000RadioButton;
    private JRadioButton a8RadioButton;
    private JRadioButton a16RadioButton;
    private JFormattedTextField customRate;
    private JFormattedTextField textFieldPort;

//    /**
//     * Corresponding set of action
//     */
//
//    private final AudioFormatStatsActions actions;

//    /**
//     * Need its parent component to set in middle of it
//     * when shown
//     */
//
//    private final JComponent relativeTo;

    /**
     * Firstly load properties set ip sample rate fields
     * register listeners for buttons
     *
     */

    public AudioFormatStats(WarDuty whereReportAction) {
//        loadProperties();

//        this.actions = actions;
//        this.relativeTo = relativeTo;
//        setContentPane(mainPane);
//        setModal(true);
//        getRootPane().setDefaultButton(buttonOK);

        //set value of the button to customRate text field
        ActionListener actionListener = e -> {
            JRadioButton radioButton = (JRadioButton) e.getSource();
            customRate.setText(radioButton.getText());
        };
        a8000RadioButton.addActionListener(actionListener);
        a16000RadioButton.addActionListener(actionListener);
        a44100RadioButton.addActionListener(actionListener);
        a48000RadioButton.addActionListener(actionListener);

        buttonOK.addActionListener(e ->
                whereReportAction.fight(
                        BUTTONS.CREATE_SERVER,
                        new String[]{getPort(), getSampleRate(), getSampleSize()},
                        null,
                        -1
                ));

        buttonCancel.addActionListener(e ->
                whereReportAction.fight(
                        BUTTONS.CANCEL_SERVER_CREATION,
                        null,
                        null,
                        -1
                ));

        a44100RadioButton.setSelected(true);
        customRate.setText(a44100RadioButton.getText());
//        buttonOK.addActionListener(e -> {
//            actions.createServer().apply(new String[]{getPort(), getSampleRate(), getSampleSize()});
//            onOK();
//        });

//        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                onCancel();
//            }
//        });

        // call onCancel() on ESCAPE
//        mainPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

//        pack();
//        setTitle("Audio Format Settings");
    }

    public JPanel getMainPane() {
        return mainPane;
    }

    /**
     * Call it from some pane on action when needed
     */

    void display() {
//        setLocationRelativeTo(relativeTo);
//        setVisible(true);
    }

    private void onOK() {
//        dispose();
    }

    private void onCancel() {
//        dispose();
    }

    private String getPort() {
        return textFieldPort.getText().trim();
//        return textFieldPort.getText().matches("\\d+") ? textFieldPort.getText() : "8188";
    }

    private String getSampleRate() {
        return customRate.getText().trim();
//        return customRate.getText().matches("\\d+") ? customRate.getText() : "44100";
    }

    private String getSampleSize() {
        return a8RadioButton.isSelected() ? "8" : "16";
    }

//    /**
//     * Load some properties
//     */

//    private void loadProperties() {
//        Properties defaultStrings = MainFrame.defaultStrings;
//        textFieldPort.setText(defaultStrings.getProperty("port"));
//        customRate.setText(defaultStrings.getProperty("rate"));
//    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        /*
        NumberFormat.getFormat() not good enough
        because when you call get text return string
        but if the string is more than 3 characters it will contain a
        character that not space but space so it ruin call Integer.valueOf(String s)
        I do not know how to remove it even regular expression removal
        with \\s doesn't work
        It may be problem with string pool but i am not good enough to see it
         */
        customRate = new JFormattedTextField(FormatWorker.getFormatter());
        textFieldPort = new JFormattedTextField(FormatWorker.getFormatter());
    }

}
