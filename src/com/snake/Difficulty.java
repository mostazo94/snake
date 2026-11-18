package com.snake;

public enum Difficulty {
    EASY("Fácil", 160, 90, 2),
    NORMAL("Normal", 125, 65, 3),
    HARD("Difícil", 95, 50, 4);

    private final String label;
    private final int initialTickMs;
    private final int minTickMs;
    private final int speedStepMs;

    Difficulty(String label, int initialTickMs, int minTickMs, int speedStepMs) {
        this.label = label;
        this.initialTickMs = initialTickMs;
        this.minTickMs = minTickMs;
        this.speedStepMs = speedStepMs;
    }

    public String label() {
        return label;
    }

    public int initialTickMs() {
        return initialTickMs;
    }

    public int minTickMs() {
        return minTickMs;
    }

    public int speedStepMs() {
        return speedStepMs;
    }

    public int tickDelayForScore(int score) {
        return Math.max(minTickMs, initialTickMs - score * speedStepMs);
    }

    public Difficulty next() {
        Difficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Difficulty previous() {
        Difficulty[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
