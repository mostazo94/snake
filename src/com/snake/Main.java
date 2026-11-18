package com.snake;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake - Java");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack(); // Ajusta la ventana exactamente al tamaño del panel

            frame.setLocationRelativeTo(null); // Centra la ventana en la pantalla
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
