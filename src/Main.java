import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends JFrame { // Приложение – наследник JFrame (окна)
    public Main(String title) {
        super(title);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        //setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        TextField tf = new TextField();
        tf.setBounds(0, 150, 200, 40);

        MyPanel panel = new MyPanel(tf);
        panel.setBounds(0, 0, 1920, 1080);
        panel.setOpaque(false);

        JButton pause_button = new JButton("Pause");
        pause_button.setBounds(0, 0, 200, 50);
        pause_button.addActionListener(e -> panel.pause());

        JButton resume_button = new JButton("Resume");
        resume_button.setBounds(0, 50, 200, 50);
        resume_button.addActionListener(e -> panel.resume());

        JButton reset_button = new JButton("Reset");
        reset_button.setBounds(0, 100, 200, 50);
        reset_button.addActionListener(e -> panel.reset());

        add(pause_button);
        add(resume_button);
        add(reset_button);
        add(tf);
        add(panel);
        setSize(1920, 1080);
        setLayout(null);
        setVisible(true);
        panel.start_simulation();
    }

    public static void main(String[] args) {
        Main mw = new Main("simulation");
    }
}

class MyPanel extends JPanel implements MouseListener {
    private double dt, t;
    private ArrayList<Body> bodies;
    private boolean stop, pressed;

    private TextField tf;

    public MyPanel(TextField tf) {
        this.tf = tf;
        dt = 0.01;
        t = 0;
        addMouseListener(this); // добавляем к текущей Панели обработчик мыши
        bodies = new ArrayList<>();
    }

    public void start_simulation() {
        Runnable render = () -> {
            if (!pressed && !stop) {
                update_bodies();
                tf.setText("t = " + String.format("%.3f", t));
            }
            repaint();
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(render, 0, (long) (dt * 1000), TimeUnit.MILLISECONDS);
    }

    public void pause() {
        stop = true;
    }

    public void resume() {
        stop = false;
    }

    public void reset() {
        bodies.clear();
        t = 0;
    }

    private void update_bodies() {
        ArrayList<Body> bodies_updated = new ArrayList<>(bodies);
        for (Body b : bodies_updated) {
            b.update(bodies, dt);
            b.hue += dt;
        }
        bodies = bodies_updated;
        t += dt;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Body b : bodies) {
            Vector arrow_start = b.position, arrow_end = b.position.add(b.velocity);
            double s = Math.sqrt(b.mass) * 2;

            g.setColor(Color.getHSBColor(b.hue, 1, 1));
            g.fillOval((int) (arrow_start.x - (s / 2)), (int) (arrow_start.y - (s / 2)), (int) s, (int) s);

            g.setColor(Color.BLACK);
            g.drawOval((int) (arrow_start.x - (s / 2)), (int) (arrow_start.y - (s / 2)), (int) s, (int) s);

            g.drawLine((int) arrow_start.x, (int) arrow_start.y, (int) arrow_end.x, (int) arrow_end.y);
        }
    }

    public void mousePressed(MouseEvent e) {
        pressed = true;
        Body p = new Body(new Vector(e.getX(), e.getY()), new Vector(0, 0), 1, 0);
        bodies.add(p);
    }

    public void mouseReleased(MouseEvent e) {
        Body b = bodies.get(bodies.size() - 1);
        Vector drag = new Vector(e.getX(), e.getY()).sub(b.position);
        //b.velocity = drag;
        b.mass = Math.max(drag.length() * drag.length(), 10);
        pressed = false;
        bodies.sort((o1, o2) -> Double.compare(o2.mass, o1.mass));
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}

class Vector {
    public double x, y;

    Vector() {
        x = 0;
        y = 0;
    }

    Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Vector(Vector a, Vector b) {
        x = b.x - a.x;
        y = b.y - a.y;
    }

    private boolean eq(double a, double b) {
        return Math.abs(a - b) < 1e-5;
    }

    Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }

    void addin(Vector v) {
        x += v.x;
        y += v.y;
    }

    Vector sub(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    void subin(Vector v) {
        x -= v.x;
        y -= v.y;
    }

    Vector mult(double k) {
        return new Vector(x * k, y * k);
    }

    Vector div(double k) {
        return new Vector(x / k, y / k);
    }

    void multin(double k) {
        x *= k;
        y *= k;
    }

    void divin(double k) {
        x /= k;
        y /= k;
    }

    double length() {
        return Math.sqrt(x * x + y * y);
    }

    Vector setlength(double l) {
        if (eq(length(), 0)) {
            return new Vector();
        }
        double k = l / length();
        return mult(k);
    }
}

class Body {
    public Vector position, velocity;
    public double mass;
    public float hue;

    public Body(Vector position, Vector velocity, double mass, float hue) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.hue = hue;
    }

    double distTo(Body b) {
        Vector d = new Vector(position, b.position);
        return d.length();
    }

    Vector force(Body b) {
        return new Vector(position, b.position).setlength((100 * (mass * b.mass)) / (distTo(b) * distTo(b)));
    }

    void update(ArrayList<Body> bodies, double dt) {
        Vector force_sum = new Vector();
        for (Body b : bodies) {
            force_sum.addin(force(b));
        }

        position.addin(velocity.mult(dt));
        velocity.addin(force_sum.div(mass).mult(dt));
    }
}
