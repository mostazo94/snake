package com.snake;

import java.awt.Color;

public enum GameTheme {
        CLASSIC_GREEN(
                        "Clásico Verde",
                        new Color(18, 22, 28),
                        new Color(13, 17, 23),
                        new Color(45, 55, 72),
                        new Color(30, 37, 46),
                        new Color(239, 68, 68, 175),
                        new Color(241, 245, 249),
                        new Color(148, 163, 184),
                        new Color(74, 222, 128),
                        new Color(250, 204, 21),
                        new Color(74, 222, 128),
                        new Color(248, 113, 113),
                        new Color(34, 197, 94),
                        new Color(21, 128, 61),
                        new Color(15, 23, 42),
                        new Color(239, 68, 68),
                        new Color(252, 165, 165),
                        new Color(74, 222, 128),
                        new Color(250, 204, 21),
                        new Color(254, 249, 195),
                        new Color(250, 204, 21, 75),
                        new Color(10, 14, 20, 185),
                        new Color(24, 31, 42, 245),
                        new Color(71, 85, 105),
                        new Color(36, 48, 66),
                        new Color(30, 39, 53),
                        new Color(34, 197, 94),
                        new Color(74, 222, 128),
                        new Color(15, 23, 42)),
        NEON_CYBERPUNK(
                        "Neón Cyberpunk",
                        new Color(11, 7, 25),
                        new Color(18, 11, 38),
                        new Color(109, 40, 217),
                        new Color(34, 22, 64),
                        new Color(244, 63, 94, 200),
                        new Color(245, 243, 255),
                        new Color(167, 139, 250),
                        new Color(34, 211, 238),
                        new Color(253, 224, 71),
                        new Color(34, 211, 238),
                        new Color(251, 113, 133),
                        new Color(232, 121, 249),
                        new Color(124, 58, 237),
                        new Color(11, 7, 25),
                        new Color(251, 113, 133),
                        new Color(255, 228, 230),
                        new Color(34, 211, 238),
                        new Color(253, 224, 71),
                        new Color(254, 249, 195),
                        new Color(253, 224, 71, 85),
                        new Color(8, 4, 20, 195),
                        new Color(22, 14, 48, 245),
                        new Color(139, 92, 246),
                        new Color(46, 26, 92),
                        new Color(31, 19, 64),
                        new Color(34, 211, 238),
                        new Color(103, 232, 249),
                        new Color(11, 7, 25)),
        RETRO_GAMEBOY(
                        "Retro GameBoy",
                        new Color(155, 188, 15),
                        new Color(15, 56, 15),
                        new Color(48, 98, 48),
                        new Color(139, 172, 15),
                        new Color(15, 56, 15, 220),
                        new Color(202, 220, 159),
                        new Color(139, 172, 15),
                        new Color(155, 188, 15),
                        new Color(215, 232, 148),
                        new Color(15, 56, 15),
                        new Color(48, 98, 48),
                        new Color(32, 74, 32),
                        new Color(48, 98, 48),
                        new Color(155, 188, 15),
                        new Color(15, 56, 15),
                        new Color(139, 172, 15),
                        new Color(48, 98, 48),
                        new Color(48, 98, 48),
                        new Color(202, 220, 159),
                        new Color(15, 56, 15, 65),
                        new Color(15, 56, 15, 165),
                        new Color(20, 62, 20, 245),
                        new Color(139, 172, 15),
                        new Color(40, 88, 40),
                        new Color(28, 74, 28),
                        new Color(155, 188, 15),
                        new Color(185, 214, 65),
                        new Color(15, 56, 15)),
        OCEAN(
                        "Océano",
                        new Color(7, 26, 47),
                        new Color(5, 19, 36),
                        new Color(30, 64, 104),
                        new Color(16, 44, 74),
                        new Color(251, 146, 60, 190),
                        new Color(224, 242, 254),
                        new Color(125, 211, 252),
                        new Color(45, 212, 191),
                        new Color(250, 204, 21),
                        new Color(45, 212, 191),
                        new Color(248, 113, 113),
                        new Color(6, 182, 212),
                        new Color(29, 78, 216),
                        new Color(7, 26, 47),
                        new Color(251, 146, 60),
                        new Color(254, 215, 170),
                        new Color(45, 212, 191),
                        new Color(250, 204, 21),
                        new Color(254, 249, 195),
                        new Color(250, 204, 21, 75),
                        new Color(4, 15, 28, 185),
                        new Color(11, 34, 59, 245),
                        new Color(56, 189, 248),
                        new Color(18, 52, 88),
                        new Color(14, 42, 72),
                        new Color(45, 212, 191),
                        new Color(94, 234, 212),
                        new Color(7, 26, 47));

