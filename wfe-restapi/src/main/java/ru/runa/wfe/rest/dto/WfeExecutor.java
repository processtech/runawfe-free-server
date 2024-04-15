package ru.runa.wfe.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfeExecutor {
    private Long id;
    private Type type;
    private String name;
    private String description;
    private String fullName;

    public enum Type {
        EXECUTOR(BatchPresentationFactory.EXECUTORS.createNonPaged()),
        USER(BatchPresentationFactory.ACTORS.createNonPaged()),
        GROUP(BatchPresentationFactory.GROUPS.createNonPaged()),
        TEMPORARY_GROUP(BatchPresentationFactory.GROUPS.createNonPaged()),
        DELEGATION_GROUP(BatchPresentationFactory.GROUPS.createNonPaged()),
        ESCALATION_GROUP(BatchPresentationFactory.GROUPS.createNonPaged());

        private BatchPresentation batchPresentation;

        Type(BatchPresentation batchPresentation) {
            this.batchPresentation = batchPresentation;
        }

        public BatchPresentation toBatchPresentation() {
            return batchPresentation;
        }
    }
}
