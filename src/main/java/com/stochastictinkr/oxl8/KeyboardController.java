package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.Keyboard;
import com.stochastictinkr.oxl8.interpreter.KeyboardListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.util.stream.IntStream;

public class KeyboardController {
    private static final int[] keyValues = {
            0x1, 0x2, 0x3, 0xC,
            0x4, 0x5, 0x6, 0xD,
            0x7, 0x8, 0x9, 0xE,
            0xA, 0x0, 0xB, 0xF
    };
    private final JPanel panel = new JPanel(new GridLayout(4, 4));
    private Color pressed = Color.DARK_GRAY.brighter();
    private Color released = Color.LIGHT_GRAY.darker();
    private final KeyboardListener keyboardListener = panel::repaint;
    private Keyboard keyboard;


    public KeyboardController() {
        IntStream.of(keyValues)
                .mapToObj(KeyComponent::new)
                .peek(
                        k -> k.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                keyboard.setKeyPressed(k.value, true);
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                keyboard.setKeyPressed(k.value, false);
                            }
                        })
                )
                .forEachOrdered(panel::add);
        panel.setOpaque(false);
    }

    public JComponent getComponent() {
        return panel;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (this.keyboard != null) {
            this.keyboard.removeListener(keyboardListener);
        }
        this.keyboard = keyboard;
        if (this.keyboard != null) {
            this.keyboard.addListener(keyboardListener);
        }
    }

    class KeyComponent extends JComponent {
        private final int value;

        public KeyComponent(int value) {
            this.value = value;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            boolean isPressed = keyboard.isKeyPressed(value);
            Color faceColor = isPressed ? pressed : released;
            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, getWidth() / 5f, getHeight() / 5f);
            g2d.setPaint(isPressed ? faceColor.darker() : faceColor.brighter());
            g2d.fill(shape);
            shape.x = 4;
            shape.y = 4;
            g2d.setPaint(isPressed ? faceColor.brighter() : faceColor.darker());
            g2d.fill(shape);
            g2d.setPaint(faceColor);
            shape.x = 2;
            shape.y = 2;
            g2d.fill(shape);
            g2d.setFont(getFont().deriveFont(16f));
            String label = Integer.toHexString(value).toUpperCase();
            LineMetrics lineMetrics = g2d.getFontMetrics().getLineMetrics(label, g);
            float x = (getWidth() - g2d.getFontMetrics().stringWidth(label)) * .5f;
            float y = (getHeight() + lineMetrics.getAscent()) * .5f;
            g2d.setPaint(faceColor.darker());
            g2d.drawString(label, x - 1, y - 1);
            g2d.setColor(faceColor.brighter());
            g2d.drawString(label, x + 1, y + 1);
            g2d.setColor(Color.BLUE);
            g2d.drawString(label, x, y);
        }
    }
}
