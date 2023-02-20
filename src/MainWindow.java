import javax.swing.*;
import java.awt.*;

//Класс главного окна программы. Здесь описывается, какие элементы будут отображаться на окне и где.
class MainWindow extends JFrame {
    public MainWindow(String title) {
        //Указание размеров окна и его поведения
        super(title);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 700);
        setBackground(Color.BLACK);

        //Создание текстового поля, на которое будут выводться информация о симуляции
        TextField text = new TextField();
        Color background_color = Color.WHITE;
        text.setBackground(background_color);
        Color foreground_color = Color.BLACK;
        text.setForeground(foreground_color);

        //Создание самой панели, в которой будет проходить симуляция
        MyPanel panel = new MyPanel(text);
        panel.setBackground(background_color);
        panel.setForeground(foreground_color);

        //Создание кнопки приостановления
        JButton pause_button = new JButton("Pause");
        pause_button.addActionListener(e -> panel.pause());
        pause_button.setBackground(background_color);
        pause_button.setForeground(foreground_color);

        //Создание кнопки возобновления
        JButton resume_button = new JButton("Resume");
        resume_button.addActionListener(e -> panel.resume());
        resume_button.setBackground(background_color);
        resume_button.setForeground(foreground_color);

        //Создание кнопки сброса
        JButton reset_button = new JButton("Reset");
        reset_button.addActionListener(e -> panel.reset());
        reset_button.setBackground(background_color);
        reset_button.setForeground(foreground_color);

        //Создание контейнера заполнения окна элементами
        Container container = getContentPane();
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        container.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        //Добавление панели симуляции в контейнер
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        container.add(panel, constraints);

        //Добавление кнопок в контейнер
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

        //Появление окна на экране
        setVisible(true);
        panel.reset();
    }
}