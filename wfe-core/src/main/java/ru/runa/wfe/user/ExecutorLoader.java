package ru.runa.wfe.user;

public interface ExecutorLoader {

    /**
     * Load {@linkplain Executor} by identity. Throws exception if load is
     * impossible.
     * 
     * @param name
     *            Loaded executor identity.
     * @return {@linkplain Executor} with specified identity.
     */
    public Executor getExecutor(Long id);

    /**
     * Load {@linkplain Actor} by code. Throws exception if load is impossible.
     * 
     * @param name
     *            Loaded actor code.
     * @return {@linkplain Actor} with specified code.
     */
    public Actor getActorByCode(Long code);

    /**
     * Load {@linkplain Executor} by name. Throws exception if load is
     * impossible.
     * 
     * @param name
     *            Loaded executor name.
     * @return Executor with specified name.
     */
    public Executor getExecutor(String name);
}
