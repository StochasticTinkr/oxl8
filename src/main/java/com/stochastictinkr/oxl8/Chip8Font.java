package com.stochastictinkr.oxl8;


import com.stochastictinkr.oxl8.interpreter.CPU;

public class Chip8Font {
    private byte[] font;

    public Chip8Font(byte[] font) {
        this.font = font;
    }

    public void install(CPU cpu) {
        if (font != null) {
            cpu.setFont(font);
        }
    }
}
