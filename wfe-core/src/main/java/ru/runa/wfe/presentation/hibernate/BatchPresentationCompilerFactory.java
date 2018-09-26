package ru.runa.wfe.presentation.hibernate;

import ru.runa.wfe.presentation.BatchPresentation;

public interface BatchPresentationCompilerFactory<T> {

    public BatchPresentationCompiler<T> createCompiler(BatchPresentation batchPresentation);

    public BatchPresentationConfiguredCompiler<T> createCompiler(BatchPresentation batchPresentation, CompilerParameters parameters);
}
