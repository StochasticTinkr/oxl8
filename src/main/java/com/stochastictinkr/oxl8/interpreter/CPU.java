package com.stochastictinkr.oxl8.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPU {
    public static final int SYS_CLS = 0x0E0;
    public static final int SYS_RET = 0xEE;
    private final List<CPUListener> listeners = new ArrayList<>();
    private final byte[] memory = new byte[8 * 1024];
    private final Timer delay;
    private final Timer sound;
    private final Display display;
    private final Keyboard keyboard;
    private final Random random = new Random();
    private volatile boolean debug;

    public CPU(Timer delay, Timer sound, Display display, Keyboard keyboard) {
        this.delay = delay;
        this.sound = sound;
        this.display = display;
        this.keyboard = keyboard;
    }

    private int pc;
    private int sp;
    private int[] stack = new int[16];
    private int[] registers = new int[16];
    private int registerI;

    private boolean paused;

    public synchronized void step() {
        if (paused) {
            return;
        }
        short opcode = wordAt(pc);
        int nnn = opcode & 0xFFF;
        int x = (opcode >> 8) & 0xF;
        int y = (opcode >> 4) & 0xF;
        int subcode = opcode & 0xF;
        int immediate = opcode & 0xFF;
        int vx = registers[x];
        int vy = registers[y];

        pc += 2;
        switch ((opcode >>> 12) & 0xF) {
            case 0x0:
                sys(nnn);
                return;
            case 0x1:
                jp(nnn);
                return;
            case 0x2:
                call(nnn);
                return;
            case 0x3:
                skipIf(vx == immediate);
                debugVxImm("SE", x, vx, immediate);
                return;
            case 0x4:
                skipIf(vx != immediate);
                debugVxImm("SNE", x, vx, immediate);
                return;
            case 0x5:
                if (subcode == 0) {
                    skipIf(vx == vy);
                    debugVxVy("SE", x, vx, y, vy);
                }
                return;
            case 0x6:
                registers[x] = immediate;
                debugVxImm("LD", x, vx, immediate);
                return;
            case 0x7:
                registers[x] = uint8(vx + immediate);
                debugVxImm("ADD", x, vx, immediate);
                return;
            case 0x8:
                switch (subcode) {
                    case 0x0:
                        registers[x] = vy;
                        debugVxVy("LD", x, vx, y, vy);
                        return;
                    case 0x1:
                        registers[x] = vx | vy;
                        debugVxVy("OR", x, vx, y, vy);
                        return;
                    case 0x2:
                        registers[x] = vx & vy;
                        debugVxVy("AND", x, vx, y, vy);
                        return;
                    case 0x3:
                        registers[x] = vx ^ vy;
                        debugVxVy("XOR", x, vx, y, vy);
                        return;
                    case 0x4: {
                        int sum = vx + vy;
                        registers[0xF] = sum >> 8;
                        registers[x] = uint8(sum);
                        debugVxVy("ADD", x, vx, y, vy);
                        return;
                    }
                    case 0x5:
                        registers[0xF] = vx > vy ? 1 : 0;
                        registers[x] = uint8(vx - vy);
                        debugVxVy("SUB", x, vx, y, vy);
                        return;
                    case 0x6:
                        registers[0xF] = vx & 1;
                        registers[x] = uint8(vx >> 1);
                        debugVx("SHR", x, vx);
                        return;
                    case 0x7:
                        registers[0xF] = vy > vx ? 1 : 0;
                        registers[x] = uint8(vy - vx);
                        debugVxVy("SUBN", x, vx, y, vy);
                        return;
                    case 0xE:
                        registers[0xF] = vx >> 7;
                        registers[x] = uint8(vx << 1);
                        debugVx("SHL", x, vx);
                        return;
                }
                break;
            case 0x9:
                if (subcode == 0) {
                    skipIf(vx != vy);
                    debugVxVy("SNE", x, vx, y, vy);
                    return;
                }
                break;
            case 0xA:
                registerI = nnn;
                debug("LD I, %03X", nnn);
                return;
            case 0xB:
                pc = registers[0] + nnn;
                debug("JP V0 (%02X), %03X", registers[0], nnn);
                return;
            case 0xC:
                registers[x] = random.nextInt(256) & immediate;
                debugVxImm("RND", x, vx, immediate);
                return;
            case 0xD:
                boolean collision = display.draw(vx, vy, memory, registerI, subcode);
                registers[0xF] = collision ? 1 : 0;
                debugVxVyN("DRW", x, vx, y, vy, subcode);
                return;
            case 0xE:
                switch (immediate) {
                    case 0x9E:
                        skipIf(keyboard.isKeyPressed(vx));
                        debugVx("SKP", x, vx);
                        return;
                    case 0xA1:
                        skipIf(!keyboard.isKeyPressed(vx));
                        debugVx("SKNP", x, vx);
                        return;
                }
                break;
            case 0xF:
                switch (immediate) {
                    case 0x07:
                        registers[x] = delay.getCount();
                        debugVx("LD", x, vx, "DT", delay.getCount());
                        return;
                    case 0x0A:
                        paused = true;
                        listeners.forEach(CPUListener::cpuPaused);
                        debugVx("LD", x, vx, "K", keyboard.firstPressed());
                        keyboard.waitForPress(key -> {
                            registers[x] = key;
                            listeners.forEach(CPUListener::cpuResumed);
                            if (debug) {
                                System.out.println("Key pressed: " + key);
                            }
                            paused = false;
                        });
                        if (debug && paused) {
                            System.out.println("CPU waiting on key press...");
                        }
                        return;
                    case 0x15:
                        delay.setCount(vx);
                        debugVx("LD", "DT", x, vx);
                        return;
                    case 0x18:
                        debugVx("LD", "ST", x, vx);
                        sound.setCount(vx);
                        return;
                    case 0x1E:
                        registerI = uint16(registerI + vx);
                        debugVx("ADD", "I", registerI, x, vx);
                        return;
                    case 0x29:
                        registerI = (vx & 0xF) * 5;
                        debugVx("LDF", x, vx);
                        return;
                    case 0x33:
                        memory[registerI] = (byte) (vx / 100);
                        memory[registerI + 1] = (byte) ((vx / 10) % 10);
                        memory[registerI + 2] = (byte) (vx % 10);
                        debugVx("BCD", "[I]", registerI, x, vx);
                        return;
                    case 0x55:
                        for (int r = 0; r <= x; ++r) {
                            memory[registerI + r] = (byte) registers[r];
                        }
                        debug("%4s %s, V0...V%X", "LD", "[I]", x);
                        return;
                    case 0x65:
                        for (int r = 0; r <= x; ++r) {
                            registers[r] = uint8(memory[registerI + r]);
                        }
                        debug("%4s V0...V%X, %s", "LD", x, "[I]");
                        return;
                }
                return;
        }
        debug("Unknown Opcode: %04X", opcode);
    }

    private void debugVx(String instruction, int x, int vx) {
        debug("%4s V%X (%02X)", instruction, x, vx);
    }

    private void debugVx(String instruction, String operand, int x, int vx) {
        debug("%4s %s, V%X (%02X)", instruction, operand, x, vx);
    }

    private void debugVx(String instruction, String operand, int operandValue, int x, int vx) {
        debug("%4s %s (%02X), V%X (%02X)", instruction, operand, operandValue, x, vx);
    }

    private void debugVx(String instruction, int x, int vx, String operand, int operandValue) {
        debug("%4s V%X (%02X), %s (%02x)", instruction, x, vx, operand, operandValue);
    }

    private void debugVxImm(String instruction, int x, int vx, int immediate) {
        debug("%4s V%X (%02X), %02X", instruction, x, vx, immediate);

    }

    private void debugVxVy(String instruction, int x, int vx, int y, int vy) {
        debug("%4s V%X (%02X), V%X (%02X)", instruction, x, vx, y, vy);
    }

    private void debugVxVyN(String instruction, int x, int vx, int y, int vy, int n) {
        debug("%4s V%X (%02X), V%X (%02X), %X", instruction, x, vx, y, vy, n);
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    private short wordAt(int index) {
        return (short) ((memory[index] << 8) | (memory[index + 1] & 0xFF));
    }


    private int uint8(int value) {
        return value & 0xFF;
    }

    private int uint16(int value) {
        return value & 0xFFFF;
    }

    private void skipIf(boolean condition) {
        if (condition) {
            pc += 2;
        }
    }

    private void sys(int nnn) {
        switch (nnn) {
            case SYS_CLS:
                display.clear();
                debug("SYS(CLS)");
                return;
            case SYS_RET:
                pc = stack[--sp];
                debug("SYS(RET)");
                return;
        }
        debug("SYS(%03X)", nnn);
    }

    private void debug(String format, Object... args) {
        if (debug) {
            System.out.printf(format, args);
            System.out.println();
            printDebug();
        }

    }

    private void jp(int nnn) {
        pc = nnn;
        debug("jp %03X", nnn);
    }

    private void call(int nnn) {
        stack[sp++] = pc;
        pc = nnn;
        debug("call %03X", nnn);
    }

    public synchronized void setFont(byte[] font) {
        System.arraycopy(font, 0, memory, 0, font.length);
    }

    public void setRom(byte[] rom) {
        pc = 512;
        System.arraycopy(rom, 0, memory, 512, rom.length);
    }

    public synchronized void addListener(CPUListener cpuListener) {
        listeners.add(cpuListener);
    }

    public synchronized void removeListener(CPUListener cpuListener) {
        listeners.remove(cpuListener);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void printDebug() {
        System.out.printf("PC=%02x, I=%02x, SP=%02x", pc, registerI, sp);
        for (int i = 0; i < 16; ++i) {
            System.out.printf(", V%X=%02x", i, registers[i]);
        }
        System.out.printf("-- PC:[%03X]=%02X %02X\n", pc, memory[pc], memory[pc + 1]);
    }
}
