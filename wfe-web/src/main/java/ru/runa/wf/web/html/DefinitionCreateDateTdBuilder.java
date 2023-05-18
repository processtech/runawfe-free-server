package ru.runa.wf.web.html;

import java.util.Date;

import ru.runa.common.WebResources;
import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;

public class DefinitionCreateDateTdBuilder extends BaseDateTdBuilder<WfDefinition> {

    @Override
    protected Date getDate(WfDefinition object) {
        return object.getCreateDate();
    }

    @Override
    protected Long getId(WfDefinition definition) {
        return definition.getId();
    }

    @Override
    protected String getActionMapping() {
        return WebResources.ACTION_MAPPING_MANAGE_DEFINITION;
    }
}
