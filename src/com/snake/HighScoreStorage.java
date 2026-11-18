package com.snake;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda y carga el puntaje máximo en un archivo local del usuario
 * para que persista entre partidas sin ensuciar el repositorio.
 */
public class HighScoreStorage {
    private final Path filePath;

    public HighScoreStorage() {
        this(Path.of(System.getProperty("user.home"), ".snake-java-highscore.txt"));
    }

    public HighScoreStorage(Path filePath) {
        this.filePath = filePath;
    }

    public int load() {
        if (!Files.exists(filePath)) {
            return 0;
        }
        try {
            String content = Files.readString(filePath).trim();
            return Math.max(0, Integer.parseInt(content));
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    public void save(int highScore) {
        try {
            Files.writeString(filePath, Integer.toString(Math.max(0, highScore)));
        } catch (IOException ignored) {
            // Si no se puede escribir en disco, el juego continúa normalmente
        }
    }
}
