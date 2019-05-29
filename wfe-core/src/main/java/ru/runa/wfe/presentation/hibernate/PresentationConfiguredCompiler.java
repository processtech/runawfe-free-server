package ru.runa.wfe.presentation.hibernate;

import java.util.List;

import org.hibernate.Query;

import ru.runa.wfe.presentation.BatchPresentation;

/**
 * Creates {@link Query} to load data according to {@link BatchPresentation}.
 */
public class PresentationConfiguredCompiler<T extends Object> extends PresentationCompiler<T> implements
        BatchPresentationConfiguredCompiler<T> {

    /**
     * Parameters, used to create last hibernate query or set explicitly to compiler.
     */
    private final CompilerParameters configuredParameters;

    /**
     * Creates component to build loading data {@link Query}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to load data.
     */
    public PresentationConfiguredCompiler(BatchPresentation batchPresentation, CompilerParameters parameters) {
        super(batchPresentation);
        this.configuredParameters = parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getBatch() {
        return getBatchQuery(new CompilerParameters(configuredParameters, false)).list();
    }

    @Override
    public int getCount() {
        return ((Number) getBatchQuery(new CompilerParameters(configuredParameters, true)).uniqueResult()).intValue();
    }
}
