package ru.runa.wfe.extension;

/**
 * Provides common interface for configurable artifacts.
 * 
 * @author dofs
 * @since 4.0
 */
public interface Configurable {

    /**
     * Configures bean.
     */
    void setConfiguration(String configuration) throws Exception;
}
