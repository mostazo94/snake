package com.snake;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sintetizador ligero de efectos retro usando únicamente javax.sound.sampled
 * (sin archivos .wav ni librerías externas).
 */
public class SoundEffects {
    private static final float SAMPLE_RATE = 22_050f;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "snake-audio");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean muted;

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
    }

    public void playClick() {
        playSequence(new double[][]{{640, 32, 0.14}});
    }

    public void playEat() {
        playSequence(new double[][]{
                {523.25, 45, 0.22},
                {783.99, 60, 0.22}
        });
    }

    public void playGoldenEat() {
        playSequence(new double[][]{
                {659.25, 55, 0.24},
                {880.00, 55, 0.24},
                {1174.66, 95, 0.26}
        });
    }

    public void playGoldenSpawn() {
        playSequence(new double[][]{
                {740.00, 40, 0.14},
                {987.77, 55, 0.14}
        });
    }

    public void playGameOver() {
        playSequence(new double[][]{
                {392.00, 100, 0.24},
                {311.13, 110, 0.24},
                {220.00, 190, 0.26}
        });
    }

    public void playVictory() {
        playSequence(new double[][]{
                {523.25, 80, 0.24},
                {659.25, 80, 0.24},
                {783.99, 80, 0.24},
                {1046.50, 200, 0.28}
        });
    }

    private void playSequence(double[][] notes) {
        if (muted) {
            return;
        }
        executor.submit(() -> {
            if (muted) {
                return;
            }
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                for (double[] note : notes) {
                    if (muted) {
                        break;
                    }
                    byte[] pcm = synthesizeTone(note[0], (int) note[1], note[2]);
                    line.write(pcm, 0, pcm.length);
                }
                line.drain();
            } catch (LineUnavailableException | IllegalArgumentException ignored) {
                // Si el sistema no tiene dispositivo de audio disponible, seguimos sin error
            }
        });
    }

    private byte[] synthesizeTone(double freqHz, int durationMs, double volume) {
        int samples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[samples];
        int fadeSamples = Math.max(1, Math.min(samples / 4, 160));

        for (int i = 0; i < samples; i++) {
            double time = i / SAMPLE_RATE;
            // Mezcla suave entre onda senoidal y triángulo para un timbre retro agradable
            double phase = 2.0 * Math.PI * freqHz * time;
            double sine = Math.sin(phase);
            double harmonic = 0.25 * Math.sin(2.0 * phase);
            double wave = (sine + harmonic) / 1.25;

            // Envolvente fade-in / fade-out para evitar clics
            double envelope = 1.0;
            if (i < fadeSamples) {
                envelope = (double) i / fadeSamples;
            } else if (i > samples - fadeSamples) {
                envelope = (double) (samples - i) / fadeSamples;
            }

            int value = (int) Math.round(wave * envelope * volume * 127.0);
            buffer[i] = (byte) Math.max(-127, Math.min(127, value));
        }
        return buffer;
    }
}
