package Bin.GUI.Forms;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.GUI.Interfaces.FirstSkinActions;

import javax.swing.*;
import java.text.ParseException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * You will see it first
 * Handle connect to a server
 * and call some thing to create a server
 * Have some parameters in properties
 */

class FirstSkin {
    private JTextField nameField;
    private JTextField ipField;
    private JFormattedTextField portField;
    private JButton connectButton;
    private JButton createButton;
    private JPanel mainPane;

    /**
     * Needs for set setting for server creation
     * Lazy initialised
     */

    private AudioFormatStats audioFormatStats;

    /**
     * Available actions
     */

    private FirstSkinActions actions;

    /**
     * Firstly load properties such as
     * port, name, ip
     * than creates
     *
     * @param actions your abilities
     */

    FirstSkin(FirstSkinActions actions) {
        loadProperties();

        this.actions = actions;

        /*
        make lazy initialisation in case if you are only a client
         */
//        audioFormatStats = new AudioFormatStats(actions, mainPane);

        connectButton.addActionListener(e -> {
            try {
                actions.connect().apply(new String[]{getMyName(), getIp(), getPort()});
            } catch (NotInitialisedException e1) {
                e1.printStackTrace();
            }
        });

        createButton.addActionListener(e -> createServer());

    }

    /**
     * Lazy initialisation
     * and display server creation dialog
     */

    private void createServer() {
        if (audioFormatStats == null) {
            audioFormatStats = new AudioFormatStats(actions, mainPane);
        }
        audioFormatStats.display();
    }

    JPanel getPane() {
        return mainPane;
    }

    /**
     * Gets name from the field or get system user name name
     * I hope it will work properly, because problems with encoding
     * with security and OS
     *
     * @return name
     */

    private String getMyName() {
        return nameField.getText().equals("") ? System.getProperty("user.name").trim() : nameField.getText().trim();
    }

    /**
     * Test if ip is acceptable if not return localhost
     *
     * @return ip
     */

    private String getIp() {
        String ip = ipField.getText();
        return verifyIp(ip) ? ip : "127.0.0.1";
    }

    /**
     * Test if is acceptable
     *
     * @return port or default
     */

    private String getPort() {
        String port = portField.getText();
        return verifyPort(port) ? port : "8188";
    }

    private static boolean verifyPort(String port) {
        return port.matches("\\d+?");
    }

    /**
     * Regular expression is a POWER
     * mean ((1-3 digits).) 3 times and then just (1-3 digits)
     *
     * @param ip to verify
     * @return true if has an appropriate format
     */

    private static boolean verifyIp(String ip) {
        String digitAndDotThenDigits = "((\\d){1,3}\\.){3}(\\d{1,3})";
        return Pattern.compile(digitAndDotThenDigits).matcher(ip.trim()).matches();
    }

    /**
     * For making button disabled or enabled when connecting
     */

    void blockConnectButton() {
        connectButton.setEnabled(false);
    }

    void releaseConnectButton() {
        connectButton.setEnabled(true);
    }

    /**
     * Needs for JFormattedTextField NumberFormatter.getFormat()
     * not appropriate because of @see AudioFormatStats at the end
     *
     * @return formatter for only digits
     */

    static JFormattedTextField.AbstractFormatterFactory getFormatter() {
        return new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                return new JFormattedTextField.AbstractFormatter() {
                    @Override
                    public Object stringToValue(String text) throws ParseException {
                        String result = text.trim();
                        result = result.replaceAll("\\D", "");
                        return result.equals("") ? "" : Integer.parseInt(result);
                    }

                    @Override
                    public String valueToString(Object value) throws ParseException {
                        return String.valueOf(value);
                    }
                };
            }
        };
    }

    /**
     * Gets some properties
     */

    private void loadProperties() {
        Properties defaultStrings = MainFrame.defaultStrings;
        portField.setText(defaultStrings.getProperty("port"));
        ipField.setText(defaultStrings.getProperty("ip"));
        nameField.setText(defaultStrings.getProperty("name"));
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        portField = new JFormattedTextField(getFormatter());
    }
}
