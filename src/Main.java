import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class MainWindow extends JFrame {

    private final MyPanel panel;
    private final TextField tf;
    private final JButton pause_button;
    private final JButton resume_button;
    private final JButton reset_button;

    public MainWindow(String title) {
        super(title);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLayout(null);

        tf = new TextField();
        tf.setBounds(0, 150, 200, 40);

        panel = new MyPanel(tf);
        panel.setBounds(0, 0, 1920, 1080);
        panel.setOpaque(false);

        pause_button = new JButton("Pause");
        pause_button.setBounds(0, 0, 200, 50);
        pause_button.addActionListener(e -> panel.pause());

        resume_button = new JButton("Resume");
        resume_button.setBounds(0, 50, 200, 50);
        resume_button.addActionListener(e -> panel.resume());

        reset_button = new JButton("Reset");
        reset_button.setBounds(0, 100, 200, 50);
        reset_button.addActionListener(e -> panel.reset());

        add(pause_button);
        add(resume_button);
        add(reset_button);
        add(tf);
        add(panel);
        setVisible(true);
    }

    public void start() {
        panel.start_simulation(0.01);
    }
}

public class Main { // Приложение – наследник JFrame (окна)
    public static void main(String[] args) {
        MainWindow main_window = new MainWindow("simulation");
        main_window.start();
    }
}

class MyPanel extends JPanel implements MouseListener, MouseMotionListener {
    private final TextField tf;
    private double t, dt;
    private ArrayList<Body> bodies;
    private Body current_body;
    private boolean stop;
    private int creation_step;

    public MyPanel(TextField tf) {
        this.tf = tf;
        t = 0;
        creation_step = 0;
        addMouseListener(this); // добавляем к текущей Панели обработчик мыши
        addMouseMotionListener(this);
        bodies = new ArrayList<>();
    }

    public void start_simulation(double dt) {
        this.dt = dt;
        Runnable render = () -> {
            tf.setText("t = " + String.format("%.3f", t));
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
            b.hue += dt / 10;
        }
        bodies = bodies_updated;
        t += dt;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (creation_step == 0 && !stop) {
            update_bodies();
        }

        for (Body b : bodies) {
            Vector center = b.position, arrow_end = b.position.add(b.velocity);
            double s = Math.sqrt(b.mass) * 2;

            double ts = 1;
            float thue = b.hue - (float) (dt * b.trail.size());
            for (Vector t : b.trail) {
                g.setColor(Color.getHSBColor(thue, 1, 1));
                g.fillOval((int) (t.x - ts / 2), (int) (t.y - ts / 2), (int) ts, (int) ts);
                ts += (s / b.trail.size());
                thue += dt;
            }

            g.setColor(Color.getHSBColor(b.hue, 1, 1));
            g.fillOval((int) (center.x - (s / 2)), (int) (center.y - (s / 2)), (int) s, (int) s);

            g.setColor(Color.BLACK);
            g.drawOval((int) (center.x - (s / 2)), (int) (center.y - (s / 2)), (int) s, (int) s);

            g.drawLine((int) center.x, (int) center.y, (int) arrow_end.x, (int) arrow_end.y);

        }
    }

    public void mousePressed(MouseEvent e) {
        creation_step++;
        switch (creation_step) {
            case 1 -> {
                current_body = new Body(new Vector(e.getX(), e.getY()), new Vector(0, 0), 1, 0);
                bodies.add(current_body);
            }
            case 2 -> current_body.velocity = new Vector(e.getX(), e.getY()).sub(current_body.position);
            case 3 -> {
                double size = new Vector(e.getX(), e.getY()).sub(current_body.position).length();
                current_body.mass = Math.max(size * size, 10);
                bodies.sort((o1, o2) -> Double.compare(o2.mass, o1.mass));
                creation_step = 0;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        switch (creation_step) {
            case 1 -> current_body.velocity = new Vector(e.getX(), e.getY()).sub(current_body.position);
            case 2 -> {
                double size = new Vector(e.getX(), e.getY()).sub(current_body.position).length();
                current_body.mass = Math.max(size * size, 10);
            }
        }
        repaint();
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

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

        public Vector copy() {
            return new Vector(x, y);
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

        Vector mult(double k) {
            return new Vector(x * k, y * k);
        }

        Vector div(double k) {
            return new Vector(x / k, y / k);
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
        public ArrayList<Vector> trail;

        public Body(Vector position, Vector velocity, double mass, float hue) {
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
            this.hue = hue;
            trail = new ArrayList<>();
        }

        double distTo(Body b) {
            Vector d = new Vector(position, b.position);
            return d.length();
        }

        Vector force(Body b) {
            return new Vector(position, b.position).setlength((100 * (mass * b.mass)) / distTo(b));
        }

        void update(ArrayList<Body> bodies, double dt) {
            Vector force_sum = new Vector();
            for (Body b : bodies) {
                force_sum.addin(force(b));
            }
            if (trail.isEmpty() || new Vector(trail.get(trail.size() - 1), position).length() > 10) {
                trail.add(position.copy());
            }
            if (trail.size() > 20) {
                trail.remove(0);
            }
            position.addin(velocity.mult(dt));
            velocity.addin(force_sum.div(mass).mult(dt));
        }
    }
}


