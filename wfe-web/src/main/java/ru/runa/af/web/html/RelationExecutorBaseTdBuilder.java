package ru.runa.af.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.html.PropertyTdBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.Executor;

public abstract class RelationExecutorBaseTdBuilder implements TdBuilder {
    final SecuredObjectExtractor extractor = new SecuredObjectExtractor() {
        private static final long serialVersionUID = 1L;

        @Override
        public SecuredObject getSecuredObject(Object o, Env env) {
            return getExecutor((RelationPair) o);
        }
    };
    final PropertyTdBuilder builder = new PropertyTdBuilder(Permission.READ, "name", extractor);

    protected abstract Executor getExecutor(RelationPair relation);

    @Override
    public TD build(Object object, Env env) {
        return builder.build(getExecutor((RelationPair) object), env);
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return builder.getSeparatedValues(getExecutor((RelationPair) object), env);
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return builder.getSeparatedValuesCount(getExecutor((RelationPair) object), env);
    }

    @Override
    public String getValue(Object object, Env env) {
        return builder.getValue(getExecutor((RelationPair) object), env);
    }
}
