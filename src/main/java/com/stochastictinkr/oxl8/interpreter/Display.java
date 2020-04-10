package com.stochastictinkr.oxl8.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Display {
    private List<DisplayListener> listeners = new ArrayList<>();
    private int width;
    private int height;
    private boolean[][] values;

    public Display(int width, int height) {
        this.width = width;
        this.height = height;
        values = new boolean[this.height][this.width];
    }

    public synchronized void clear() {
        for (boolean[] row: values) {
            Arrays.fill(row, false);
        }
    }

    public synchronized boolean isPixelSet(int x, int y) {
        return values[y][x];
    }

    public synchronized int getWidth() {
        return width;
    }

    public synchronized int getHeight() {
        return height;
    }

    public synchronized boolean draw(int x, int y, byte[] memory, int offset, int length) {
        boolean collision = false;
        int endRow = (y + length) % height;
        int endCol = (x + 8) % width;
        while (y != endRow) {
            int value = memory[offset++];
            int x2 = endCol;
            while (x2 != x) {
                --x2;
                if (x2 == 0) {
                    x2 = width-1;
                }
                if ((value & 1) == 1) {
                    collision = collision || values[y][x2];
                    values[y][x2] = !values[y][x2];
                }
            }

            ++y;
            if (y == height) {
                y = 0;
            }
        }
        listeners.forEach(DisplayListener::displayUpdated);
        return collision;
    }

    public synchronized void removeListener(DisplayListener displayListener) {
        this.listeners.remove(displayListener);
    }

    public synchronized void addListener(DisplayListener displayListener) {
        this.listeners.add(displayListener);
    }
}
