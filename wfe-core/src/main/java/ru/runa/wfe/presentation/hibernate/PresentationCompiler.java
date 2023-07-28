package ru.runa.wfe.presentation.hibernate;

import java.util.List;
import org.hibernate.Query;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;

/**
 * Creates {@link Query} to load data according to {@link BatchPresentation}.
 */
public class PresentationCompiler<T> implements BatchPresentationCompiler<T> {

    /**
     * {@link BatchPresentation}, used to load data.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Creates component to build loading data {@link Query}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to load data.
     */
    public PresentationCompiler(BatchPresentation batchPresentation) {
        this.batchPresentation = batchPresentation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<T> getBatch(CompilerParameters parameters) {
        return getBatchQuery(parameters).list();
    }

    @Override
    public final int getCount(CompilerParameters parameters) {
        return ((Number) getBatchQuery(new CompilerParameters(parameters, true)).uniqueResult()).intValue();
    }

    /**
     * Creates query to load data from database.
     * 
     * @param compilerParams
     *            {@link Query} creation parameters.
     * @return {@link Query} to load data from database.
     */
    protected Query getBatchQuery(CompilerParameters compilerParams) {
        HibernateCompilerQueryBuilder builder = new HibernateCompilerQueryBuilder(batchPresentation, compilerParams);
        Query query = builder.build();
        builder.getPlaceholders().apply(query);
        if (compilerParams.isPagingEnabled() && batchPresentation.getRangeSize() != BatchPresentationConsts.RANGE_SIZE_UNLIMITED) {
            query.setFirstResult((batchPresentation.getPageNumber() - 1) * batchPresentation.getRangeSize());
            query.setMaxResults(batchPresentation.getRangeSize());
        }
        return query;
    }
}
