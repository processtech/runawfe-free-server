package ru.runa.wfe.user;

public interface ExecutorLoader {

    /**
     * Load {@linkplain Executor} by identity. Throws exception if load is impossible.
     */
    Executor getExecutor(Long id);

    /**
     * Load {@linkplain Actor} by code. Throws exception if load is impossible.
     */
    Actor getActorByCode(Long code);

    /**
     * Load {@linkplain Executor} by name. Throws exception if load is impossible.
     */
    Executor getExecutor(String name);
}
