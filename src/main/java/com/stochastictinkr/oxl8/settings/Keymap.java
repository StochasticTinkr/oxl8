package com.stochastictinkr.oxl8.settings;

import java.util.stream.IntStream;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_V;

public class Keymap {
    private static int[][] defaultKeymap() {
        return new int[][]{
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
    }

    private int[][] map;

    public Keymap() {
        this(defaultKeymap());
    }

    public Keymap(int[][] map) {
        this.map = map;
    }

    public int keyForCode(int keyCode) {
        int key;
        for (key = 15; key >= 0; --key) {
            if (IntStream.of(map[key]).anyMatch(v -> v == keyCode)) {
                break;
            }
        }
        return key;
    }
}
