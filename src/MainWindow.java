import javax.swing.*;
import java.awt.*;

class MainWindow extends JFrame {

    private final MyPanel panel;
    private final TextField text;
    private final JButton pause_button;
    private final JButton resume_button;
    private final JButton reset_button;

    public MainWindow(String title) {
        super(title);
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 700);

        text = new TextField();

        panel = new MyPanel(text);

        pause_button = new JButton("Pause");
        pause_button.addActionListener(e -> panel.pause());

        resume_button = new JButton("Resume");
        resume_button.addActionListener(e -> panel.resume());

        reset_button = new JButton("Reset");
        reset_button.addActionListener(e -> panel.reset());

        Container container = getContentPane();
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        container.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // По умолчанию натуральная высота, максимальная ширина
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
    }

    public void start() {
        panel.startSimulation(0.01);
    }
}