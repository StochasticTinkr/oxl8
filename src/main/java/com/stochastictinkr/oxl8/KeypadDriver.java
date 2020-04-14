package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.Keypad;
import com.stochastictinkr.oxl8.settings.Keymap;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeypadDriver extends KeyAdapter {
    private final Keypad keypad;
    private final Keymap keymap;

    public KeypadDriver(Keypad keypad, Keymap keymap) {
        this.keypad = keypad;
        this.keymap = keymap;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        processKeyEvent(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        processKeyEvent(e.getKeyCode(), false);
    }

    private void processKeyEvent(int keyCode, boolean pressed) {
        int key = keymap.keyForCode(keyCode);
        if (key >= 0) {
            keypad.setKeyPressed(key, pressed);
        }
    }
}
