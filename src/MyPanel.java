import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;


//Класс панели симуляции. Здесь описывается поведение всего, что происходит в симуляции тел.
class MyPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private final TextField text; //Текстовое поле с информацией
    private final Random rand; //Генератор случайных чисел для раскраски тел
    private final double dt; //Шаг времени между кадрами для обновления тел
    private double t; //Время с начала симуляции
    private ArrayList<Body> bodies; //Массив создаваемых тел
    private Body current_body; //Текущее создаваемое тело
    private boolean stop; //Состояние: активное или приостановленное
    private int creation_step; //Этап создания нового тела
    private int fps; //Количество кадров в секунду
    private int frames_rendered; //Количество отрисованных кадров с предыдущего измерения
    private double dx, dy, scale; //Сдвиги по горизонтали и вертикали, масштаб
    private boolean up, down, left, right; //Показатели нажатия клавиш для сдвига
    private final Phaser phaser;
    private final ExecutorService body_updater;

    public MyPanel(TextField text) {
        //Инициализация переменных
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
        body_updater = Executors.newFixedThreadPool(8);
        phaser = new Phaser();

        //Добавление считывателей клавиатуры и мышки
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);

        //Передача фокусировки на данную панель
        setFocusable(true);
        requestFocus();
        //Запуск счётчика кадров и покараски панели
        reset();
        countFps();
        repaint();
    }
    private void countFps() { //Подсчёт кадров в секунду
        int refresh = 500;
        //Каждые refresh миллисекунд производится измерение количества кадров в секунду
        Runnable count = () -> {
            fps = frames_rendered * (1000 / refresh);
            frames_rendered = 0;
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(count, 0, refresh, TimeUnit.MILLISECONDS);
    }

    public void pause() { //Приостановление симуляции
        stop = true;
        requestFocus();
    }

    public void resume() { //возобновление симуляции
        stop = false;
        requestFocus();
    }

    public void reset() { //Сброс симуляции, реинициализация полей класса
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

    public void stress() {
        Random rng = new Random();
        for (int i = 0; i < 1000; i++) {
            Vector pos = new Vector(rng.nextDouble() * 500 - 250, rng.nextDouble() * 500 - 250);
            Vector vel = new Vector(rng.nextDouble() * 500 - 250, rng.nextDouble() * 500 - 250);
            double m = rng.nextDouble() * 100;
            float h = rng.nextFloat();
            bodies.add(new Body(pos, vel, m, h));
        }
    }

    private void updateBodies() { //Обновление параметров тел во время прорисоски кадров
        ArrayList<Body> bodies_updated = new ArrayList<>(bodies);
        //Для каждого тела обновляем его полодение и скорость, а также его цвет
        phaser.bulkRegister(bodies_updated.size() + 1);
        for (Body b : bodies_updated) {
            body_updater.execute(() -> {
            b.update(bodies, dt);
            b.hue += dt / 10;
            phaser.arriveAndDeregister();
            });
        }
        phaser.arriveAndAwaitAdvance();
        //Замена старого списка тел новым и совершение шага во времени
        bodies = bodies_updated;
        t += dt;
    }

    private void fillCircle(Graphics g, Vector position, double size) {
        //Отрисовка окружности с учётом масштаба и сдвига
        g.fillOval((int) (((position.x - size / 2) + dx) * scale) + getWidth() / 2, (int) (((position.y - size / 2) + dy) * scale) + getHeight() / 2, (int) (size * scale), (int) (size * scale));
    }

    private void drawLine(Graphics g, Vector start, Vector end) {
        //Отрисовка отрезка с учётом масштаба и сдвига
        g.drawLine((int) ((start.x + dx) * scale) + getWidth() / 2, (int) ((start.y + dy) * scale) + getHeight() / 2, (int) ((end.x + dx) * scale) + getWidth() / 2, (int) ((end.y + dy) * scale) + getHeight() / 2);
    }

    @Override
    public void paint(Graphics g) { //Основной цикл обновления и отрисовки тел
        super.paint(g);

        //Вывод информации в текстовое поле
        text.setText("fps: " + fps + " t: " + String.format("%.3f", t) + " dx: " + String.format("%.3f", dx) + " dy: " + String.format("%.3f", dy) + " scale: " + String.format("%.3f", scale));

        //Обновление тел
        if (creation_step == 0 && !stop) {
            updateBodies();
        }

        //Увенличение счётчика отрисованных кадров
        frames_rendered++;

        //Сдвиг экрана в зависимости от зажатых клавиш
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

        //Отрисовка каждого тела
        for (Body b : bodies) {

            //Создание вектора положения тела и его радиуса для укорочения кода
            Vector center = b.position, arrow_end = b.position.add(b.velocity);
            double s = Math.sqrt(b.mass) * 2;


            //Отрисовка следа тела
            double ts = 1;
            float thue = b.hue - (float) (dt / 10 * b.trail.size());
            //Проходясь по предыдущим положениям тела, рисуем окружности
            for (Vector t : b.trail) {
                g.setColor(Color.getHSBColor(thue, 1, 1));
                fillCircle(g, t, ts);
                ts += (s / b.trail.size());
                thue += dt / 10;
            }

            //Отрисовка тела, как окружности
            g.setColor(Color.getHSBColor(b.hue, 1, 1));
            fillCircle(g, center, s);

            //Если симуляция приостановлена, то рисуется вектор скорости
            if (creation_step != 0 || stop) {
                g.setColor(getForeground());
                drawLine(g, center, arrow_end);
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //При нажатии мышки совершается переход на следующий этап создания тела
        creation_step++;
        requestFocus();
        switch (creation_step) {
            case 1 -> {
                // Определение положения тела
                current_body = new Body(new Vector(e), new Vector(0, 0), 1, rand.nextFloat());
                bodies.add(current_body);
            }
            // Определение скорости тела
            case 2 -> current_body.velocity = new Vector(e).sub(current_body.position);
            case 3 -> {
                // определение размера тела
                double size = new Vector(e).sub(current_body.position).length();
                current_body.mass = Math.max(size * size, 10);
                bodies.sort((o1, o2) -> Double.compare(o2.mass, o1.mass));
                creation_step = 0;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Динамическое обновление параметров создаваемого тела при движении мыши
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
    public void mouseWheelMoved(MouseWheelEvent e) { //Изменение масштаба при прокрутке колеса мыши
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
        //Обновление полей, отвечающих за сдвиг экрана при нажатии клавиш
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W) {
            up = true;
        } else if (k == KeyEvent.VK_S) {
            down = true;
        } else if (k == KeyEvent.VK_A) {
            left = true;
        } else if (k == KeyEvent.VK_D) {
            right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //Обновление полей, отвечающих за сдвиг экрана при отпускании клавиш
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W) {
            up = false;
        } else if (k == KeyEvent.VK_S) {
            down = false;
        } else if (k == KeyEvent.VK_A) {
            left = false;
        } else if (k == KeyEvent.VK_D) {
            right = false;
        }
    }

    //Класс вектора
    class Vector {
        public double x, y; //x и y составляющие вектора

        Vector() { //Конструктор по умолчанию
            x = 0;
            y = 0;
        }

        Vector(double x, double y) { //Конструктор по x и y составляющим
            this.x = x;
            this.y = y;
        }

        Vector(Vector a, Vector b) {
            //Конструктор, создающий вектор, соединяющий концы векторов a и b
            x = b.x - a.x;
            y = b.y - a.y;
        }

        Vector(MouseEvent e) {
            //Конструктор, создающий вектор по положению мыши на экране
            x = ((double) (e.getX() - getWidth() / 2) / scale) - dx;
            y = ((double) (e.getY() - getHeight() / 2) / scale) - dy;
        }

        private boolean eq0(double a) { //Метод сравнивания с нулём
            return Math.abs(a) < 1e-5;
        }

        public Vector copy() { //Метод копирования вектора
            return new Vector(x, y);
        }

        Vector add(Vector v) {
            return new Vector(x + v.x, y + v.y);
        } // Метод сложения векторов

        void addIn(Vector v) { //Метод добавления и присвоения вектора к данному
            x += v.x;
            y += v.y;
        }

        Vector sub(Vector v) { // Метод отнимания векторов
            return new Vector(x - v.x, y - v.y);
        }

        Vector multiply(double k) { // Метод умножения вектора на число
            return new Vector(x * k, y * k);
        }

        Vector div(double k) { // Метод деления вектора на число
            return new Vector(x / k, y / k);
        }

        double length() { // Метод, вычисляющий длину вектора
            return Math.sqrt(x * x + y * y);
        }

        Vector setLength(double l) { //Метод, изменяющий длину вектора на данную
            if (eq0(length())) {
                return new Vector();
            }
            double k = l / length();
            return multiply(k);
        }
    }

    //Класс тела
    class Body {
        public Vector position, velocity; //Вектора положения и скорости тела
        public double mass; //Масса тела
        public float hue; //Оттенок тела
        public ArrayList<Vector> trail; //Список векторов положений следа тела

        public Body(Vector position, Vector velocity, double mass, float hue) {
            //Конструктор по указанным полям
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
            this.hue = hue;
            trail = new ArrayList<>();
        }

        double distTo(Body b) { //Метод, вычисляющий расстояние до другого тела
            Vector d = new Vector(position, b.position);
            return d.length();
        }

        Vector force(Body b) { //Метод, вычисляющий силу взаимодействия до другого тела
            return new Vector(position, b.position).setLength((100 * (mass * b.mass)) / distTo(b));
        }

        void update(ArrayList<Body> bodies, double dt) { //Метод, обновляющий параметры тела
            // Расчёт результирующей силы на данное тело
            Vector force_sum = new Vector();
            for (Body b : bodies) {
                force_sum.addIn(force(b));
            }

            //Сохранение старого положения в список следа тела и сохранение его длины
            trail.add(position.copy());
            while (trail.size() > 50 || trail.size() * bodies.size() > 1000) {
                trail.remove(0);
            }

            //Обновление положения и скорости тела
            position.addIn(velocity.multiply(dt));
            velocity.addIn(force_sum.div(mass).multiply(dt));
        }
    }
}