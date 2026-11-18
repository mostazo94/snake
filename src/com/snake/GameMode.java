package com.snake;

public enum GameMode {
    INFINITE("Infinito", true),
    CLASSIC("Clásico", false);

    private final String label;
    private final boolean wrapWalls;

    GameMode(String label, boolean wrapWalls) {
        this.label = label;
        this.wrapWalls = wrapWalls;
    }

    public String label() {
        return label;
    }

    public boolean wrapsWalls() {
        return wrapWalls;
    }

    public GameMode next() {
        GameMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public GameMode previous() {
        GameMode[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
