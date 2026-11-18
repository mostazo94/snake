# Snake en Java (Swing)

Implementación clásica y limpia del juego **Snake** escrita en **Java puro (JDK 21)** utilizando **Java Swing**, sin dependencias externas.

## Características

- **Bordes infinitos**: al cruzar cualquier borde del tablero, la serpiente reaparece por el lado opuesto.
- **Movimiento fluido**: cola de hasta 2 direcciones en buffer para que giros rápidos se sientan responsivos e impidan giros accidentales de 180°.
- **Aceleración progresiva**: la velocidad aumenta suavemente conforme sumas puntos (de `125 ms` hasta un mínimo de `65 ms`).
- **HUD y récord persistente**: barra superior con puntaje actual y récord histórico guardado automáticamente en `~/.snake-java-highscore.txt`.
- **Pausa y reinicio rápido**: pantallas superpuestas para pausa, *Game Over* y *Victoria*.
- **Arquitectura separada y testeada**: la lógica del juego (`SnakeGame`) está desacoplada de la interfaz gráfica (`GamePanel`) y cubierta con pruebas unitarias.

## Controles

| Acción | Teclas |
| --- | --- |
| Moverse | `↑` `↓` `←` `→` o `W` `A` `S` `D` |
| Pausar / Reanudar | `Espacio` o `P` |
| Reiniciar (en Game Over) | `Enter`, `Espacio` o `R` |

## Estructura del proyecto

```text
snake-java/
├── src/com/snake/
│   ├── Main.java              # Punto de entrada y creación del JFrame
│   ├── Direction.java         # Enum de direcciones con validación de opuestos
│   ├── GridPoint.java         # Record inmutable para coordenadas (x, y)
│   ├── SnakeGame.java         # Lógica pura del juego (serpiente, comida, colisiones)
│   ├── HighScoreStorage.java  # Persistencia local del puntaje máximo
│   └── GamePanel.java         # Renderizado con Swing, Timer, HUD y teclado
├── test/com/snake/
│   └── SnakeGameTest.java     # Pruebas unitarias de la lógica y persistencia
├── .gitignore
└── README.md
```

## Requisitos

- **JDK 21** o superior (compatible con cualquier JDK 17+).

## Cómo compilar y ejecutar

### Windows (PowerShell)

```powershell
javac -encoding UTF-8 -d out src/com/snake/*.java
java -cp out com.snake.Main
```

### Linux / macOS

```bash
javac -encoding UTF-8 -d out src/com/snake/*.java
java -cp out com.snake.Main
```

## Cómo ejecutar las pruebas

```powershell
javac -encoding UTF-8 -d out src/com/snake/*.java test/com/snake/SnakeGameTest.java
java -cp out com.snake.SnakeGameTest
```
