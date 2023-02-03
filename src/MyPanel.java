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
    private double t;
    public double dt;
    private ArrayList<Body> bodies;
    private Body current_body;
    private boolean stop;
    private int creation_step, fps, frames_rendered;
    private double dx, dy, scale;
    private boolean up, down, left, right;

    public MyPanel(TextField text) {
        this.text = text;
        rand = new Random((long) (t * 1000));
        up = false;
        down = false;
        left = false;
        right = false;
        bodies = new ArrayList<>();
        t = 0;
        dt = 0.01;
        creation_step = 0;
        dx = 0;
        dy = 0;
        scale = 1;
        fps = 0;
        frames_rendered = 0;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();
        reset();
        countFps();
        repaint();
    }

    public void countFps() {
        int refresh = 500;
        Runnable count = () -> {
            fps = frames_rendered * (1000 / refresh);
            frames_rendered = 0;
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(count, 0, refresh, TimeUnit.MILLISECONDS);
    }

    public void pause() {
        stop = true;
        requestFocus();
    }

    public void resume() {
        stop = false;
        requestFocus();
    }

    public void reset() {
        requestFocus();
        bodies = new ArrayList<>();
        t = 0;
        creation_step = 0;
        dx = 0;
        dy = 0;
        scale = 1;
        fps = 0;
        frames_rendered = 0;
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
        g.fillOval((int) (((position.x - size / 2) + dx) * scale) + getWidth() / 2, (int) (((position.y - size / 2) + dy) * scale) + getHeight() / 2, (int) (size * scale), (int) (size * scale));
    }

//    private void drawCircle(Graphics g, Vector position, double size) {
//        g.drawOval((int) (((position.x - size / 2) + dx) * scale) + getWidth() / 2, (int) (((position.y - size / 2) + dy) * scale) + getHeight() / 2, (int) (size * scale), (int) (size * scale));
//    }

    private void drawLine(Graphics g, Vector start, Vector end) {
        g.drawLine((int) ((start.x + dx) * scale) + getWidth() / 2, (int) ((start.y + dy) * scale) + getHeight() / 2, (int) ((end.x + dx) * scale) + getWidth() / 2, (int) ((end.y + dy) * scale) + getHeight() / 2);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        text.setText("fps: " + fps + " dx: " + String.format("%.3f", dx) + " dy: " + String.format("%.3f", dy) + " scale: " + String.format("%.3f", scale));
        if (creation_step == 0 && !stop) {
            updateBodies();
        }
        frames_rendered++;
        if (up) {
            dy += 10 / scale;
        }
        if (down) {
            dy -= 10 / scale;
        }
        if (left) {
            dx += 10 / scale;
        }
        if (right) {
            dx -= 10 / scale;
        }
        for (Body b : bodies) {
            Vector center = b.position, arrow_end = b.position.add(b.velocity);
            double s = Math.sqrt(b.mass) * 2;

            double ts = 1;
            float thue = b.hue - (float) (dt / 10 * b.trail.size());
            for (Vector t : b.trail) {
                g.setColor(Color.getHSBColor(thue, 1, 1));
                fillCircle(g, t, ts);
                ts += (s / b.trail.size());
                thue += dt / 10;
            }

            g.setColor(Color.getHSBColor(b.hue, 1, 1));
            fillCircle(g, center, s);

            if (creation_step != 0 || stop) {
                g.setColor(getForeground());
                drawLine(g, center, arrow_end);
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        creation_step++;
        requestFocus();
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
        if (e.getKeyChar() == 'w') {
            up = true;
        } else if (e.getKeyChar() == 's') {
            down = true;
        } else if (e.getKeyChar() == 'a') {
            left = true;
        } else if (e.getKeyChar() == 'd') {
            right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w') {
            up = false;
        } else if (e.getKeyChar() == 's') {
            down = false;
        } else if (e.getKeyChar() == 'a') {
            left = false;
        } else if (e.getKeyChar() == 'd') {
            right = false;
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

        Vector(MouseEvent e) {
            x = ((double)(e.getX() - getWidth() / 2) / scale) - dx;
            y = ((double)(e.getY() - getHeight() / 2) / scale) - dy;
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

        void addIn(Vector v) {
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

        Vector setLength(double l) {
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
            return new Vector(position, b.position).setLength((100 * (mass * b.mass)) / distTo(b));
        }

        void update(ArrayList<Body> bodies, double dt) {
            Vector force_sum = new Vector();
            for (Body b : bodies) {
                force_sum.addIn(force(b));
            }
            trail.add(position.copy());
            if (trail.size() > 50) {
                trail.remove(0);
            }
            position.addIn(velocity.mult(dt));
            velocity.addIn(force_sum.div(mass).mult(dt));
        }
    }
}
