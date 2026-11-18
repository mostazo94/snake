# Snake en Java (Swing)

Implementación moderna y limpia del clásico **Snake** escrita en **Java puro (JDK 21)** utilizando **Java Swing** y **Java Sound**, sin dependencias externas.

## Características

- **Pantalla de inicio interactiva**: elige con teclado o mouse entre **2 modos** (*Infinito* con bordes envolventes o *Clásico* con paredes sólidas) y **3 dificultades** (*Fácil*, *Normal*, *Difícil*).
- **Manzana dorada temporal**: cada 5 manzanas normales aparece una manzana dorada con barra de tiempo que otorga **+3 puntos extra** si la atrapas a tiempo.
- **Efectos de sonido retro sintetizados**: audio generado en tiempo real con `javax.sound.sampled` (al comer, al aparecer/atrapar manzana dorada, clics de menú, *Game Over* y *Victoria*), con opción de silenciar (`M`).
- **Animaciones y partículas fluidas (60 FPS)**: explosiones de partículas al comer, textos flotantes (`+1` / `+3`) y pulso animado en las manzanas.
- **Movimiento responsivo**: cola de hasta 2 direcciones en buffer para giros rápidos sin posibilidad de girar 180° accidentalmente.
- **HUD y récord persistente**: puntaje actual, modo/dificultad activos, estado de sonido y récord guardado automáticamente en `~/.snake-java-highscore.txt`.
- **Arquitectura desacoplada y testeada**: la lógica del juego (`SnakeGame`) está separada de la interfaz gráfica (`GamePanel`) y verificada con pruebas unitarias.

## Controles

| Acción | Teclas / Mouse |
| --- | --- |
| Navegar menú | `↑` `↓` `←` `→`, `W` `A` `S` `D` o clic |
| Iniciar partida | `Enter`, `Espacio` o botón **JUGAR** |
| Mover la serpiente | `↑` `↓` `←` `→` o `W` `A` `S` `D` |
| Pausar / Reanudar | `Espacio` o `P` |
| Silenciar / Activar sonido | `M` |
| Reiniciar (en Game Over) | `Enter`, `Espacio` o `R` |
| Volver al menú | `Esc` |

## Estructura del proyecto

```text
snake-java/
├── src/com/snake/
│   ├── Main.java              # Punto de entrada y creación del JFrame
│   ├── Direction.java         # Enum de direcciones con validación de opuestos
│   ├── GridPoint.java         # Record inmutable para coordenadas (x, y)
│   ├── GameMode.java          # Modos de juego (Infinito vs. Clásico)
│   ├── Difficulty.java        # Dificultades y curva de velocidad
│   ├── TickEvent.java         # Record de eventos por tick para desacoplar lógica y UI
│   ├── SnakeGame.java         # Lógica pura (serpiente, comida, manzana dorada, colisiones)
│   ├── HighScoreStorage.java  # Persistencia local del puntaje máximo
│   ├── SoundEffects.java      # Sintetizador de efectos retro con javax.sound.sampled
│   └── GamePanel.java         # UI en Swing a 60 FPS, menú, HUD, partículas y controles
├── test/com/snake/
│   └── SnakeGameTest.java     # 11 pruebas unitarias de la lógica y persistencia
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
