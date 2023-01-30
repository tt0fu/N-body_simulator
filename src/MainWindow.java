import javax.swing.*;
import java.awt.*;

class MainWindow extends JFrame {

    private final MyPanel panel;
    private final Color background_color = Color.DARK_GRAY, foreground_color = Color.WHITE;

    public MainWindow(String title) {
        super(title);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 700);
        setBackground(Color.BLACK);

        TextField text = new TextField();
        text.setBackground(background_color);
        text.setForeground(foreground_color);

        panel = new MyPanel(text);
        panel.setBackground(background_color);
        panel.setForeground(foreground_color);

        JButton pause_button = new JButton("Pause");
        pause_button.addActionListener(e -> panel.pause());
        pause_button.setBackground(background_color);
        pause_button.setForeground(foreground_color);

        JButton resume_button = new JButton("Resume");
        resume_button.addActionListener(e -> panel.resume());
        resume_button.setBackground(background_color);
        resume_button.setForeground(foreground_color);

        JButton reset_button = new JButton("Reset");
        reset_button.addActionListener(e -> panel.reset());
        reset_button.setBackground(background_color);
        reset_button.setForeground(foreground_color);

        Container container = getContentPane();
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        container.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;

        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        container.add(panel, constraints);

        constraints.gridwidth = 1;
        constraints.weighty = 0;
        constraints.gridy = 1;
        constraints.gridx = 0;
        container.add(pause_button, constraints);

        constraints.gridx = 1;
        container.add(resume_button, constraints);

        constraints.gridx = 2;
        container.add(reset_button, constraints);

        constraints.gridx = 3;
        container.add(text, constraints);

        setVisible(true);
        panel.reset();
    }

    public void start() {
        panel.startSimulation(0.01);
    }
}