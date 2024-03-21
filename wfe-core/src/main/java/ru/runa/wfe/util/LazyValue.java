package ru.runa.wfe.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyValue<T> {
    @NonNull
    private final Supplier<T> supplier;
    private final AtomicReference<T> value = new AtomicReference<>();

    /**
     * On first call, calls supplier (which is expected to return non-null value) and caches result. On next calls, returns cached value. Thread-safe.
     * Supplier must be thread-safe.
     *
     * TODO Is there any standard class for this?
     */
    public T get() {
        T v = value.get();
        if (v == null) {
            v = supplier.get();
            if (v == null) {
                throw new IllegalArgumentException("Supplier returned null");
            }
            value.set(v);
        }
        return v;
    }
}
