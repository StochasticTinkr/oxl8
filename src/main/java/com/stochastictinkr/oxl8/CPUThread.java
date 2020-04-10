package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.interpreter.CPU;
import com.stochastictinkr.oxl8.interpreter.CPUListener;
import lombok.SneakyThrows;

public class CPUThread implements Runnable {
    private final Object pause = new Object();
    private final CPUListener cpuListener = new CPUListener() {
        @Override
        public void cpuPaused() {
        }

        @Override
        public void cpuResumed() {
            synchronized (pause) {
                pause.notifyAll();
            }
        }
    };
    private volatile boolean finish;
    private final CPU cpu;

    public CPUThread(CPU cpu) {
        this.cpu = cpu;
    }

    @SneakyThrows
    @Override
    public void run() {
        cpu.addListener(cpuListener);
        try {
            while (!finish) {
                cpu.step();
                while (cpu.isPaused()) {
                    synchronized (pause) {
                        pause.wait();
                    }
                }
            }
        } finally {
            cpu.removeListener(cpuListener);
        }
    }

    public boolean isRunning() {
        return !finish;
    }

    public void halt() {
        finish = true;
    }
}
