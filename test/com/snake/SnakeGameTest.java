package com.snake;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Pruebas unitarias en Java puro (sin dependencias externas) para la lógica del juego
 * y la persistencia del puntaje máximo.
 */
public class SnakeGameTest {
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        testInitialState();
        testMovementAndIgnoreOppositeTurn();
        testBufferedQuickTurns();
        testInfiniteWrapAroundEdges();
        testPauseStopsMovement();
        testEatingFoodGrowsSnakeAndUpdatesScore();
        testSelfCollisionAndResetPreservesHighScore();
        testSpeedScalingClampedAtMinimum();
        testHighScoreStorageRoundTrip();

        System.out.println("Todas las pruebas pasaron correctamente (" + passed + " tests).");
    }

    private static void testInitialState() {
        SnakeGame game = new SnakeGame(10, 10, 5, new Random(42));
        assertEquals(3, game.getSnakeLength(), "La serpiente debe empezar con longitud 3");
        assertEquals(Direction.RIGHT, game.getCurrentDirection(), "Debe empezar moviéndose a la derecha");
        assertEquals(0, game.getScore(), "El puntaje inicial debe ser 0");
        assertEquals(5, game.getHighScore(), "Debe conservar el récord inicial");
        assertTrue(game.getFood() != null, "Debe existir comida al iniciar");
        assertTrue(!game.isGameOver() && !game.isPaused(), "No debe iniciar pausado ni terminado");
        passed++;
    }

    private static void testMovementAndIgnoreOppositeTurn() {
        SnakeGame game = new SnakeGame(10, 10, 0, new Random(1));
        GridPoint initialHead = firstSegment(game);

        // Intento de giro de 180° (de RIGHT a LEFT): debe ignorarse
        game.queueDirection(Direction.LEFT);
        game.update();

        GridPoint newHead = firstSegment(game);
        assertEquals(Direction.RIGHT, game.getCurrentDirection(), "No debe permitir giro de 180°");
        assertEquals(new GridPoint(initialHead.x() + 1, initialHead.y()), newHead, "Debe avanzar a la derecha");
        passed++;
    }

    private static void testBufferedQuickTurns() {
        SnakeGame game = new SnakeGame(10, 10, 0, new Random(2));
        game.queueDirection(Direction.UP);
        game.queueDirection(Direction.LEFT);

        game.update();
        assertEquals(Direction.UP, game.getCurrentDirection(), "Primer tick debe girar hacia arriba");

        game.update();
        assertEquals(Direction.LEFT, game.getCurrentDirection(), "Segundo tick debe girar hacia la izquierda");
        passed++;
    }

    private static void testInfiniteWrapAroundEdges() {
        // Tablero pequeño de 6x6; cabeza inicial en (3, 3) moviéndose a la derecha
        SnakeGame game = new SnakeGame(6, 6, 0, new Random(10));

        // Avanzamos 3 veces: x pasa 3 -> 4 -> 5 -> 0 (wrap horizontal)
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        assertTrue(!game.isGameOver(), "Cruzar el borde no debe provocar Game Over");
        assertEquals(0, firstSegment(game).x(), "Debe reaparecer en x = 0 al cruzar el borde derecho");
        passed++;
    }

    private static void testPauseStopsMovement() {
        SnakeGame game = new SnakeGame(10, 10, 0, new Random(3));
        GridPoint before = firstSegment(game);

        game.togglePause();
        assertTrue(game.isPaused(), "El juego debe estar pausado");

        game.queueDirection(Direction.UP);
        game.update();

        assertEquals(before, firstSegment(game), "La serpiente no debe moverse en pausa");
        assertEquals(Direction.RIGHT, game.getCurrentDirection(), "No debe cambiar dirección en pausa");

        game.togglePause();
        assertTrue(!game.isPaused(), "Debe reanudar al volver a pausar");
        passed++;
    }

    private static void testEatingFoodGrowsSnakeAndUpdatesScore() {
        // Random determinista que siempre devuelve 0 -> la comida aparece en la primera celda libre (0, 0)
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };

        SnakeGame game = new SnakeGame(6, 6, 0, zeroRandom);
        assertEquals(new GridPoint(0, 0), game.getFood(), "La comida determinista debe estar en (0, 0)");

        // Cabeza inicia en (3, 3). Subimos 3 veces -> (3, 0), luego derecha 3 veces -> (0, 0)
        game.queueDirection(Direction.UP);
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        game.queueDirection(Direction.RIGHT);
        for (int i = 0; i < 3; i++) {
            game.update();
        }

        assertEquals(1, game.getScore(), "El puntaje debe subir a 1 al comer");
        assertEquals(1, game.getHighScore(), "El récord debe actualizarse a 1");
        assertTrue(game.isNewRecord(), "Debe marcar nuevo récord");
        assertEquals(4, game.getSnakeLength(), "La serpiente debe crecer de 3 a 4 segmentos");
        passed++;
    }

    private static void testSelfCollisionAndResetPreservesHighScore() {
        // Hacemos crecer la serpiente a 5 segmentos comiendo 2 veces y luego giramos sobre nuestro cuerpo
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };

        SnakeGame game = new SnakeGame(6, 6, 0, zeroRandom);

        // Comer en (0, 0): subir 3 -> (3, 0), derecha 3 -> (0, 0)
        game.queueDirection(Direction.UP);
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        game.queueDirection(Direction.RIGHT);
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        assertEquals(1, game.getScore(), "Debe haber comido la primera manzana");

        // La segunda manzana aparece en (1, 0) porque (0, 0) ahora está ocupada por la cabeza
        assertEquals(new GridPoint(1, 0), game.getFood(), "La segunda manzana debe estar en (1, 0)");
        game.update(); // ahora cabeza en (1, 0), longitud 5
        assertEquals(5, game.getSnakeLength(), "Longitud debe ser 5 para poder chocar consigo misma");

        // Giro en U cerrado: DOWN -> LEFT -> UP para chocar con el propio cuerpo
        game.queueDirection(Direction.DOWN);
        game.update();
        game.queueDirection(Direction.LEFT);
        game.update();
        game.queueDirection(Direction.UP);
        game.update();

        assertTrue(game.isGameOver(), "Chocar con el propio cuerpo debe terminar la partida");
        assertEquals(2, game.getHighScore(), "El récord debe ser 2");

        game.reset();
        assertTrue(!game.isGameOver(), "Reset debe limpiar Game Over");
        assertEquals(0, game.getScore(), "Reset debe reiniciar el puntaje actual");
        assertEquals(2, game.getHighScore(), "Reset debe mantener el récord acumulado");
        passed++;
    }

    private static void testSpeedScalingClampedAtMinimum() {
        SnakeGame game = new SnakeGame(10, 10, 0, new Random(5));
        assertEquals(SnakeGame.INITIAL_TICK_MS, game.getTickDelayMs(), "Velocidad inicial debe ser INITIAL_TICK_MS");
        assertTrue(SnakeGame.MIN_TICK_MS < SnakeGame.INITIAL_TICK_MS, "MIN_TICK_MS debe ser menor a INITIAL_TICK_MS");
        passed++;
    }

    private static void testHighScoreStorageRoundTrip() throws Exception {
        Path tempFile = Files.createTempFile("snake-highscore-test-", ".txt");
        try {
            Files.deleteIfExists(tempFile);
            HighScoreStorage storage = new HighScoreStorage(tempFile);

            assertEquals(0, storage.load(), "Sin archivo debe cargar 0");
            storage.save(42);
            assertEquals(42, storage.load(), "Debe recuperar el récord guardado");
        } finally {
            Files.deleteIfExists(tempFile);
        }
        passed++;
    }

    private static GridPoint firstSegment(SnakeGame game) {
        return game.getSnake().iterator().next();
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " | esperado: " + expected + ", obtenido: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
