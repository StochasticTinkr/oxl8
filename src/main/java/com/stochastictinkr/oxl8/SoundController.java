package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.Timer;
import lombok.SneakyThrows;

import javax.sound.sampled.*;
import java.awt.*;

public class SoundController {
    private Timer sound;
    private final Beeper beeper = new Beeper();
    private boolean on;

    public SoundController() {
    }

    public void start() throws LineUnavailableException {
        beeper.open();
    }

    public Timer getSound() {
        return sound;
    }

    public void setSound(Timer sound) {
        this.sound = sound;
    }

    public void stopSound() {
        beeper.quite();
        on = false;
    }

    private void startSound() {
        beeper.beep();
        on = true;
    }

    public void updateSound() {
        if (on && sound.getCount() == 0) {
            stopSound();
            return;
        }
        if (!on && sound.getCount() != 0) {
            startSound();
        }
    }
}
