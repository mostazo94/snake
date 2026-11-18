package com.snake;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    public static final int COLS = 20;
    public static final int ROWS = 20;
    public static final int TILE_SIZE = 28;
    private static final int HUD_HEIGHT = 48;

    private static final int BOARD_WIDTH = COLS * TILE_SIZE;
    private static final int BOARD_HEIGHT = ROWS * TILE_SIZE;

    private static final Color BG_COLOR = new Color(18, 22, 28);
    private static final Color HUD_BG_COLOR = new Color(13, 17, 23);
    private static final Color HUD_BORDER_COLOR = new Color(45, 55, 72);
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

    private static final Color OVERLAY_BACKDROP = new Color(10, 14, 20, 185);
    private static final Color OVERLAY_CARD_BG = new Color(24, 31, 42, 240);
    private static final Color OVERLAY_CARD_BORDER = new Color(71, 85, 105);

    private static final Font HUD_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font HUD_HINT_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font OVERLAY_TITLE_FONT = new Font("SansSerif", Font.BOLD, 30);
    private static final Font OVERLAY_SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font OVERLAY_BADGE_FONT = new Font("SansSerif", Font.BOLD, 15);

    private final HighScoreStorage highScoreStorage;
    private final SnakeGame game;
    private final Timer timer;
    private int savedHighScore;

    public GamePanel() {
        this.highScoreStorage = new HighScoreStorage();
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

        this.timer = new Timer(game.getTickDelayMs(), e -> onTick());
        this.timer.start();
    }

    private void onTick() {
        game.update();
        syncSpeedAndHighScore();
        repaint();
    }

    private void syncSpeedAndHighScore() {
        int desiredDelay = game.getTickDelayMs();
        if (timer.getDelay() != desiredDelay) {
            timer.setDelay(desiredDelay);
        }
        if (game.getHighScore() > savedHighScore) {
            savedHighScore = game.getHighScore();
            highScoreStorage.save(savedHighScore);
        }
    }

    private void handleKeyPress(int keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                game.reset();
                timer.setDelay(game.getTickDelayMs());
                repaint();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_P) {
            game.togglePause();
            repaint();
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> game.queueDirection(Direction.UP);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> game.queueDirection(Direction.DOWN);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> game.queueDirection(Direction.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> game.queueDirection(Direction.RIGHT);
            default -> {
                // Ignorar otras teclas
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

        // Desplazamos el origen debajo del HUD para dibujar el tablero limpiamente
        g2.translate(0, HUD_HEIGHT);
        drawGrid(g2);
        drawFood(g2);
        drawSnake(g2);

        if (game.isPaused()) {
            drawPauseOverlay(g2);
        } else if (game.isGameOver()) {
            drawGameOverOverlay(g2);
        }
        g2.translate(0, -HUD_HEIGHT);
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(HUD_BG_COLOR);
        g2.fillRect(0, 0, BOARD_WIDTH, HUD_HEIGHT);

        g2.setColor(HUD_BORDER_COLOR);
        g2.drawLine(0, HUD_HEIGHT - 1, BOARD_WIDTH, HUD_HEIGHT - 1);

        g2.setFont(HUD_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int textY = (HUD_HEIGHT + fm.getAscent() - fm.getDescent()) / 2;

        // Puntaje actual (izquierda)
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Puntaje: " + game.getScore(), 16, textY);

        // Récord (centro)
        String recordText = "Récord: " + game.getHighScore();
        int recordWidth = fm.stringWidth(recordText);
        g2.setColor(game.isNewRecord() ? RECORD_GOLD : TEXT_PRIMARY);
        g2.drawString(recordText, (BOARD_WIDTH - recordWidth) / 2, textY);

        // Ayuda de pausa (derecha)
        g2.setFont(HUD_HINT_FONT);
        FontMetrics hintFm = g2.getFontMetrics();
        String hint = game.isPaused() ? "Espacio: Reanudar" : "Espacio: Pausa";
        int hintWidth = hintFm.stringWidth(hint);
        g2.setColor(TEXT_MUTED);
        g2.drawString(hint, BOARD_WIDTH - hintWidth - 16, textY);
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

    private void drawFood(Graphics2D g2) {
        GridPoint food = game.getFood();
        if (food == null) {
            return;
        }

        int px = food.x() * TILE_SIZE + 4;
        int py = food.y() * TILE_SIZE + 5;
        int size = TILE_SIZE - 8;

        g2.setColor(FOOD_COLOR);
        g2.fillOval(px, py, size, size);

        g2.setColor(FOOD_HIGHLIGHT);
        g2.fillOval(px + 4, py + 4, 5, 5);

        g2.setColor(FOOD_LEAF_COLOR);
        g2.fillOval(px + size / 2 - 1, py - 3, 6, 4);
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

    private void drawPauseOverlay(Graphics2D g2) {
        int cardW = 320;
        int cardH = 130;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        drawCenteredText(g2, "PAUSA", OVERLAY_TITLE_FONT, TEXT_PRIMARY, cardY + 56);
        drawCenteredText(g2, "Presiona Espacio o P para continuar", OVERLAY_SUBTITLE_FONT, TEXT_MUTED, cardY + 96);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        int cardW = 350;
        int cardH = game.isNewRecord() ? 195 : 165;
        int cardX = (BOARD_WIDTH - cardW) / 2;
        int cardY = (BOARD_HEIGHT - cardH) / 2;

        drawOverlayCard(g2, cardX, cardY, cardW, cardH);

        String title = game.isWon() ? "¡VICTORIA!" : "GAME OVER";
        Color titleColor = game.isWon() ? SNAKE_HEAD_COLOR : SNAKE_DEAD_HEAD_COLOR;
        drawCenteredText(g2, title, OVERLAY_TITLE_FONT, titleColor, cardY + 54);

        String scoreSummary = "Puntaje: " + game.getScore() + "   •   Récord: " + game.getHighScore();
        drawCenteredText(g2, scoreSummary, OVERLAY_SUBTITLE_FONT, TEXT_PRIMARY, cardY + 92);

        int hintY = cardY + 132;
        if (game.isNewRecord()) {
            drawCenteredText(g2, "★ ¡NUEVO RÉCORD! ★", OVERLAY_BADGE_FONT, RECORD_GOLD, cardY + 126);
            hintY = cardY + 162;
        }

        drawCenteredText(g2, "Presiona Enter, Espacio o R para reiniciar", HUD_HINT_FONT, TEXT_MUTED, hintY);
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
}
