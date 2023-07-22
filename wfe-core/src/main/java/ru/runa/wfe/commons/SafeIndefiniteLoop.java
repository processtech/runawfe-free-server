package ru.runa.wfe.commons;

import ru.runa.wfe.InternalApplicationException;

public abstract class SafeIndefiniteLoop {
    private final int max;

    public SafeIndefiniteLoop(int max) {
        this.max = max;
    }

    protected abstract boolean continueLoop();

    protected abstract void doOp();

    public final void doLoop() {
        int counter = 0;
        while (continueLoop() && counter < max) {
            doOp();
            counter++;
        }
        if (counter == max) {
            throw new InternalApplicationException("Indefinite loop detected [max continuations = " + max + "]");
        }
    }
}
