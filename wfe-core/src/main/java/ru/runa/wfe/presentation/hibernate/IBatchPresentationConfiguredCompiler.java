package ru.runa.wfe.presentation.hibernate;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;

public interface IBatchPresentationConfiguredCompiler<T> extends IBatchPresentationCompiler<T> {

    /**
     * Creates query to load data according to {@link BatchPresentation}. Query parameters is stored in configured compiler.
     * 
     * @return Loaded data.
     */
    public List<T> getBatch();

    /**
     * Creates query to load data count according to {@link BatchPresentation}. Query parameters is stored in configured compiler.
     * 
     * @return Loaded rows count account to {@link BatchPresentation} and query parameters (paging is ignored).
     */
    public int getCount();
}
