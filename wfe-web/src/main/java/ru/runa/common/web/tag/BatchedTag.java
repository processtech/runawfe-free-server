package ru.runa.common.web.tag;

import ru.runa.wfe.presentation.BatchPresentation;

public interface BatchedTag {
    public void setBatchPresentationId(String batchPresentationId);

    public String getBatchPresentationId();

    public BatchPresentation getBatchPresentation();
}
