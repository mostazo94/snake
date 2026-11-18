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
    public enum State {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    public static final int GOLDEN_SPAWN_INTERVAL = 5;
    public static final int GOLDEN_FOOD_TICKS = 35;
    public static final int GOLDEN_FOOD_POINTS = 3;

    private final int cols;
    private final int rows;
    private final Random random;

    private final Deque<GridPoint> snake = new ArrayDeque<>();
    private final Set<GridPoint> occupied = new HashSet<>();
    private final Deque<Direction> directionQueue = new ArrayDeque<>();

    private GameMode gameMode = GameMode.INFINITE;
    private Difficulty difficulty = Difficulty.NORMAL;
    private State state = State.MENU;

    private Direction currentDirection;
    private GridPoint food;
    private GridPoint goldenFood;
    private int goldenFoodTicksLeft;

    private int score;
    private int normalApplesEaten;
    private int highScore;
    private boolean newRecord;
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
        initBoard();
        this.state = State.MENU;
    }

    private void initBoard() {
        snake.clear();
        occupied.clear();
        directionQueue.clear();

        currentDirection = Direction.RIGHT;
        score = 0;
        normalApplesEaten = 0;
        newRecord = false;
        won = false;
        goldenFood = null;
        goldenFoodTicksLeft = 0;

        int startX = cols / 2;
        int startY = rows / 2;
        addSegmentAtTail(new GridPoint(startX, startY));
        addSegmentAtTail(new GridPoint(startX - 1, startY));
        addSegmentAtTail(new GridPoint(startX - 2, startY));

        spawnFood();
    }

    public void startGame() {
        initBoard();
        state = State.PLAYING;
    }

    public void reset() {
        startGame();
    }

    public void returnToMenu() {
        initBoard();
        state = State.MENU;
    }

    public void setGameMode(GameMode gameMode) {
        if (gameMode != null) {
            this.gameMode = gameMode;
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    private void addSegmentAtTail(GridPoint point) {
        snake.addLast(point);
        occupied.add(point);
    }

    public void togglePause() {
        if (state == State.PLAYING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED) {
            state = State.PLAYING;
        }
    }

    /**
     * Encola hasta 2 cambios de dirección válidos para que giros rápidos
     * (por ejemplo Arriba + Izquierda) se sientan fluidos y nunca giren 180°.
     */
    public void queueDirection(Direction newDirection) {
        if (state != State.PLAYING || newDirection == null || directionQueue.size() >= 2) {
            return;
        }
        Direction reference = directionQueue.isEmpty() ? currentDirection : directionQueue.peekLast();
        if (newDirection != reference && !reference.isOpposite(newDirection)) {
            directionQueue.addLast(newDirection);
        }
    }

    /**
     * Avanza un paso (tick) en el juego y devuelve un TickEvent con lo ocurrido.
     */
    public TickEvent update() {
        if (state != State.PLAYING) {
            return TickEvent.NONE;
        }

        if (!directionQueue.isEmpty()) {
            currentDirection = directionQueue.pollFirst();
        }

        GridPoint head = snake.peekFirst();
        GridPoint rawNext = head.translate(currentDirection);
        GridPoint next;

        if (gameMode.wrapsWalls()) {
            next = new GridPoint(
                    Math.floorMod(rawNext.x(), cols),
                    Math.floorMod(rawNext.y(), rows)
            );
        } else {
            if (rawNext.x() < 0 || rawNext.x() >= cols || rawNext.y() < 0 || rawNext.y() >= rows) {
                state = State.GAME_OVER;
                return new TickEvent(false, false, null, false, true, false);
            }
            next = rawNext;
        }

        boolean willEatNormal = next.equals(food);
        boolean willEatGolden = next.equals(goldenFood);
        boolean willGrow = willEatNormal || willEatGolden;

        // Si NO crece, liberamos la cola antes de verificar colisiones:
        // así es válido moverse a la celda que la cola acaba de desocupar.
        if (!willGrow) {
            GridPoint tail = snake.removeLast();
            occupied.remove(tail);
        }

        if (occupied.contains(next)) {
            state = State.GAME_OVER;
            return new TickEvent(false, false, null, false, true, false);
        }

        snake.addFirst(next);
        occupied.add(next);

        boolean goldenSpawned = false;

        if (willEatGolden) {
            goldenFood = null;
            goldenFoodTicksLeft = 0;
            addScore(GOLDEN_FOOD_POINTS);
        } else if (goldenFood != null) {
            goldenFoodTicksLeft--;
            if (goldenFoodTicksLeft <= 0) {
                goldenFood = null;
                goldenFoodTicksLeft = 0;
            }
        }

        if (willEatNormal) {
            normalApplesEaten++;
            addScore(1);
            spawnFood();

            if (state == State.PLAYING
                    && goldenFood == null
                    && normalApplesEaten % GOLDEN_SPAWN_INTERVAL == 0) {
                goldenSpawned = spawnGoldenFood();
            }
        }

        boolean ended = state == State.GAME_OVER;
        return new TickEvent(
                willEatNormal,
                willEatGolden,
                willGrow ? next : null,
                goldenSpawned,
                ended && !won,
                ended && won
        );
    }

    private void addScore(int points) {
        score += points;
        if (score > highScore) {
            highScore = score;
            newRecord = true;
        }
    }

    private void spawnFood() {
        List<GridPoint> freeCells = collectFreeCells(goldenFood);
        if (freeCells.isEmpty()) {
            food = null;
            goldenFood = null;
            goldenFoodTicksLeft = 0;
            won = true;
            state = State.GAME_OVER;
            return;
        }
        food = freeCells.get(random.nextInt(freeCells.size()));
    }

    private boolean spawnGoldenFood() {
        List<GridPoint> freeCells = collectFreeCells(food);
        if (freeCells.isEmpty()) {
            return false;
        }
        goldenFood = freeCells.get(random.nextInt(freeCells.size()));
        goldenFoodTicksLeft = GOLDEN_FOOD_TICKS;
        return true;
    }

    private List<GridPoint> collectFreeCells(GridPoint extraExcluded) {
        List<GridPoint> freeCells = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                GridPoint cell = new GridPoint(x, y);
                if (!occupied.contains(cell) && !cell.equals(extraExcluded)) {
                    freeCells.add(cell);
                }
            }
        }
        return freeCells;
    }

    public int getTickDelayMs() {
        return difficulty.tickDelayForScore(score);
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

    public GridPoint getGoldenFood() {
        return goldenFood;
    }

    public int getGoldenFoodTicksLeft() {
        return goldenFoodTicksLeft;
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

    public GameMode getGameMode() {
        return gameMode;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public State getState() {
        return state;
    }

    public boolean isInMenu() {
        return state == State.MENU;
    }

    public boolean isPaused() {
        return state == State.PAUSED;
    }

    public boolean isGameOver() {
        return state == State.GAME_OVER;
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
