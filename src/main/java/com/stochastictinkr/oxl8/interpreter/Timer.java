package com.stochastictinkr.oxl8.interpreter;

public class Timer {
    int count;

    public void setCount(int count) {
        this.count = count;
    }

    public void tick() {
        if (count > 0) {
            --count;
        }
    }

    public int getCount() {
        return count;
    }
}
