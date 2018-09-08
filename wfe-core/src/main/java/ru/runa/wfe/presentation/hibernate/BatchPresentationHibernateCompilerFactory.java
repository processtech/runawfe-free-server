package ru.runa.wfe.presentation.hibernate;

import ru.runa.wfe.presentation.BatchPresentation;

public class BatchPresentationHibernateCompilerFactory<T> implements BatchPresentationCompilerFactory<T> {

    @Override
    public BatchPresentationCompiler<T> createCompiler(BatchPresentation batchPresentation) {
        return new PresentationCompiler<>(batchPresentation);
    }

    @Override
    public BatchPresentationConfiguredCompiler<T> createCompiler(BatchPresentation batchPresentation, CompilerParameters parameters) {
        return new PresentationConfiguredCompiler<>(batchPresentation, parameters);
    }
}
