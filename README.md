# Snake en Java (Swing)

Implementación moderna y limpia del clásico **Snake** escrita en **Java puro (JDK 21)** utilizando **Java Swing** y **Java Sound**, sin dependencias externas.

## Características

- **Pantalla de inicio interactiva**: elige con teclado o mouse entre **2 modos** (*Infinito* con bordes envolventes o *Clásico* con paredes sólidas), **3 dificultades** (*Fácil*, *Normal*, *Difícil*) y **4 temas visuales**.
- **Temas visuales / skins en vivo**: cambia al instante entre *Clásico Verde*, *Neón Cyberpunk*, *Retro GameBoy* y *Océano* desde el menú o en cualquier momento con la tecla `T`.
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
| Cambiar tema visual | `T` (en menú, partida o pausa) |
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
│   ├── GameTheme.java         # Paletas de temas visuales (Verde, Cyberpunk, GameBoy, Océano)
│   ├── TickEvent.java         # Record de eventos por tick para desacoplar lógica y UI
│   ├── SnakeGame.java         # Lógica pura (serpiente, comida, manzana dorada, colisiones)
│   ├── HighScoreStorage.java  # Persistencia local del puntaje máximo
│   ├── SoundEffects.java      # Sintetizador de efectos retro con javax.sound.sampled
│   └── GamePanel.java         # UI en Swing a 60 FPS, menú, HUD, temas, partículas y controles
├── test/com/snake/
│   └── SnakeGameTest.java     # 12 pruebas unitarias de la lógica, temas y persistencia
├── build-dist.ps1             # Script para generar el paquete portable (.rar) con JRE incluido
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

## Generar paquete portable para Windows (`.rar`)

Si quieres generar un archivo comprimido listo para descomprimir y jugar con doble clic (sin necesitar Java instalado en la PC destino):

```powershell
.\build-dist.ps1
```

Esto crea `dist/Snake-Java-Windows.rar` con el `.jar` compilado, un entorno mínimo de Java generado con `jlink` y el lanzador `Jugar Snake.bat`.

