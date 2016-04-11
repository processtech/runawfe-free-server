package ru.runa.wfe.presentation.hibernate;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;

public interface IBatchPresentationCompiler<T> {

    /**
     * Load data according to {@link BatchPresentation} and provided compiler parameters.
     * 
     * @param parameters
     *            Parameters, used to build query for object loading.
     * @return Loaded data.
     */
    public List<T> getBatch(CompilerParameters parameters);

    /**
     * Load data count according to {@link BatchPresentation} and provided compiler parameters. Paging is ignoring.
     * 
     * @param parameters
     *            Parameters, used to build query for object loading.
     * @return Loaded data rows count.
     */
    public int getCount(CompilerParameters parameters);
}
