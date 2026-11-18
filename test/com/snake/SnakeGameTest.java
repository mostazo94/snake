package com.snake;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Pruebas unitarias en Java puro (sin dependencias externas) para la lógica del juego,
 * modos, dificultades, manzana dorada y persistencia del puntaje máximo.
 */
public class SnakeGameTest {
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        testInitialMenuStateAndStart();
        testMovementAndIgnoreOppositeTurn();
        testBufferedQuickTurns();
        testInfiniteWrapAroundEdges();
        testClassicModeWallCollision();
        testPauseStopsMovement();
        testEatingFoodGrowsSnakeAndUpdatesScore();
        testGoldenAppleSpawnsRewardsThreePointsAndExpires();
        testSelfCollisionAndResetPreservesHighScore();
        testDifficultySpeedOrdering();
        testThemesCycleCleanly();
        testHighScoreStorageRoundTrip();

        System.out.println("Todas las pruebas pasaron correctamente (" + passed + " tests).");
    }

    private static void testInitialMenuStateAndStart() {
        SnakeGame game = new SnakeGame(10, 10, 5, new Random(42));
        assertTrue(game.isInMenu(), "El juego debe iniciar en el menú");
        assertEquals(3, game.getSnakeLength(), "La serpiente debe empezar con longitud 3");
        assertEquals(Direction.RIGHT, game.getCurrentDirection(), "Debe empezar moviéndose a la derecha");
        assertEquals(0, game.getScore(), "El puntaje inicial debe ser 0");
        assertEquals(5, game.getHighScore(), "Debe conservar el récord inicial");
        assertTrue(game.getFood() != null, "Debe existir comida al iniciar");

        GridPoint before = firstSegment(game);
        game.update();
        assertEquals(before, firstSegment(game), "En el menú la serpiente no debe avanzar");

        game.startGame();
        assertEquals(SnakeGame.State.PLAYING, game.getState(), "startGame() debe pasar a estado PLAYING");
        passed++;
    }

    private static void testMovementAndIgnoreOppositeTurn() {
        SnakeGame game = startPlayingGame(10, 10, 0, new Random(1));
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
        SnakeGame game = startPlayingGame(10, 10, 0, new Random(2));
        game.queueDirection(Direction.UP);
        game.queueDirection(Direction.LEFT);

        game.update();
        assertEquals(Direction.UP, game.getCurrentDirection(), "Primer tick debe girar hacia arriba");

        game.update();
        assertEquals(Direction.LEFT, game.getCurrentDirection(), "Segundo tick debe girar hacia la izquierda");
        passed++;
    }

    private static void testInfiniteWrapAroundEdges() {
        // Tablero 6x6; cabeza inicial en (3, 3) moviéndose a la derecha
        SnakeGame game = startPlayingGame(6, 6, 0, new Random(10));
        game.setGameMode(GameMode.INFINITE);

        // Avanzamos 3 veces: x pasa 3 -> 4 -> 5 -> 0 (wrap horizontal)
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        assertTrue(!game.isGameOver(), "Cruzar el borde en modo Infinito no debe provocar Game Over");
        assertEquals(0, firstSegment(game).x(), "Debe reaparecer en x = 0 al cruzar el borde derecho");
        passed++;
    }

    private static void testClassicModeWallCollision() {
        SnakeGame game = new SnakeGame(6, 6, 0, new Random(10));
        game.setGameMode(GameMode.CLASSIC);
        game.startGame();

        // Cabeza inicial en (3, 3): 2 pasos -> (5, 3), tercer paso -> choca con pared derecha
        game.update();
        game.update();
        assertTrue(!game.isGameOver(), "Aún está dentro del tablero en x = 5");

        TickEvent event = game.update();
        assertTrue(game.isGameOver(), "Chocar con la pared en modo Clásico debe provocar Game Over");
        assertTrue(event.gameOverTriggered(), "TickEvent debe informar gameOverTriggered");
        passed++;
    }

    private static void testPauseStopsMovement() {
        SnakeGame game = startPlayingGame(10, 10, 0, new Random(3));
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
        Random zeroRandom = constantZeroRandom();

        SnakeGame game = startPlayingGame(6, 6, 0, zeroRandom);
        assertEquals(new GridPoint(0, 0), game.getFood(), "La comida determinista debe estar en (0, 0)");

        // Cabeza inicia en (3, 3). Subimos 3 veces -> (3, 0), luego derecha 3 veces -> (0, 0)
        game.queueDirection(Direction.UP);
        for (int i = 0; i < 3; i++) {
            game.update();
        }
        game.queueDirection(Direction.RIGHT);
        TickEvent lastEvent = TickEvent.NONE;
        for (int i = 0; i < 3; i++) {
            lastEvent = game.update();
        }

        assertTrue(lastEvent.ateFood(), "Debe reportar ateFood en TickEvent");
        assertEquals(1, game.getScore(), "El puntaje debe subir a 1 al comer");
        assertEquals(1, game.getHighScore(), "El récord debe actualizarse a 1");
        assertTrue(game.isNewRecord(), "Debe marcar nuevo récord");
        assertEquals(4, game.getSnakeLength(), "La serpiente debe crecer de 3 a 4 segmentos");
        passed++;
    }

    private static void testGoldenAppleSpawnsRewardsThreePointsAndExpires() {
        // Tablero 10x10 con Random que siempre elige la primera celda libre (0, 0), (1, 0), (2, 0)...
        SnakeGame game = startPlayingGame(10, 10, 0, constantZeroRandom());

        // Cabeza inicia en (5, 5). Subimos 5 -> (5, 0), luego derecha 5 -> (0, 0) (manzana 1)
        game.queueDirection(Direction.UP);
        for (int i = 0; i < 5; i++) {
            game.update();
        }
        game.queueDirection(Direction.RIGHT);
        for (int i = 0; i < 5; i++) {
            game.update();
        }
        assertEquals(1, game.getScore(), "Debe haber comido la manzana 1 en (0, 0)");

        // Seguimos avanzando a la derecha por la fila y = 0 para comer manzanas 2, 3, 4 y 5 en (1,0)..(4,0)
        TickEvent fifthEat = TickEvent.NONE;
        for (int i = 0; i < 4; i++) {
            fifthEat = game.update();
        }

        assertEquals(5, game.getScore(), "Debe haber comido 5 manzanas normales");
        assertTrue(fifthEat.goldenSpawned(), "Tras la 5.ª manzana debe aparecer una manzana dorada");
        assertTrue(game.getGoldenFood() != null, "goldenFood no debe ser null");
        assertEquals(SnakeGame.GOLDEN_FOOD_TICKS, game.getGoldenFoodTicksLeft(), "Debe iniciar con los ticks máximos");

        // Con Random=0, food normal está en (5, 0) y goldenFood en (6, 0).
        // Avanzamos 1 paso -> come normal en (5, 0) (puntaje 6); siguiente paso -> come dorada en (6, 0) (+3 => 9)
        game.update();
        assertEquals(6, game.getScore(), "Debe sumar +1 por la manzana normal en (5, 0)");

        TickEvent goldenEat = game.update();
        assertTrue(goldenEat.ateGoldenFood(), "Debe reportar ateGoldenFood");
        assertEquals(6 + SnakeGame.GOLDEN_FOOD_POINTS, game.getScore(), "La manzana dorada debe sumar +3 puntos");
        assertTrue(game.getGoldenFood() == null, "La manzana dorada debe desaparecer tras comerla");

        // Probamos también expiración en otra partida sin comer la dorada
        SnakeGame expiryGame = startPlayingGame(10, 10, 0, constantZeroRandom());
        expiryGame.queueDirection(Direction.UP);
        for (int i = 0; i < 5; i++) {
            expiryGame.update();
        }
        expiryGame.queueDirection(Direction.RIGHT);
        for (int i = 0; i < 9; i++) {
            expiryGame.update();
        }
        assertTrue(expiryGame.getGoldenFood() != null, "Debe aparecer manzana dorada para test de expiración");

        // Bajamos a otra fila para no tocarla y dejamos pasar GOLDEN_FOOD_TICKS ticks
        expiryGame.queueDirection(Direction.DOWN);
        expiryGame.update();
        expiryGame.queueDirection(Direction.RIGHT);
        for (int i = 0; i < SnakeGame.GOLDEN_FOOD_TICKS; i++) {
            expiryGame.update();
        }
        assertTrue(expiryGame.getGoldenFood() == null, "La manzana dorada debe expirar al agotarse sus ticks");
        passed++;
    }

    private static void testSelfCollisionAndResetPreservesHighScore() {
        SnakeGame game = startPlayingGame(6, 6, 0, constantZeroRandom());

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

        // La segunda manzana aparece en (1, 0)
        game.update();
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

    private static void testDifficultySpeedOrdering() {
        assertTrue(Difficulty.EASY.initialTickMs() > Difficulty.NORMAL.initialTickMs(),
                "Fácil debe ser más lento que Normal");
        assertTrue(Difficulty.NORMAL.initialTickMs() > Difficulty.HARD.initialTickMs(),
                "Normal debe ser más lento que Difícil");
        assertEquals(Difficulty.HARD.minTickMs(), Difficulty.HARD.tickDelayForScore(500),
                "La velocidad debe respetarse con tope en minTickMs");
        passed++;
    }

    private static void testThemesCycleCleanly() {
        GameTheme first = GameTheme.CLASSIC_GREEN;
        GameTheme current = first;
        for (int i = 0; i < GameTheme.values().length; i++) {
            assertTrue(current.label() != null && !current.label().isBlank(), "Cada tema debe tener etiqueta");
            assertTrue(current.bg() != null && current.snakeHead() != null && current.food() != null,
                    "Cada tema debe definir sus colores principales");
            assertEquals(current, current.next().previous(), "next() y previous() deben ser inversos");
            current = current.next();
        }
        assertEquals(first, current, "Recorrer todos los temas debe volver al inicial");
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

    private static SnakeGame startPlayingGame(int cols, int rows, int initialHighScore, Random random) {
        SnakeGame game = new SnakeGame(cols, rows, initialHighScore, random);
        game.startGame();
        return game;
    }

    private static Random constantZeroRandom() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
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
