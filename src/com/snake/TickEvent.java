package com.snake;

/**
 * Resume qué ocurrió en un tick del juego para que la UI pueda disparar
 * efectos visuales y sonidos sin acoplar Swing dentro de la lógica pura.
 */
public record TickEvent(
        boolean ateFood,
        boolean ateGoldenFood,
        GridPoint eatenAt,
        boolean goldenSpawned,
        boolean gameOverTriggered,
        boolean wonTriggered
) {
    public static final TickEvent NONE = new TickEvent(false, false, null, false, false, false);
}
