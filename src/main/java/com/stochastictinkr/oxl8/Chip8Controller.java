package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.CPU;
import com.stochastictinkr.oxl8.interpreter.Display;
import com.stochastictinkr.oxl8.interpreter.Keypad;
import com.stochastictinkr.oxl8.interpreter.Timer;
import com.stochastictinkr.oxl8.settings.DisplaySize;
import com.stochastictinkr.oxl8.settings.FileSelection;
import com.stochastictinkr.oxl8.settings.Settings;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Chip8Controller {
    private final JFrame frame = new JFrame();
    private final FileLoader fileLoader = new FileLoader();
    private final DisplayComponent displayComponent = new DisplayComponent();
    private final KeypadController keypadController = new KeypadController();
    private final Settings settings;
    private final Object stateLock = new Object();

    private final Timer delay;
    private final Timer sound;
    private final Keypad keypad;
    private final CPU cpu;
    private final Display display;
    private byte[] loadedRom;
    private String romName = "";

    private final EmulationWorker worker = new EmulationWorker();
    private WorkerState workerState = WorkerState.STOPPED;
    private final JPanel keypadPanel = new JPanel();

    public Chip8Controller(Settings settings) throws IOException {
        this.settings = settings;
        delay = new Timer();
        sound = new Timer();
        keypad = new Keypad();
        display = createDisplay(settings.getDisplaySize());
        cpu = new CPU(delay, sound, display, keypad);
        displayComponent.setDisplay(display);
        fileLoader.load(settings.getFontFile(), Chip8Font::new).install(cpu);
        loadRom(settings.getRomFile());
        frame.add(displayComponent, BorderLayout.CENTER);
        configureKeypadPanel();
        frame.add(keypadPanel, BorderLayout.EAST);
        frame.addKeyListener(new KeypadDriver(keypad, settings.getKeymap()));
        keypadPanel.setVisible(settings.isShowKeypad());

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void configureKeypadPanel() {
        keypadController.setKeypad(keypad);
        keypadPanel.setOpaque(false);
        keypadPanel.setLayout(new BoxLayout(keypadPanel, BoxLayout.PAGE_AXIS));
        keypadPanel.add(Box.createGlue());
        JComponent keypadComponent = keypadController.getComponent();
        keypadComponent.setMaximumSize(new Dimension(15 * 16, 15 * 16));
        keypadComponent.setPreferredSize(new Dimension(15 * 16, 15 * 16));
        keypadPanel.add(keypadComponent);
        keypadPanel.add(Box.createGlue());
    }

    private synchronized void loadRom(FileSelection romFile) throws IOException {
        if (romFile.isDefault()) {
            loadedRom = new byte[0];
            romName = null;
        } else {
            loadedRom = fileLoader.load(romFile);
            romName = romFile.getLocation();
        }
        display.clear();
        displayComponent.repaint();
        updateTitle();
    }

    private void updateTitle() {
        frame.setTitle(String.format("Oxl8 - %s - %s", getWorkerState(), romName != null ? romName : "No ROM loaded"));
    }

    public synchronized void start() throws InterruptedException {
        resetWorker();
        cpu.setRom(loadedRom);
        final Thread thread = new Thread(worker);
        thread.setDaemon(true);
        thread.start();
        waitForStateChange(WorkerState.WAITING);
    }

    public synchronized void stop() throws InterruptedException {
        resetWorker();
    }

    private void resetWorker() throws InterruptedException {
        while (true) {
            synchronized (stateLock) {
                switch (workerState) {
                    case WAITING: return;
                    case RUNNING:
                        setWorkerState(WorkerState.STOPPING);
                    case STOPPING:
                        waitForStateChange(workerState);
                        break;
                    case STOPPED:
                        setWorkerState(WorkerState.WAITING);
                }
            }
        }
    }

    private Display createDisplay(DisplaySize displaySize) {
        return new Display(displaySize.getWidth(), displaySize.getHeight());
    }

    private enum WorkerState {
        WAITING,
        RUNNING,
        STOPPING,
        STOPPED;
    }

    private void setWorkerState(WorkerState state) {
        synchronized (stateLock) {
            workerState = state;
            updateTitle();
            stateLock.notifyAll();
        }
    }


    private WorkerState compareAndSwapState(WorkerState expectedState, WorkerState state) {
        synchronized (stateLock) {
            if (expectedState != workerState) {
                return workerState;
            }
            setWorkerState(state);
            return workerState;
        }
    }
    private WorkerState getWorkerState() {
        synchronized (stateLock) {
            return workerState;
        }
    }

    private WorkerState waitForStateChange(WorkerState oldState) throws InterruptedException {
        synchronized (stateLock) {
            while (workerState == oldState) {
                stateLock.wait();
            }
            return workerState;
        }
    }

    private class EmulationWorker implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            final WorkerState workerState = compareAndSwapState(WorkerState.WAITING, WorkerState.RUNNING);
            if (workerState != WorkerState.RUNNING) {
                return;
            }
            try {
                while (getWorkerState() == WorkerState.RUNNING) {
                    final int clockMultiplier = settings.getClockMultiplier();
                    for (int steps = 0; steps < clockMultiplier; ++steps) {
                        cpu.step();
                    }
                    delay.tick();
                    sound.tick();
                    final int timerFrequency = settings.getTimerFrequency();
                    final int frameDelayNS = 1000000000 / timerFrequency;
                    Thread.sleep(frameDelayNS / 1000000, frameDelayNS % 1000000);
                }
            } finally {
                setWorkerState(WorkerState.STOPPED);
            }
        }
    }
}
