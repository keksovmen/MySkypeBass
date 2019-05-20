package Bin.GUI.Forms;

import javax.swing.*;
import java.awt.event.*;

public class AudioFormatStats extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox a8000CheckBox;
    private JCheckBox a16000CheckBox;
    private JCheckBox a44100CheckBox;
    private JCheckBox a48000CheckBox;
    private JCheckBox a8CheckBox;
    private JCheckBox a16CheckBox;

    int sampleRate;
    int sampleSize;
    private boolean started;

    public AudioFormatStats(JFrame parent) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setTitle("Audio Format Settings");
        setIconImage(parent.getIconImage());
        setLocation(parent.getX() + 30, parent.getY() + 30);
        setVisible(true);
    }

    private void onOK() {
        // add your code here
        started = true;
        if (a8000CheckBox.isSelected())
            sampleRate = Integer.parseInt(a8000CheckBox.getText());
        else if (a16000CheckBox.isSelected())
            sampleRate = Integer.parseInt(a16000CheckBox.getText());
        else if (a44100CheckBox.isSelected())
            sampleRate = Integer.parseInt(a44100CheckBox.getText());
        else
            sampleRate = Integer.parseInt(a48000CheckBox.getText());

        if (a8CheckBox.isSelected())
            sampleSize = Integer.parseInt(a8CheckBox.getText());
        else
            sampleSize = Integer.parseInt(a16CheckBox.getText());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

//    public static void main(String[] args) {
//        AudioFormatStats dialog = new AudioFormatStats();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    public boolean isStarted() {
        return started;
    }
}
