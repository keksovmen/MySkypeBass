package Bin.GUI.Forms;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class FirstSkin{
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton createButton;
    private JPanel mainPane;

    private AudioFormatStats audioFormatStats;

    public FirstSkin(Consumer<String[]> connect, Consumer<String[]> createServer) {

        audioFormatStats = new AudioFormatStats(createServer);

        connectButton.addActionListener(e -> connect.accept(new String[]{getMyName(), getIp(), getPort()}));

        createButton.addActionListener(e -> audioFormatStats.display());



    }

    JPanel getPane() {
        return mainPane;
    }

    private String getMyName(){
        return nameField.getText().equals("") ? System.getProperty("user.name").trim() : nameField.getText().trim();
    }

    private String getIp(){
        String ip = ipField.getText();
        return verifyIp(ip) ? ip : "127.0.0.1";
    }

    private String getPort(){
        String port = portField.getText();
        return verifyPort(port) ? port : "8188";
    }

    public static boolean verifyPort(String port){
        return port.matches("\\d+?");
    }

    public static boolean verifyIp(String ip){
        String digitAndDotThenDigits = "((\\d){1,3}\\.){3}(\\d{1,3})";
        return Pattern.compile(digitAndDotThenDigits).matcher(ip.trim()).matches();
    }

}
