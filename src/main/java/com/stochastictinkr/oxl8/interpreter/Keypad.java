package com.stochastictinkr.oxl8.interpreter;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class Keypad {
    private final List<KeypadListener> listeners = new ArrayList<>();
    private final Queue<IntConsumer> oneTimeListeners = new LinkedList<>();
    private boolean[] pressedKeys = new boolean[16];

    public synchronized boolean isKeyPressed(int key) {
        return pressedKeys[key];
    }

    public synchronized void setKeyPressed(int key, boolean pressed) {
        boolean oldValue = pressedKeys[key];
        pressedKeys[key] = pressed;
        if (pressed) {
            final IntConsumer listener = oneTimeListeners.poll();
            if (listener != null) {
                listener.accept(key);
            }
        }
        if (oldValue != pressed) {
            listeners.forEach(KeypadListener::keypadChangedStates);
        }
    }

    public synchronized void waitForPress(IntConsumer listener) {
        IntStream.range(0, 16)
                .filter(this::isKeyPressed)
                .findFirst()
                .ifPresentOrElse(listener, () -> oneTimeListeners.add(listener));
    }

    public synchronized void removeListener(KeypadListener keypadListener) {
        listeners.remove(keypadListener);
    }

    public synchronized void addListener(KeypadListener keypadListener) {
        listeners.add(keypadListener);
    }

    public int firstPressed() {
        return IntStream.range(0, 16)
                .filter(this::isKeyPressed)
                .findFirst().orElse(-1);
    }
}
