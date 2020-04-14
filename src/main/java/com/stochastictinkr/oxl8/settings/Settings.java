package com.stochastictinkr.oxl8.settings;

import lombok.Data;

@Data
public class Settings {
    private int timerFrequency = 60;
    private int clockMultiplier = 100;
    private final Keymap keymap = new Keymap();
    private final DisplaySize displaySize = new DisplaySize(64, 32);
    private final FileSelection romFile = new FileSelection();
    private final FileSelection fontFile = new FileSelection();

    private boolean soundEnabled = false;
    private boolean debugEnabled = false;
    private boolean showKeypad = true;
}
