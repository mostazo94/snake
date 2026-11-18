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

    private static final Font HUD_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font HUD_META_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 34);
    private static final Font OVERLAY_TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font OVERLAY_SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font MENU_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font MENU_VALUE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font POPUP_FONT = new Font("SansSerif", Font.BOLD, 14);

    // Rectángulos clicables del menú (en coordenadas del tablero)
    private static final int MENU_CARD_W = 410;
    private static final int MENU_CARD_H = 362;
    private static final int MENU_CARD_X = (BOARD_WIDTH - MENU_CARD_W) / 2;
    private static final int MENU_CARD_Y = (BOARD_HEIGHT - MENU_CARD_H) / 2;
    private static final Rectangle MODE_ROW_RECT = new Rectangle(MENU_CARD_X + 26, MENU_CARD_Y + 100, MENU_CARD_W - 52, 42);
    private static final Rectangle DIFF_ROW_RECT = new Rectangle(MENU_CARD_X + 26, MENU_CARD_Y + 150, MENU_CARD_W - 52, 42);
    private static final Rectangle THEME_ROW_RECT = new Rectangle(MENU_CARD_X + 26, MENU_CARD_Y + 200, MENU_CARD_W - 52, 42);
    private static final Rectangle PLAY_BTN_RECT = new Rectangle(MENU_CARD_X + 105, MENU_CARD_Y + 264, MENU_CARD_W - 210, 44);

    private final HighScoreStorage highScoreStorage;
    private final SoundEffects soundEffects;
    private final SnakeGame game;
    private final Timer frameTimer;
    private final Random fxRandom = new Random();

    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private GameTheme theme = GameTheme.CLASSIC_GREEN;
    private int savedHighScore;
    private int menuSelectedRow = 0; // 0 = Modo, 1 = Dificultad, 2 = Tema, 3 = Jugar
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
        setBackground(theme.bg());
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

    private void setTheme(GameTheme newTheme) {
        this.theme = newTheme;
        setBackground(theme.bg());
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
            spawnParticles(event.eatenAt(), theme.food(), theme.foodHighlight(), 12);
            spawnFloatingText(event.eatenAt(), "+1", theme.textPrimary());
            soundEffects.playEat();
        }
        if (event.ateGoldenFood() && event.eatenAt() != null) {
            spawnParticles(event.eatenAt(), theme.golden(), theme.goldenHighlight(), 24);
            spawnFloatingText(event.eatenAt(), "+" + SnakeGame.GOLDEN_FOOD_POINTS, theme.recordGold());
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
        if (keyCode == KeyEvent.VK_T) {
            setTheme(theme.next());
            soundEffects.playClick();
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
                menuSelectedRow = (menuSelectedRow + 3) % 4;
                soundEffects.playClick();
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                menuSelectedRow = (menuSelectedRow + 1) % 4;
                soundEffects.playClick();
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (menuSelectedRow == 0) {
                    game.setGameMode(game.getGameMode().previous());
                    soundEffects.playClick();
                } else if (menuSelectedRow == 1) {
                    game.setDifficulty(game.getDifficulty().previous());
                    soundEffects.playClick();
                } else if (menuSelectedRow == 2) {
                    setTheme(theme.previous());
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
                } else if (menuSelectedRow == 2) {
                    setTheme(theme.next());
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
                || THEME_ROW_RECT.contains(boardPoint)
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
        } else if (THEME_ROW_RECT.contains(boardPoint)) {
            menuSelectedRow = 2;
            setTheme(theme.next());
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
        drawBoardBackground(g2);
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
        g2.setColor(theme.hudBg());
        g2.fillRect(0, 0, BOARD_WIDTH, HUD_HEIGHT);

        g2.setColor(theme.hudBorder());
        g2.drawLine(0, HUD_HEIGHT - 1, BOARD_WIDTH, HUD_HEIGHT - 1);

        // Puntaje (izquierda arriba) + Modo/Dificultad (izquierda abajo)
        g2.setFont(HUD_FONT);
        g2.setColor(theme.textPrimary());
        g2.drawString("Puntaje: " + game.getScore(), 16, 24);

        g2.setFont(HUD_META_FONT);
        g2.setColor(theme.textMuted());
        g2.drawString(game.getGameMode().label() + " · " + game.getDifficulty().label(), 16, 43);

        // Récord (centro)
        g2.setFont(HUD_FONT);
        FontMetrics fm = g2.getFontMetrics();
        String recordText = "Récord: " + game.getHighScore();
        int recordWidth = fm.stringWidth(recordText);
        g2.setColor(game.isNewRecord() ? theme.recordGold() : theme.textPrimary());
        g2.drawString(recordText, (BOARD_WIDTH - recordWidth) / 2, 33);

        // Estado de sonido y atajos (derecha)
        g2.setFont(HUD_META_FONT);
        FontMetrics metaFm = g2.getFontMetrics();

        String soundLabel = soundEffects.isMuted() ? "M: Sonido OFF" : "M: Sonido ON";
        g2.setColor(soundEffects.isMuted() ? theme.snakeDeadHead() : theme.textMuted());
        g2.drawString(soundLabel, BOARD_WIDTH - metaFm.stringWidth(soundLabel) - 16, 22);

        String controlsLabel = game.isPaused() ? "T: Tema · Espacio: Reanudar" : "T: Tema · Espacio: Pausa";
        g2.setColor(theme.textMuted());
        g2.drawString(controlsLabel, BOARD_WIDTH - metaFm.stringWidth(controlsLabel) - 16, 42);
    }

    private void drawBoardBackground(Graphics2D g2) {
        g2.setColor(theme.bg());
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(theme.grid());
        for (int x = 0; x <= BOARD_WIDTH; x += TILE_SIZE) {
            g2.drawLine(x, 0, x, BOARD_HEIGHT);
        }
        for (int y = 0; y <= BOARD_HEIGHT; y += TILE_SIZE) {
            g2.drawLine(0, y, BOARD_WIDTH, y);
        }
    }

    private void drawClassicWallIndicator(Graphics2D g2) {
        if (!game.getGameMode().wrapsWalls()) {
            g2.setColor(theme.classicWall());
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

        g2.setColor(theme.food());
        g2.fillOval(px, py, size, size);

        g2.setColor(theme.foodHighlight());
        g2.fillOval(px + 4, py + 4, 5, 5);

        g2.setColor(theme.foodLeaf());
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
        g2.setColor(theme.goldenGlow());
        g2.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);

        // Manzana dorada
        g2.setColor(theme.golden());
        g2.fillOval(px, py, size, size);

        g2.setColor(theme.goldenHighlight());
        g2.fillOval(px + 4, py + 4, 6, 6);

        // Barra de tiempo restante debajo de la manzana dorada
        float ratio = (float) game.getGoldenFoodTicksLeft() / SnakeGame.GOLDEN_FOOD_TICKS;
        int barW = TILE_SIZE - 4;
        int barX = golden.x() * TILE_SIZE + 2;
        int barY = golden.y() * TILE_SIZE + TILE_SIZE - 3;

        g2.setColor(theme.hudBg());
        g2.fillRect(barX, barY, barW, 3);
        g2.setColor(theme.golden());
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
                g2.setColor(game.isGameOver() && !game.isWon() ? theme.snakeDeadHead() : theme.snakeHead());
                g2.fillRoundRect(px, py, size, size, 10, 10);
                drawEyes(g2, px, py, size);
            } else {
                float ratio = (float) index / length;
                g2.setColor(blendColor(theme.snakeBodyStart(), theme.snakeBodyEnd(), ratio));
                g2.fillRoundRect(px, py, size, size, 8, 8);
            }
            index++;
        }
    }

    private void drawEyes(Graphics2D g2, int px, int py, int size) {
        g2.setColor(theme.eye());
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

        drawCenteredText(g2, "SNAKE", TITLE_FONT, theme.accent(), MENU_CARD_Y + 50);
        drawCenteredText(g2, "Personaliza tu partida y presiona Jugar", HUD_META_FONT, theme.textMuted(), MENU_CARD_Y + 74);

        drawMenuOptionRow(g2, MODE_ROW_RECT, "Modo", "<  " + game.getGameMode().label() + "  >", menuSelectedRow == 0, false);
        drawMenuOptionRow(g2, DIFF_ROW_RECT, "Dificultad", "<  " + game.getDifficulty().label() + "  >", menuSelectedRow == 1, false);
        drawMenuOptionRow(g2, THEME_ROW_RECT, "Tema", "<  " + theme.label() + "  >", menuSelectedRow == 2, true);

        // Botón JUGAR
        boolean playHighlighted = playButtonHovered || menuSelectedRow == 3;
        g2.setColor(playHighlighted ? theme.buttonHover() : theme.buttonBg());
        g2.fillRoundRect(PLAY_BTN_RECT.x, PLAY_BTN_RECT.y, PLAY_BTN_RECT.width, PLAY_BTN_RECT.height, 12, 12);

        g2.setFont(BUTTON_FONT);
        g2.setColor(theme.buttonText());
        FontMetrics btnFm = g2.getFontMetrics();
        String playText = "JUGAR";
        int btnTextX = PLAY_BTN_RECT.x + (PLAY_BTN_RECT.width - btnFm.stringWidth(playText)) / 2;
        int btnTextY = PLAY_BTN_RECT.y + (PLAY_BTN_RECT.height + btnFm.getAscent() - btnFm.getDescent()) / 2;
        g2.drawString(playText, btnTextX, btnTextY);

        drawCenteredText(g2, "Flechas / Clic para elegir  •  Enter para jugar", HUD_META_FONT, theme.textMuted(), MENU_CARD_Y + 340);
    }

    private void drawMenuOptionRow(Graphics2D g2, Rectangle rect, String label, String value, boolean selected, boolean showThemeSwatches) {
        if (selected) {
            g2.setColor(theme.rowSelectedBg());
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g2.setColor(theme.accent());
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        } else {
            g2.setColor(theme.rowBg());
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        }

        g2.setFont(MENU_LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(theme.textMuted());
        g2.drawString(label, rect.x + 16, textY);

        if (showThemeSwatches) {
            int swatchY = rect.y + (rect.height - 10) / 2;
            int swatchX = rect.x + 70;
            drawSwatch(g2, swatchX, swatchY, theme.bg());
            drawSwatch(g2, swatchX + 14, swatchY, theme.snakeHead());
            drawSwatch(g2, swatchX + 28, swatchY, theme.food());
        }

        g2.setFont(MENU_VALUE_FONT);
        FontMetrics valFm = g2.getFontMetrics();
        g2.setColor(selected ? theme.accent() : theme.textPrimary());
        g2.drawString(value, rect.x + rect.width - valFm.stringWidth(value) - 16, textY);
    }

    private void drawSwatch(Graphics2D g2, int x, int y, Color color) {
        g2.setColor(color);
        g2.fillOval(x, y, 10, 10);
        g2.setColor(theme.overlayCardBorder());
        g2.drawOval(x, y, 10, 10);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        int cardW = 340;
        int cardH = 152;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        drawCenteredText(g2, "PAUSA", OVERLAY_TITLE_FONT, theme.textPrimary(), cardY + 52);
        drawCenteredText(g2, "Espacio o P para continuar", OVERLAY_SUBTITLE_FONT, theme.textMuted(), cardY + 90);
        drawCenteredText(g2, "T: Cambiar tema   •   Esc: Menú", HUD_META_FONT, theme.textMuted(), cardY + 122);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        int cardW = 370;
        int cardH = game.isNewRecord() ? 215 : 185;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        String title = game.isWon() ? "¡VICTORIA!" : "GAME OVER";
        Color titleColor = game.isWon() ? theme.accent() : theme.snakeDeadHead();
        drawCenteredText(g2, title, OVERLAY_TITLE_FONT, titleColor, cardY + 52);

        String scoreSummary = "Puntaje: " + game.getScore() + "   •   Récord: " + game.getHighScore();
        drawCenteredText(g2, scoreSummary, OVERLAY_SUBTITLE_FONT, theme.textPrimary(), cardY + 90);

        int hintY = cardY + 132;
        if (game.isNewRecord()) {
            drawCenteredText(g2, "★ ¡NUEVO RÉCORD! ★", MENU_VALUE_FONT, theme.recordGold(), cardY + 124);
            hintY = cardY + 160;
        }

        drawCenteredText(g2, "Enter / R: Jugar de nuevo   •   Esc: Menú", HUD_META_FONT, theme.textMuted(), hintY);
    }

    private void drawOverlayCard(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(theme.overlayBackdrop());
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g2.setColor(theme.overlayCardBg());
        g2.fillRoundRect(x, y, width, height, 18, 18);

        g2.setColor(theme.overlayCardBorder());
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
