package com.stochastictinkr.oxl8;

import lombok.ToString;

import javax.sound.sampled.*;
import java.util.Random;

public class Beeper {
    public static final int SAMPLE_RATE = 44100;
    public static final int BUFFER_SIZE = SAMPLE_RATE / 10;

    private static final double[] SCALE = {
            261.63,
            293.66,
            329.63,
            349.23,
            392.00,
            440.00,
            493.88,
            523.25,
    };

    private final Random random = new Random();
    private final Object stateLock = new Object();
    private final State current = new State();
    private final State target = new State();

    private SourceDataLine sourceDataLine;

    public Beeper() {
    }

    public void open() throws LineUnavailableException {
        final AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        sourceDataLine = AudioSystem.getSourceDataLine(format);
        sourceDataLine.open(format, BUFFER_SIZE*2);
        sourceDataLine.start();
        final Thread thread = new Thread(this::run);
        thread.setDaemon(true);
        thread.start();
    }

    public void beep() {
        synchronized (stateLock) {
            final double frequency = target.frequency;
            while (frequency == target.frequency) {
                target.frequency = SCALE[random.nextInt(SCALE.length)];
            }
            target.volume = .5;
        }
    }

    public void quite() {
        synchronized (stateLock) {
            target.volume = 0;
        }
    }

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        final Beeper beeper = new Beeper();
        beeper.open();
        for (int i =0; i < 100; ++i) {
            beeper.beep();
            Thread.sleep(150);
            if (i % 5 == 0) {
                beeper.quite();
                Thread.sleep(1000);
            }
        }
    }

    private void run() {
        double theta = 0;
        final double delta = Math.PI / BUFFER_SIZE;
        while (true) {
            final double volumeRamp;
            synchronized (stateLock) {
                if (target.volume == 0) {
                    volumeRamp = -0.02/BUFFER_SIZE;
                } else {
                    if (current.frequency != target.frequency) {
                        if (current.volume > 0) {
                            volumeRamp = -0.10/BUFFER_SIZE;
                        } else {
                            current.frequency = target.frequency;
                            volumeRamp = 0.10/BUFFER_SIZE;
                        }
                    } else {
                        volumeRamp = 0;
                    }
                }
//                volumeRamp = (target.volume - current.volume) / (double)BUFFER_SIZE;
//                frequencyRamp = (target.frequency - current.frequency) / (double)BUFFER_SIZE;
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; ++i) {
                double s = Math.sin(theta * current.frequency) * current.volume
                        + Math.sin(theta * current.frequency*1.16) * current.volume / 4;
                buffer[i] = (byte)Math.min(Math.max(-127, (s*128)), 128);
                current.volume = Math.min(Math.max(0, current.volume +volumeRamp), 1);
                theta += delta;
            }
            sourceDataLine.write(buffer, 0, buffer.length);
            while (theta > Math.PI * 2) {
                theta -= Math.PI * 2;
            }
        }
    }

    @ToString
    private static class State {
        private double frequency;
        private double volume;
    }

}
