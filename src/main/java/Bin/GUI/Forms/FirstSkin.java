package Bin.GUI.Forms;

import Bin.GUI.Main;

import javax.swing.*;
import java.util.regex.Pattern;

public class FirstSkin{
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton createButton;
    private JPanel pane;

    public FirstSkin(MainFrame mainFrame) {

        connectButton.addActionListener(e -> connect(mainFrame));

        createButton.addActionListener(e -> create(mainFrame));

    }

    JPanel getPane() {
        return pane;
    }

    private boolean verifyPort(){
        return portField.getText().matches("\\d+?");
    }

    private boolean verifyIp(String ip){
        String digitAndDotThenDigits = "((\\d){1,3}\\.){3}(\\d{1,3})";
        return Pattern.compile(digitAndDotThenDigits).matcher(ip.trim()).matches();
    }

    String getMyName(){
        return nameField.getText().equals("") ? System.getProperty("user.name").trim() : nameField.getText().trim();
    }

    private void connect(MainFrame mainFrame){
        if (verifyIp(ipField.getText()) && verifyPort()){
            if (!Main.getInstance().connect(getMyName(), ipField.getText(), Integer.parseInt(portField.getText()))){
                mainFrame.showDialog("Cannot connect to the server, check ip and port correctly, or server is shut down");
            }
        }else
            mainFrame.showDialog("Ip should be like xxx.xxx.xxx.xxx\nPort should contain only digits");
    }

    private void create(MainFrame mainFrame){
        if (verifyPort()){
            AudioFormatStats audioFormatStats = new AudioFormatStats(mainFrame);
            if (audioFormatStats.isStarted()){
                if (!Main.getInstance().startServer(Integer.parseInt(portField.getText()), audioFormatStats.sampleRate, audioFormatStats.sampleSize)) {
                    mainFrame.showDialog("Server has not started, check if the port is not already on use, or out of the available range");
                }else {
                    createButton.setVisible(false);
                }
            }
        }else Main.getInstance().getMainFrame().showDialog("Port is invalid should contain only digits");
    }

}
