package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.AudioFormatStatsActions;

import javax.swing.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.function.Consumer;

public class AudioFormatStats extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton a8000RadioButton;
    private JRadioButton a16000RadioButton;
    private JRadioButton a44100RadioButton;
    private JRadioButton a48000RadioButton;
    private JRadioButton a8RadioButton;
    private JRadioButton a16RadioButton;
    private JTextField customRate;
    private JTextField textFieldPort;

    private AudioFormatStatsActions actions;

    public AudioFormatStats(Consumer<String[]> createServer) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        ActionListener actionListener = e -> customRate.setText(((JRadioButton) e.getSource()).getText());
        a8000RadioButton.addActionListener(actionListener);
        a16000RadioButton.addActionListener(actionListener);
        a44100RadioButton.addActionListener(actionListener);
        a48000RadioButton.addActionListener(actionListener);

        buttonOK.addActionListener(e -> {
            createServer.accept(new String[]{getPort(), getSampleRate(), getSampleSize()});
            onOK();
        });

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setTitle("Audio Format Settings");
    }

    public AudioFormatStats(AudioFormatStatsActions actions) {
        this.actions = actions;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        ActionListener actionListener = e -> customRate.setText(((JRadioButton) e.getSource()).getText());
        a8000RadioButton.addActionListener(actionListener);
        a16000RadioButton.addActionListener(actionListener);
        a44100RadioButton.addActionListener(actionListener);
        a48000RadioButton.addActionListener(actionListener);

        buttonOK.addActionListener(e -> {
            try {
                actions.createServer().apply(new String[]{getPort(), getSampleRate(), getSampleSize()});
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }finally {
                onOK();
            }
        });

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setTitle("Audio Format Settings");
    }

    protected void display(){
        setLocationRelativeTo(getRootPane());
        setVisible(true);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private String getPort(){
        return textFieldPort.getText().matches("\\d+") ? textFieldPort.getText() : "8188";
    }

    private String getSampleRate(){
        return customRate.getText().matches("\\d+") ? customRate.getText() : "44100";
    }

    private String getSampleSize(){
        return a8RadioButton.isSelected() ? "8" : "16";
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        customRate = new JFormattedTextField(NumberFormat.getInstance());
        textFieldPort = new JFormattedTextField(NumberFormat.getInstance());
    }

}
