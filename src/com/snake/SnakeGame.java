package com.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SnakeGame {
    public static final int INITIAL_TICK_MS = 125;
    public static final int MIN_TICK_MS = 65;
    public static final int SPEED_STEP_MS = 3;

    private final int cols;
    private final int rows;
    private final Random random;

    private final Deque<GridPoint> snake = new ArrayDeque<>();
    private final Set<GridPoint> occupied = new HashSet<>();
    private final Deque<Direction> directionQueue = new ArrayDeque<>();

    private Direction currentDirection;
    private GridPoint food;
    private int score;
    private int highScore;
    private boolean newRecord;
    private boolean paused;
    private boolean gameOver;
    private boolean won;

    public SnakeGame(int cols, int rows) {
        this(cols, rows, 0, new Random());
    }

    public SnakeGame(int cols, int rows, int initialHighScore) {
        this(cols, rows, initialHighScore, new Random());
    }

    public SnakeGame(int cols, int rows, int initialHighScore, Random random) {
        this.cols = cols;
        this.rows = rows;
        this.highScore = Math.max(0, initialHighScore);
        this.random = random;
        reset();
    }

    public void reset() {
        snake.clear();
        occupied.clear();
        directionQueue.clear();

        currentDirection = Direction.RIGHT;
        score = 0;
        newRecord = false;
        paused = false;
        gameOver = false;
        won = false;

        int startX = cols / 2;
        int startY = rows / 2;
        // Cabeza primero, luego el cuerpo hacia la izquierda
        addSegmentAtTail(new GridPoint(startX, startY));
        addSegmentAtTail(new GridPoint(startX - 1, startY));
        addSegmentAtTail(new GridPoint(startX - 2, startY));

        spawnFood();
    }

    private void addSegmentAtTail(GridPoint point) {
        snake.addLast(point);
        occupied.add(point);
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    /**
     * Encola hasta 2 cambios de dirección válidos para que giros rápidos
     * (por ejemplo Arriba + Izquierda) se sientan fluidos y nunca giren 180°.
     */
    public void queueDirection(Direction newDirection) {
        if (gameOver || paused || newDirection == null || directionQueue.size() >= 2) {
            return;
        }
        Direction reference = directionQueue.isEmpty() ? currentDirection : directionQueue.peekLast();
        if (newDirection != reference && !reference.isOpposite(newDirection)) {
            directionQueue.addLast(newDirection);
        }
    }

    /**
     * Avanza un paso (tick) en el juego.
     */
    public void update() {
        if (gameOver || paused) {
            return;
        }

        if (!directionQueue.isEmpty()) {
            currentDirection = directionQueue.pollFirst();
        }

        GridPoint head = snake.peekFirst();
        GridPoint rawNext = head.translate(currentDirection);

        // Bordes infinitos: al salir por un lado, aparece por el opuesto
        GridPoint next = new GridPoint(
                Math.floorMod(rawNext.x(), cols),
                Math.floorMod(rawNext.y(), rows)
        );

        boolean willEat = next.equals(food);

        // Si NO come, liberamos la cola antes de verificar colisiones:
        // así es válido moverse a la celda que la cola acaba de desocupar.
        if (!willEat) {
            GridPoint tail = snake.removeLast();
            occupied.remove(tail);
        }

        if (occupied.contains(next)) {
            gameOver = true;
            return;
        }

        snake.addFirst(next);
        occupied.add(next);

        if (willEat) {
            score++;
            if (score > highScore) {
                highScore = score;
                newRecord = true;
            }
            spawnFood();
        }
    }

    private void spawnFood() {
        List<GridPoint> freeCells = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                GridPoint cell = new GridPoint(x, y);
                if (!occupied.contains(cell)) {
                    freeCells.add(cell);
                }
            }
        }

        if (freeCells.isEmpty()) {
            food = null;
            won = true;
            gameOver = true;
            return;
        }

        food = freeCells.get(random.nextInt(freeCells.size()));
    }

    /**
     * Calcula el intervalo entre ticks en milisegundos.
     * Acelera suavemente conforme aumenta el puntaje hasta un mínimo cómodo.
     */
    public int getTickDelayMs() {
        return Math.max(MIN_TICK_MS, INITIAL_TICK_MS - score * SPEED_STEP_MS);
    }

    public Iterable<GridPoint> getSnake() {
        return Collections.unmodifiableCollection(snake);
    }

    public int getSnakeLength() {
        return snake.size();
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public GridPoint getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public boolean isNewRecord() {
        return newRecord;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }
}
