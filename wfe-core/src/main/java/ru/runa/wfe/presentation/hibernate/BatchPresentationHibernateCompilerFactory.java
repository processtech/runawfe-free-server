package ru.runa.wfe.presentation.hibernate;

import ru.runa.wfe.presentation.BatchPresentation;

public class BatchPresentationHibernateCompilerFactory<T extends Object> implements IBatchPresentationCompilerFactory<T> {

    @Override
    public IBatchPresentationCompiler<T> createCompiler(BatchPresentation batchPresentation) {
        return new PresentationCompiler<T>(batchPresentation);
    }

    @Override
    public IBatchPresentationConfiguredCompiler<T> createCompiler(BatchPresentation batchPresentation, CompilerParameters parameters) {
        return new PresentationConfiguredCompiler<T>(batchPresentation, parameters);
    }
}
