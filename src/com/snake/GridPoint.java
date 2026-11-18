package com.snake;

/**
 * Representa una celda (x, y) dentro de la cuadrícula del tablero.
 * Usamos un Java record para que sea inmutable y tenga equals/hashCode automáticos.
 */
public record GridPoint(int x, int y) {
    public GridPoint translate(Direction direction) {
        return new GridPoint(x + direction.dx(), y + direction.dy());
    }
}
