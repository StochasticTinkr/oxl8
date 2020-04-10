package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.CPU;
import com.stochastictinkr.oxl8.interpreter.Display;
import com.stochastictinkr.oxl8.interpreter.Keyboard;
import com.stochastictinkr.oxl8.interpreter.Timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.stream.IntStream;

import static java.awt.event.KeyEvent.*;

public class App {
    private final byte[] font;
    private final byte[] rom;
    private final int[][] keymap = {
            /* 0 */ {VK_X},
            /* 1 */ {VK_1},
            /* 2 */ {VK_2},
            /* 3 */ {VK_3},
            /* 4 */ {VK_Q},
            /* 5 */ {VK_W},
            /* 6 */ {VK_E},
            /* 7 */ {VK_A},
            /* 8 */ {VK_S},
            /* 9 */ {VK_D},
            /* A */ {VK_Z},
            /* B */ {VK_C},
            /* C */ {VK_4},
            /* D */ {VK_R},
            /* E */ {VK_F},
            /* F */ {VK_V},
    };
    private final Timer delay = new Timer();
    private final Timer sound = new Timer();
    private final Display display = new Display(64, 32);
    private final Keyboard keyboard = new Keyboard();
    private final CPU cpu = new CPU(delay, sound, display, keyboard);
    private final DisplayComponent displayComponent = new DisplayComponent();
    private final KeyboardController keyboardController = new KeyboardController();
    private final JFrame frame;

    private Action runCpu = new AbstractAction("Run") {
        @Override
        public void actionPerformed(ActionEvent e) {
            startCpuThread();
        }
    };

    private Action stopCpu = new AbstractAction("Stop") {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopCpuThread();
        }
    };
    private Action stepCpu = new AbstractAction("Step") {
        @Override
        public void actionPerformed(ActionEvent e) {
            stepCpu();
        }
    };
    private CPUThread cpuThread;

    public App(byte[] font, byte[] rom, boolean debug, String file) {
        this.font = font;
        this.rom = rom;
        this.cpu.setDebug(debug);
        frame = new JFrame("Oxl8 CHIP-8 emulator -- " + file);
        frame.getContentPane().setBackground(new Color(64, 70, 24));
        displayComponent.setPreferredSize(new Dimension(128 * 8, 64 * 8));
        JComponent keyboardComponent = keyboardController.getComponent();
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createGlue());
        panel.add(keyboardComponent);
        panel.add(Box.createGlue());
        keyboardComponent.setMaximumSize(new Dimension(15 * 16, 15 * 16));
        keyboardComponent.setPreferredSize(new Dimension(15 * 16, 15 * 16));
        frame.add(displayComponent, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.EAST);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                processKeyEvent(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                processKeyEvent(e.getKeyCode(), false);
            }

            private void processKeyEvent(int keyCode, boolean pressed) {
                int key = keyForCode(keyCode);
                if (key >= 0) {
                    keyboard.setKeyPressed(key, pressed);
                }
            }

            private int keyForCode(int keyCode) {
                int key;
                for (key = 15; key >= 0; --key) {
                    if (IntStream.of(keymap[key]).anyMatch(v -> v == keyCode)) {
                        break;
                    }
                }
                return key;
            }
        });

        JPanel buttons = new JPanel();
        frame.add(buttons, BorderLayout.NORTH);
        buttons.add(new JButton(runCpu));
        buttons.add(new JButton(stopCpu));
        buttons.add(new JButton(stepCpu));

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        keyboardController.setKeyboard(keyboard);
        displayComponent.setDisplay(display);
    }



    public void start() {
        frame.pack();
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        cpu.setFont(font);
        cpu.setRom(rom);
    }

    private void stepCpu() {
        if (isCpuThreadRunning()) {
            return;
        }
        cpu.step();
    }

    private void stopCpuThread() {
        if (isCpuThreadRunning()) {
            cpuThread.halt();
        }
    }

    private void startCpuThread() {
        if (isCpuThreadRunning()) {
            return;
        }
        cpuThread = new CPUThread(cpu);
        new Thread(cpuThread).start();
    }

    private boolean isCpuThreadRunning() {
        return cpuThread != null && cpuThread.isRunning();
    }

}
