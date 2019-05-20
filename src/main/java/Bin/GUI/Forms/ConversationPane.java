package Bin.GUI.Forms;

import Bin.Audio.AudioClient;
import Bin.Expendable;
import Bin.GUI.Main;
import Bin.Utility.BaseUser;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class ConversationPane implements Expendable {

    private Map<Integer, JPanel> usersPane;
    private ConferencePane conferencePane;
    private int offset;

    public ConversationPane() {
        conferencePane = new ConferencePane();
        usersPane = new HashMap<>(3);
    }

    private JPanel createUser(BaseUser clientUser){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;

        JPanel jPanel = new JPanel(layout);
        JLabel label = new JLabel(clientUser.toString());

        FloatControl control = AudioClient.getInstance().getSettings(clientUser.getId());
        int def = (int) control.getValue();
        JSlider jSlider = new JSlider((int) control.getMinimum(), (int) control.getMaximum(), (int) control.getValue());
        jSlider.addChangeListener(e -> control.setValue(jSlider.getValue()));
        jSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    jSlider.setValue(def);
            }
        });

        jSlider.setMajorTickSpacing(10);
        jSlider.setMinorTickSpacing(5);
        jSlider.setPaintTicks(true);
        jSlider.setPreferredSize(new Dimension(140, 30));


        jPanel.add(label, constraints);

        constraints.gridy = 1;
        jPanel.add(jSlider, constraints);

        usersPane.put(clientUser.getId(), jPanel);
        return jPanel;
    }

    @Override
    public boolean add(int id) {
        EventQueue.invokeLater(() -> {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridy = offset++;
            conferencePane.getCenterPane().add(createUser(Main.getInstance().getUserById(id)), constraints);

        });
        return true;
    }

    @Override
    public void remove(int id) {

    }

    @Override
    public void close() {

    }

    JPanel getMainPane() {
        return conferencePane.getMainPane();
    }
}
