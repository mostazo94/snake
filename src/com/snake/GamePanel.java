package com.snake;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    public static final int COLS = 20;
    public static final int ROWS = 20;
    public static final int TILE_SIZE = 28;
    private static final int HUD_HEIGHT = 54;
    private static final int FRAME_MS = 16;

    private static final int BOARD_WIDTH = COLS * TILE_SIZE;
    private static final int BOARD_HEIGHT = ROWS * TILE_SIZE;

    private static final Color BG_COLOR = new Color(18, 22, 28);
    private static final Color HUD_BG_COLOR = new Color(13, 17, 23);
    private static final Color HUD_BORDER_COLOR = new Color(45, 55, 72);
    private static final Color CLASSIC_WALL_COLOR = new Color(239, 68, 68, 170);
    private static final Color GRID_COLOR = new Color(30, 37, 46);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color RECORD_GOLD = new Color(250, 204, 21);

    private static final Color SNAKE_HEAD_COLOR = new Color(74, 222, 128);
    private static final Color SNAKE_DEAD_HEAD_COLOR = new Color(248, 113, 113);
    private static final Color SNAKE_BODY_START = new Color(34, 197, 94);
    private static final Color SNAKE_BODY_END = new Color(21, 128, 61);
    private static final Color EYE_COLOR = new Color(15, 23, 42);

    private static final Color FOOD_COLOR = new Color(239, 68, 68);
    private static final Color FOOD_HIGHLIGHT = new Color(252, 165, 165);
    private static final Color FOOD_LEAF_COLOR = new Color(74, 222, 128);

    private static final Color GOLDEN_COLOR = new Color(250, 204, 21);
    private static final Color GOLDEN_HIGHLIGHT = new Color(254, 249, 195);
    private static final Color GOLDEN_GLOW = new Color(250, 204, 21, 70);

    private static final Color OVERLAY_BACKDROP = new Color(10, 14, 20, 190);
    private static final Color OVERLAY_CARD_BG = new Color(24, 31, 42, 245);
    private static final Color OVERLAY_CARD_BORDER = new Color(71, 85, 105);
    private static final Color ROW_SELECTED_BG = new Color(36, 48, 66);
    private static final Color BUTTON_GREEN = new Color(34, 197, 94);
    private static final Color BUTTON_GREEN_HOVER = new Color(74, 222, 128);

    private static final Font HUD_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font HUD_META_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 34);
    private static final Font OVERLAY_TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font OVERLAY_SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font MENU_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font MENU_VALUE_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font POPUP_FONT = new Font("SansSerif", Font.BOLD, 14);

    // Rectángulos clicables del menú (en coordenadas del tablero)
    private static final int MENU_CARD_W = 390;
    private static final int MENU_CARD_H = 310;
    private static final int MENU_CARD_X = (BOARD_WIDTH - MENU_CARD_W) / 2;
    private static final int MENU_CARD_Y = (BOARD_HEIGHT - MENU_CARD_H) / 2;
    private static final Rectangle MODE_ROW_RECT = new Rectangle(MENU_CARD_X + 26, MENU_CARD_Y + 104, MENU_CARD_W - 52, 42);
    private static final Rectangle DIFF_ROW_RECT = new Rectangle(MENU_CARD_X + 26, MENU_CARD_Y + 156, MENU_CARD_W - 52, 42);
    private static final Rectangle PLAY_BTN_RECT = new Rectangle(MENU_CARD_X + 95, MENU_CARD_Y + 220, MENU_CARD_W - 190, 42);

    private final HighScoreStorage highScoreStorage;
    private final SoundEffects soundEffects;
    private final SnakeGame game;
    private final Timer frameTimer;
    private final Random fxRandom = new Random();

    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private int savedHighScore;
    private int menuSelectedRow = 0; // 0 = Modo, 1 = Dificultad, 2 = Jugar
    private boolean playButtonHovered = false;
    private long lastFrameNanos;
    private double stepAccumulatorMs = 0.0;
    private double animationTimeSec = 0.0;

    public GamePanel() {
        this.highScoreStorage = new HighScoreStorage();
        this.soundEffects = new SoundEffects();
        this.savedHighScore = highScoreStorage.load();
        this.game = new SnakeGame(COLS, ROWS, savedHighScore);

        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + HUD_HEIGHT));
        setBackground(BG_COLOR);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getPoint());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        this.lastFrameNanos = System.nanoTime();
        this.frameTimer = new Timer(FRAME_MS, e -> onFrame());
        this.frameTimer.start();
    }

    private void onFrame() {
        long now = System.nanoTime();
        double dtMs = Math.min(100.0, (now - lastFrameNanos) / 1_000_000.0);
        lastFrameNanos = now;

        animationTimeSec += dtMs / 1000.0;
        updateVisualEffects(dtMs / 1000.0);

        if (game.getState() == SnakeGame.State.PLAYING) {
            stepAccumulatorMs += dtMs;
            int tickDelay = game.getTickDelayMs();
            if (stepAccumulatorMs >= tickDelay) {
                stepAccumulatorMs -= tickDelay;
                TickEvent event = game.update();
                handleTickEvent(event);
            }
        } else {
            stepAccumulatorMs = 0.0;
        }

        repaint();
    }

    private void handleTickEvent(TickEvent event) {
        if (event.ateFood() && event.eatenAt() != null) {
            spawnParticles(event.eatenAt(), FOOD_COLOR, FOOD_HIGHLIGHT, 12);
            spawnFloatingText(event.eatenAt(), "+1", TEXT_PRIMARY);
            soundEffects.playEat();
        }
        if (event.ateGoldenFood() && event.eatenAt() != null) {
            spawnParticles(event.eatenAt(), GOLDEN_COLOR, GOLDEN_HIGHLIGHT, 24);
            spawnFloatingText(event.eatenAt(), "+" + SnakeGame.GOLDEN_FOOD_POINTS, RECORD_GOLD);
            soundEffects.playGoldenEat();
        }
        if (event.goldenSpawned()) {
            soundEffects.playGoldenSpawn();
        }
        if (event.wonTriggered()) {
            soundEffects.playVictory();
        } else if (event.gameOverTriggered()) {
            soundEffects.playGameOver();
        }

        if (game.getHighScore() > savedHighScore) {
            savedHighScore = game.getHighScore();
            highScoreStorage.save(savedHighScore);
        }
    }

    private void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_M) {
            soundEffects.toggleMute();
            repaint();
            return;
        }

        switch (game.getState()) {
            case MENU -> handleMenuKey(keyCode);
            case PLAYING, PAUSED -> handlePlayingKey(keyCode);
            case GAME_OVER -> handleGameOverKey(keyCode);
        }
    }

    private void handleMenuKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                menuSelectedRow = (menuSelectedRow + 2) % 3;
                soundEffects.playClick();
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                menuSelectedRow = (menuSelectedRow + 1) % 3;
                soundEffects.playClick();
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (menuSelectedRow == 0) {
                    game.setGameMode(game.getGameMode().previous());
                    soundEffects.playClick();
                } else if (menuSelectedRow == 1) {
                    game.setDifficulty(game.getDifficulty().previous());
                    soundEffects.playClick();
                }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                if (menuSelectedRow == 0) {
                    game.setGameMode(game.getGameMode().next());
                    soundEffects.playClick();
                } else if (menuSelectedRow == 1) {
                    game.setDifficulty(game.getDifficulty().next());
                    soundEffects.playClick();
                }
            }
            case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> startFreshGame();
            default -> {
            }
        }
        repaint();
    }

    private void handlePlayingKey(int keyCode) {
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_P) {
            game.togglePause();
            soundEffects.playClick();
            repaint();
            return;
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            clearEffects();
            game.returnToMenu();
            soundEffects.playClick();
            repaint();
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> game.queueDirection(Direction.UP);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> game.queueDirection(Direction.DOWN);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> game.queueDirection(Direction.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> game.queueDirection(Direction.RIGHT);
            default -> {
            }
        }
    }

    private void handleGameOverKey(int keyCode) {
        if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            startFreshGame();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            clearEffects();
            game.returnToMenu();
            soundEffects.playClick();
            repaint();
        }
    }

    private void handleMouseMove(Point screenPoint) {
        if (!game.isInMenu()) {
            if (playButtonHovered) {
                playButtonHovered = false;
                setCursor(Cursor.getDefaultCursor());
            }
            return;
        }

        Point boardPoint = new Point(screenPoint.x, screenPoint.y - HUD_HEIGHT);
        boolean overClickable = MODE_ROW_RECT.contains(boardPoint)
                || DIFF_ROW_RECT.contains(boardPoint)
                || PLAY_BTN_RECT.contains(boardPoint);

        playButtonHovered = PLAY_BTN_RECT.contains(boardPoint);
        setCursor(overClickable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    private void handleMouseClick(Point screenPoint) {
        if (!game.isInMenu()) {
            return;
        }

        Point boardPoint = new Point(screenPoint.x, screenPoint.y - HUD_HEIGHT);
        if (MODE_ROW_RECT.contains(boardPoint)) {
            menuSelectedRow = 0;
            game.setGameMode(game.getGameMode().next());
            soundEffects.playClick();
            repaint();
        } else if (DIFF_ROW_RECT.contains(boardPoint)) {
            menuSelectedRow = 1;
            game.setDifficulty(game.getDifficulty().next());
            soundEffects.playClick();
            repaint();
        } else if (PLAY_BTN_RECT.contains(boardPoint)) {
            startFreshGame();
        }
    }

    private void startFreshGame() {
        clearEffects();
        stepAccumulatorMs = 0.0;
        playButtonHovered = false;
        setCursor(Cursor.getDefaultCursor());
        game.startGame();
        soundEffects.playClick();
        repaint();
    }

    private void clearEffects() {
        particles.clear();
        floatingTexts.clear();
    }

    private void spawnParticles(GridPoint cell, Color primary, Color secondary, int count) {
        double cx = cell.x() * TILE_SIZE + TILE_SIZE / 2.0;
        double cy = cell.y() * TILE_SIZE + TILE_SIZE / 2.0;
        for (int i = 0; i < count; i++) {
            double angle = fxRandom.nextDouble() * Math.PI * 2.0;
            double speed = 45.0 + fxRandom.nextDouble() * 110.0;
            Color color = (i % 3 == 0) ? secondary : primary;
            double size = 3.0 + fxRandom.nextDouble() * 3.5;
            double life = 0.32 + fxRandom.nextDouble() * 0.25;
            particles.add(new Particle(cx, cy, Math.cos(angle) * speed, Math.sin(angle) * speed, color, size, life));
        }
    }

    private void spawnFloatingText(GridPoint cell, String text, Color color) {
        double cx = cell.x() * TILE_SIZE + TILE_SIZE / 2.0;
        double cy = cell.y() * TILE_SIZE + 8.0;
        floatingTexts.add(new FloatingText(cx, cy, text, color, 0.75));
    }

    private void updateVisualEffects(double dtSec) {
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            Particle p = pIt.next();
            p.x += p.vx * dtSec;
            p.y += p.vy * dtSec;
            p.vx *= 0.93;
            p.vy *= 0.93;
            p.life -= dtSec;
            if (p.life <= 0) {
                pIt.remove();
            }
        }

        Iterator<FloatingText> tIt = floatingTexts.iterator();
        while (tIt.hasNext()) {
            FloatingText ft = tIt.next();
            ft.y -= 34.0 * dtSec;
            ft.life -= dtSec;
            if (ft.life <= 0) {
                tIt.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHud(g2);

        g2.translate(0, HUD_HEIGHT);
        drawGrid(g2);
        drawClassicWallIndicator(g2);
        drawFood(g2);
        drawGoldenFood(g2);
        drawSnake(g2);
        drawParticles(g2);
        drawFloatingTexts(g2);

        switch (game.getState()) {
            case MENU -> drawMenuOverlay(g2);
            case PAUSED -> drawPauseOverlay(g2);
            case GAME_OVER -> drawGameOverOverlay(g2);
            case PLAYING -> {
            }
        }
        g2.translate(0, -HUD_HEIGHT);
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(HUD_BG_COLOR);
        g2.fillRect(0, 0, BOARD_WIDTH, HUD_HEIGHT);

        g2.setColor(HUD_BORDER_COLOR);
        g2.drawLine(0, HUD_HEIGHT - 1, BOARD_WIDTH, HUD_HEIGHT - 1);

        // Puntaje (izquierda arriba) + Modo/Dificultad (izquierda abajo)
        g2.setFont(HUD_FONT);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Puntaje: " + game.getScore(), 16, 24);

        g2.setFont(HUD_META_FONT);
        g2.setColor(TEXT_MUTED);
        g2.drawString(game.getGameMode().label() + " · " + game.getDifficulty().label(), 16, 43);

        // Récord (centro)
        g2.setFont(HUD_FONT);
        FontMetrics fm = g2.getFontMetrics();
        String recordText = "Récord: " + game.getHighScore();
        int recordWidth = fm.stringWidth(recordText);
        g2.setColor(game.isNewRecord() ? RECORD_GOLD : TEXT_PRIMARY);
        g2.drawString(recordText, (BOARD_WIDTH - recordWidth) / 2, 33);

        // Estado de sonido y pausa (derecha)
        g2.setFont(HUD_META_FONT);
        FontMetrics metaFm = g2.getFontMetrics();

        String soundLabel = soundEffects.isMuted() ? "M: Sonido OFF" : "M: Sonido ON";
        g2.setColor(soundEffects.isMuted() ? SNAKE_DEAD_HEAD_COLOR : TEXT_MUTED);
        g2.drawString(soundLabel, BOARD_WIDTH - metaFm.stringWidth(soundLabel) - 16, 22);

        String pauseLabel = game.isPaused() ? "Espacio: Reanudar" : "Espacio: Pausa";
        g2.setColor(TEXT_MUTED);
        g2.drawString(pauseLabel, BOARD_WIDTH - metaFm.stringWidth(pauseLabel) - 16, 42);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x <= BOARD_WIDTH; x += TILE_SIZE) {
            g2.drawLine(x, 0, x, BOARD_HEIGHT);
        }
        for (int y = 0; y <= BOARD_HEIGHT; y += TILE_SIZE) {
            g2.drawLine(0, y, BOARD_WIDTH, y);
        }
    }

    private void drawClassicWallIndicator(Graphics2D g2) {
        if (!game.getGameMode().wrapsWalls()) {
            g2.setColor(CLASSIC_WALL_COLOR);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(1, 1, BOARD_WIDTH - 3, BOARD_HEIGHT - 3);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawFood(Graphics2D g2) {
        GridPoint food = game.getFood();
        if (food == null) {
            return;
        }

        double pulse = Math.sin(animationTimeSec * 5.0) * 1.6;
        int size = (int) Math.round((TILE_SIZE - 8) + pulse);
        int px = food.x() * TILE_SIZE + (TILE_SIZE - size) / 2;
        int py = food.y() * TILE_SIZE + (TILE_SIZE - size) / 2 + 1;

        g2.setColor(FOOD_COLOR);
        g2.fillOval(px, py, size, size);

        g2.setColor(FOOD_HIGHLIGHT);
        g2.fillOval(px + 4, py + 4, 5, 5);

        g2.setColor(FOOD_LEAF_COLOR);
        g2.fillOval(px + size / 2 - 1, py - 3, 6, 4);
    }

    private void drawGoldenFood(Graphics2D g2) {
        GridPoint golden = game.getGoldenFood();
        if (golden == null) {
            return;
        }

        double pulse = Math.sin(animationTimeSec * 9.0) * 2.2;
        int baseSize = TILE_SIZE - 7;
        int size = (int) Math.round(baseSize + pulse);
        int cx = golden.x() * TILE_SIZE + TILE_SIZE / 2;
        int cy = golden.y() * TILE_SIZE + TILE_SIZE / 2;
        int px = cx - size / 2;
        int py = cy - size / 2;

        // Halo dorado pulsante
        int glowSize = size + 10;
        g2.setColor(GOLDEN_GLOW);
        g2.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);

        // Manzana dorada
        g2.setColor(GOLDEN_COLOR);
        g2.fillOval(px, py, size, size);

        g2.setColor(GOLDEN_HIGHLIGHT);
        g2.fillOval(px + 4, py + 4, 6, 6);

        // Barra de tiempo restante debajo de la manzana dorada
        float ratio = (float) game.getGoldenFoodTicksLeft() / SnakeGame.GOLDEN_FOOD_TICKS;
        int barW = TILE_SIZE - 4;
        int barX = golden.x() * TILE_SIZE + 2;
        int barY = golden.y() * TILE_SIZE + TILE_SIZE - 3;

        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRect(barX, barY, barW, 3);
        g2.setColor(GOLDEN_COLOR);
        g2.fillRect(barX, barY, Math.max(1, Math.round(barW * ratio)), 3);
    }

    private void drawSnake(Graphics2D g2) {
        int index = 0;
        int length = Math.max(1, game.getSnakeLength() - 1);

        for (GridPoint segment : game.getSnake()) {
            int px = segment.x() * TILE_SIZE + 2;
            int py = segment.y() * TILE_SIZE + 2;
            int size = TILE_SIZE - 4;

            if (index == 0) {
                g2.setColor(game.isGameOver() && !game.isWon() ? SNAKE_DEAD_HEAD_COLOR : SNAKE_HEAD_COLOR);
                g2.fillRoundRect(px, py, size, size, 10, 10);
                drawEyes(g2, px, py, size);
            } else {
                float ratio = (float) index / length;
                g2.setColor(blendColor(SNAKE_BODY_START, SNAKE_BODY_END, ratio));
                g2.fillRoundRect(px, py, size, size, 8, 8);
            }
            index++;
        }
    }

    private void drawEyes(Graphics2D g2, int px, int py, int size) {
        g2.setColor(EYE_COLOR);
        int eyeSize = 4;
        int offsetNear = 5;
        int offsetFar = size - offsetNear - eyeSize;

        switch (game.getCurrentDirection()) {
            case RIGHT -> {
                g2.fillOval(px + offsetFar, py + offsetNear, eyeSize, eyeSize);
                g2.fillOval(px + offsetFar, py + offsetFar, eyeSize, eyeSize);
            }
            case LEFT -> {
                g2.fillOval(px + offsetNear, py + offsetNear, eyeSize, eyeSize);
                g2.fillOval(px + offsetNear, py + offsetFar, eyeSize, eyeSize);
            }
            case UP -> {
                g2.fillOval(px + offsetNear, py + offsetNear, eyeSize, eyeSize);
                g2.fillOval(px + offsetFar, py + offsetNear, eyeSize, eyeSize);
            }
            case DOWN -> {
                g2.fillOval(px + offsetNear, py + offsetFar, eyeSize, eyeSize);
                g2.fillOval(px + offsetFar, py + offsetFar, eyeSize, eyeSize);
            }
        }
    }

    private void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            float alpha = Math.max(0f, Math.min(1f, (float) (p.life / p.maxLife)));
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.round(alpha * 255)));
            int d = Math.max(2, (int) Math.round(p.size * alpha));
            g2.fillOval((int) Math.round(p.x - d / 2.0), (int) Math.round(p.y - d / 2.0), d, d);
        }
    }

    private void drawFloatingTexts(Graphics2D g2) {
        g2.setFont(POPUP_FONT);
        FontMetrics fm = g2.getFontMetrics();
        for (FloatingText ft : floatingTexts) {
            float alpha = Math.max(0f, Math.min(1f, (float) (ft.life / ft.maxLife)));
            g2.setColor(new Color(ft.color.getRed(), ft.color.getGreen(), ft.color.getBlue(), Math.round(alpha * 255)));
            int x = (int) Math.round(ft.x - fm.stringWidth(ft.text) / 2.0);
            int y = (int) Math.round(ft.y);
            g2.drawString(ft.text, x, y);
        }
    }

    private void drawMenuOverlay(Graphics2D g2) {
        drawOverlayCard(g2, MENU_CARD_X, MENU_CARD_Y, MENU_CARD_W, MENU_CARD_H);

        drawCenteredText(g2, "SNAKE", TITLE_FONT, SNAKE_HEAD_COLOR, MENU_CARD_Y + 52);
        drawCenteredText(g2, "Elige modo y dificultad para empezar", HUD_META_FONT, TEXT_MUTED, MENU_CARD_Y + 76);

        drawMenuOptionRow(g2, MODE_ROW_RECT, "Modo", "<  " + game.getGameMode().label() + "  >", menuSelectedRow == 0);
        drawMenuOptionRow(g2, DIFF_ROW_RECT, "Dificultad", "<  " + game.getDifficulty().label() + "  >", menuSelectedRow == 1);

        // Botón JUGAR
        boolean playHighlighted = playButtonHovered || menuSelectedRow == 2;
        g2.setColor(playHighlighted ? BUTTON_GREEN_HOVER : BUTTON_GREEN);
        g2.fillRoundRect(PLAY_BTN_RECT.x, PLAY_BTN_RECT.y, PLAY_BTN_RECT.width, PLAY_BTN_RECT.height, 12, 12);

        g2.setFont(BUTTON_FONT);
        g2.setColor(EYE_COLOR);
        FontMetrics btnFm = g2.getFontMetrics();
        String playText = "JUGAR";
        int btnTextX = PLAY_BTN_RECT.x + (PLAY_BTN_RECT.width - btnFm.stringWidth(playText)) / 2;
        int btnTextY = PLAY_BTN_RECT.y + (PLAY_BTN_RECT.height + btnFm.getAscent() - btnFm.getDescent()) / 2;
        g2.drawString(playText, btnTextX, btnTextY);

        drawCenteredText(g2, "Flechas / Clic para elegir  •  Enter para jugar", HUD_META_FONT, TEXT_MUTED, MENU_CARD_Y + 290);
    }

    private void drawMenuOptionRow(Graphics2D g2, Rectangle rect, String label, String value, boolean selected) {
        if (selected) {
            g2.setColor(ROW_SELECTED_BG);
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g2.setColor(SNAKE_HEAD_COLOR);
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        } else {
            g2.setColor(new Color(30, 39, 53));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        }

        g2.setFont(MENU_LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(TEXT_MUTED);
        g2.drawString(label, rect.x + 16, textY);

        g2.setFont(MENU_VALUE_FONT);
        FontMetrics valFm = g2.getFontMetrics();
        g2.setColor(selected ? SNAKE_HEAD_COLOR : TEXT_PRIMARY);
        g2.drawString(value, rect.x + rect.width - valFm.stringWidth(value) - 16, textY);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        int cardW = 340;
        int cardH = 148;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        drawCenteredText(g2, "PAUSA", OVERLAY_TITLE_FONT, TEXT_PRIMARY, cardY + 54);
        drawCenteredText(g2, "Espacio o P para continuar", OVERLAY_SUBTITLE_FONT, TEXT_MUTED, cardY + 92);
        drawCenteredText(g2, "Esc para volver al menú", HUD_META_FONT, TEXT_MUTED, cardY + 122);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        int cardW = 370;
        int cardH = game.isNewRecord() ? 215 : 185;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        String title = game.isWon() ? "¡VICTORIA!" : "GAME OVER";
        Color titleColor = game.isWon() ? SNAKE_HEAD_COLOR : SNAKE_DEAD_HEAD_COLOR;
        drawCenteredText(g2, title, OVERLAY_TITLE_FONT, titleColor, cardY + 52);

        String scoreSummary = "Puntaje: " + game.getScore() + "   •   Récord: " + game.getHighScore();
        drawCenteredText(g2, scoreSummary, OVERLAY_SUBTITLE_FONT, TEXT_PRIMARY, cardY + 90);

        int hintY = cardY + 132;
        if (game.isNewRecord()) {
            drawCenteredText(g2, "★ ¡NUEVO RÉCORD! ★", MENU_VALUE_FONT, RECORD_GOLD, cardY + 124);
            hintY = cardY + 160;
        }

        drawCenteredText(g2, "Enter / R: Jugar de nuevo   •   Esc: Menú", HUD_META_FONT, TEXT_MUTED, hintY);
    }

    private void drawOverlayCard(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(OVERLAY_BACKDROP);
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g2.setColor(OVERLAY_CARD_BG);
        g2.fillRoundRect(x, y, width, height, 18, 18);

        g2.setColor(OVERLAY_CARD_BORDER);
        g2.drawRoundRect(x, y, width, height, 18, 18);
    }

    private void drawCenteredText(Graphics2D g2, String text, Font font, Color color, int baselineY) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int x = (BOARD_WIDTH - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, baselineY);
    }

    private Color blendColor(Color from, Color to, float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        int r = Math.round(from.getRed() + (to.getRed() - from.getRed()) * clamped);
        int g = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int b = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
        return new Color(r, g, b);
    }

    private static final class Particle {
        double x;
        double y;
        double vx;
        double vy;
        final Color color;
        final double size;
        final double maxLife;
        double life;

        Particle(double x, double y, double vx, double vy, Color color, double size, double maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.maxLife = maxLife;
            this.life = maxLife;
        }
    }

    private static final class FloatingText {
        final double x;
        double y;
        final String text;
        final Color color;
        final double maxLife;
        double life;

        FloatingText(double x, double y, String text, Color color, double maxLife) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.maxLife = maxLife;
            this.life = maxLife;
        }
    }
}
