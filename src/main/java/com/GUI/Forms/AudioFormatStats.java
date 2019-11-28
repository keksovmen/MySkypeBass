package com.GUI.Forms;

import com.Client.ButtonsHandler;
import com.Pipeline.BUTTONS;
import com.Util.FormatWorker;
import com.Util.Resources;

import javax.swing.*;
import java.awt.event.ActionListener;

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

    /**
     * Firstly load properties set ip sample rate fields
     * register listeners for buttons
     */

    public AudioFormatStats(ButtonsHandler whereToReportActions, Runnable cancelServerCreation) {
        ActionListener actionListener = e -> {
            JRadioButton radioButton = (JRadioButton) e.getSource();
            customRate.setText(radioButton.getText());
        };
        a8000RadioButton.addActionListener(actionListener);
        a16000RadioButton.addActionListener(actionListener);
        a44100RadioButton.addActionListener(actionListener);
        a48000RadioButton.addActionListener(actionListener);

        buttonOK.addActionListener(e -> onOK(whereToReportActions));

        buttonCancel.addActionListener(e -> onCancel(cancelServerCreation));

        a44100RadioButton.setSelected(true);

        customRate.setText(Resources.getDefaultRate());
        textFieldPort.setText(Resources.getDefaultPort());

    }

    public JPanel getMainPane() {
        return mainPane;
    }

    private void onOK(ButtonsHandler whereToReportActions) {
        whereToReportActions.handleRequest(
                BUTTONS.CREATE_SERVER,
                new Object[]{getPort(), getSampleRate(), getSampleSize()}
        );
    }

    private void onCancel(Runnable cancelServerCreation) {
        cancelServerCreation.run();
    }

    private String getPort() {
        return textFieldPort.getText().trim();
    }

    private String getSampleRate() {
        return customRate.getText().trim();
    }

    private String getSampleSize() {
        return a8RadioButton.isSelected() ? "8" : "16";
    }

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
