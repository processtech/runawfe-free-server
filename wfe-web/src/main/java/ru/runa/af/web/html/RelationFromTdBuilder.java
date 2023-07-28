package ru.runa.af.web.html;

import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.user.Executor;

public class RelationFromTdBuilder extends RelationExecutorBaseTdBuilder {
    protected Executor getExecutor(RelationPair relation) {
        return relation.getLeft();
    }
}
