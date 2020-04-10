package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.Display;
import com.stochastictinkr.oxl8.interpreter.DisplayListener;

import javax.swing.*;
import java.awt.*;

public class DisplayComponent extends JComponent {
    private final DisplayListener displayListener = this::repaint;
    private Display display;

    public void setDisplay(Display display) {
        if (this.display != null) {
            display.removeListener(displayListener);
        }
        this.display = display;
        this.display.addListener(displayListener);

    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.GRAY);
        final int maxWidth = getWidth() / display.getWidth();
        final int maxHeight = getHeight() / display.getHeight();
        int size = Math.min(maxWidth, maxHeight);
        for (int y = 0; y < display.getHeight(); ++y) {
            for (int x = 0; x < display.getWidth(); ++x) {
                g.fill3DRect(x * size, y * size, size, size, display.isPixelSet(x, y));
            }
        }
    }
}