        private final String label;
        private final Color bg;
        private final Color hudBg;
        private final Color hudBorder;
        private final Color grid;
        private final Color classicWall;
        private final Color textPrimary;
        private final Color textMuted;
        private final Color accent;
        private final Color recordGold;
        private final Color snakeHead;
        private final Color snakeDeadHead;
        private final Color snakeBodyStart;
        private final Color snakeBodyEnd;
        private final Color eye;
        private final Color food;
        private final Color foodHighlight;
        private final Color foodLeaf;
        private final Color golden;
        private final Color goldenHighlight;
        private final Color goldenGlow;
        private final Color overlayBackdrop;
        private final Color overlayCardBg;
        private final Color overlayCardBorder;
        private final Color rowSelectedBg;
        private final Color rowBg;
        private final Color buttonBg;
        private final Color buttonHover;
        private final Color buttonText;

        GameTheme(
                        String label,
                        Color bg,
                        Color hudBg,
                        Color hudBorder,
                        Color grid,
                        Color classicWall,
                        Color textPrimary,
                        Color textMuted,
                        Color accent,
                        Color recordGold,
                        Color snakeHead,
                        Color snakeDeadHead,
                        Color snakeBodyStart,
                        Color snakeBodyEnd,
                        Color eye,
                        Color food,
                        Color foodHighlight,
                        Color foodLeaf,
                        Color golden,
                        Color goldenHighlight,
                        Color goldenGlow,
                        Color overlayBackdrop,
                        Color overlayCardBg,
                        Color overlayCardBorder,
                        Color rowSelectedBg,
                        Color rowBg,
                        Color buttonBg,
                        Color buttonHover,
                        Color buttonText) {
                this.label = label;
                this.bg = bg;
                this.hudBg = hudBg;
                this.hudBorder = hudBorder;
                this.grid = grid;
                this.classicWall = classicWall;
                this.textPrimary = textPrimary;
                this.textMuted = textMuted;
                this.accent = accent;
                this.recordGold = recordGold;
                this.snakeHead = snakeHead;
                this.snakeDeadHead = snakeDeadHead;
                this.snakeBodyStart = snakeBodyStart;
                this.snakeBodyEnd = snakeBodyEnd;
                this.eye = eye;
                this.food = food;
                this.foodHighlight = foodHighlight;
                this.foodLeaf = foodLeaf;
                this.golden = golden;
                this.goldenHighlight = goldenHighlight;
                this.goldenGlow = goldenGlow;
                this.overlayBackdrop = overlayBackdrop;
                this.overlayCardBg = overlayCardBg;
                this.overlayCardBorder = overlayCardBorder;
                this.rowSelectedBg = rowSelectedBg;
                this.rowBg = rowBg;
                this.buttonBg = buttonBg;
                this.buttonHover = buttonHover;
                this.buttonText = buttonText;
        }

        public String label() {
                return label;
        }

        public Color bg() {
                return bg;
        }

        public Color hudBg() {
                return hudBg;
        }

        public Color hudBorder() {
                return hudBorder;
        }

        public Color grid() {
                return grid;
        }

        public Color classicWall() {
                return classicWall;
        }

        public Color textPrimary() {
                return textPrimary;
        }

        public Color textMuted() {
                return textMuted;
        }

        public Color accent() {
                return accent;
        }

        public Color recordGold() {
                return recordGold;
        }

        public Color snakeHead() {
                return snakeHead;
        }

        public Color snakeDeadHead() {
                return snakeDeadHead;
        }

        public Color snakeBodyStart() {
                return snakeBodyStart;
        }

        public Color snakeBodyEnd() {
                return snakeBodyEnd;
        }

        public Color eye() {
                return eye;
        }

        public Color food() {
                return food;
        }

        public Color foodHighlight() {
                return foodHighlight;
        }

        public Color foodLeaf() {
                return foodLeaf;
        }

        public Color golden() {
                return golden;
        }

        public Color goldenHighlight() {
                return goldenHighlight;
        }

        public Color goldenGlow() {
                return goldenGlow;
        }

        public Color overlayBackdrop() {
                return overlayBackdrop;
        }

        public Color overlayCardBg() {
                return overlayCardBg;
        }

        public Color overlayCardBorder() {
                return overlayCardBorder;
        }

        public Color rowSelectedBg() {
                return rowSelectedBg;
        }

        public Color rowBg() {
                return rowBg;
        }

        public Color buttonBg() {
                return buttonBg;
        }

        public Color buttonHover() {
                return buttonHover;
        }

        public Color buttonText() {
                return buttonText;
        }

        public GameTheme next() {
                GameTheme[] values = values();
                return values[(ordinal() + 1) % values.length];
        }

        public GameTheme previous() {
                GameTheme[] values = values();
                return values[(ordinal() - 1 + values.length) % values.length];
        }
}
