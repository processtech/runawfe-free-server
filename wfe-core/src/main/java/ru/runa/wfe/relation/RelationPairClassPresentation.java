package ru.runa.wfe.relation;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;

public class RelationPairClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.relation.name";
    public static final String EXECUTOR_FROM = "batch_presentation.relation.executor_from";
    public static final String EXECUTOR_TO = "batch_presentation.relation.executor_to";

    public static final ClassPresentation INSTANCE = new RelationPairClassPresentation();

    private RelationPairClassPresentation() {
        super(RelationPair.class, "", false, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(RelationPair.class, "relation.name"), true,
                        FieldFilterMode.DATABASE, FieldState.HIDDEN),
                new FieldDescriptor(EXECUTOR_FROM, String.class.getName(), new DefaultDbSource(RelationPair.class, "left.name"), true, 1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.af.web.html.RelationFromTdBuilder", null),
                new FieldDescriptor(EXECUTOR_TO, String.class.getName(), new DefaultDbSource(RelationPair.class, "right.name"), true, 2, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.af.web.html.RelationToTdBuilder", null) });
    }
}
