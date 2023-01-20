import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class MyPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private final TextField text;
    private final Random rand;
    private double t, dt;
    private ArrayList<Body> bodies;
    private Body current_body;
    private boolean stop;
    private int creation_step;
    private double dx, dy, scale;

    public MyPanel(TextField text) {
        this.text = text;
        t = 0;
        creation_step = 0;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();
        bodies = new ArrayList<>();
        rand = new Random((long) (t * 1000));
        dx = (double) this.getWidth() / 2;
        dy = (double) this.getHeight() / 2;
        //System.out.println(this.getWidth() + " " + this.getHeight());
        scale = 1;
        repaint();
    }

    public void startSimulation(double dt) {
        this.dt = dt;
        Runnable render = () -> {
            text.setText("t = " + String.format("%.3f", t) +
                    " dx = " + String.format("%.3f", dx) +
                    " dy = " + String.format("%.3f", dy) +
                    "\n scale = " + String.format("%.3f", scale));
            repaint();
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(16);
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
        dx = (double) this.getWidth() / 2;
        dy = (double) this.getHeight() / 2;
        scale = 1;
    }

    private void updateBodies() {
        ArrayList<Body> bodies_updated = new ArrayList<>(bodies);
        for (Body b : bodies_updated) {
            b.update(bodies, dt);
            b.hue += dt / 10;
        }
        bodies = bodies_updated;
        t += dt;
    }

    private void fillCircle(Graphics g, Vector position, double size) {
        g.fillOval((int) (((position.x - size / 2) * scale) + dx), (int) (((position.y - size / 2) * scale) + dy), (int) (size * scale), (int) (size * scale));
    }

    private void drawCircle(Graphics g, Vector position, double size) {
        g.drawOval((int) (((position.x - size / 2) * scale) + dx), (int) (((position.y - size / 2) * scale) + dy), (int) (size * scale), (int) (size * scale));
    }

    private void drawLine(Graphics g, Vector start, Vector end) {
        g.drawLine((int) ((start.x * scale) + dx), (int) ((start.y * scale) + dy), (int) ((end.x * scale) + dx), (int) ((end.y * scale) + dy));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (creation_step == 0 && !stop) {
            updateBodies();
        }
        for (Body b : bodies) {
            Vector center = b.position, arrow_end = b.position.add(b.velocity);
            double s = Math.sqrt(b.mass) * 2;

            double ts = 1;
            float thue = b.hue - (float) (dt * b.trail.size());
            for (Vector t : b.trail) {
                g.setColor(Color.getHSBColor(thue, 1, 1));
                fillCircle(g, t, ts);
                ts += (s / b.trail.size());
                thue += dt;
            }

            g.setColor(Color.getHSBColor(b.hue, 1, 1));
            fillCircle(g, center, s);

            if (creation_step != 0 || stop) {
                g.setColor(Color.BLACK);
                drawLine(g, center, arrow_end);
            }
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {
        creation_step++;
        switch (creation_step) {
            case 1 -> {
                current_body = new Body(new Vector(e), new Vector(0, 0), 1, rand.nextFloat());
                bodies.add(current_body);
            }
            case 2 -> current_body.velocity = new Vector(e).sub(current_body.position);
            case 3 -> {
                double size = new Vector(e).sub(current_body.position).length();
                current_body.mass = Math.max(size * size, 10);
                bodies.sort((o1, o2) -> Double.compare(o2.mass, o1.mass));
                creation_step = 0;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        switch (creation_step) {
            case 1 -> current_body.velocity = new Vector(e).sub(current_body.position);
            case 2 -> {
                double size = new Vector(e).sub(current_body.position).length();
                current_body.mass = Math.max(size * size, 10);
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == 1) {
            scale /= 1.1;
        } else {
            scale *= 1.1;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println(e.getKeyChar());
        switch (e.getKeyChar()) {
            case 'w':
                dx--;
            case 's':
                dx++;
            case 'a':
                dy--;
            case 'd':
                dy++;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

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

        Vector(MouseEvent e) {
            x = (e.getX() - dx) / scale;
            y = (e.getY() - dy) / scale;
        }

        private boolean eq0(double a) {
            return Math.abs(a) < 1e-5;
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
            if (eq0(length())) {
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
