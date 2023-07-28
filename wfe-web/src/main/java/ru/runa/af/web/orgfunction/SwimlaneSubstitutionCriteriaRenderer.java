package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@CommonsLog
public class SwimlaneSubstitutionCriteriaRenderer implements ParamRenderer {

    @Override
    public boolean hasJSEditor() {
        return true;
    }

    @Override
    public List<String[]> loadJSEditorData(User user) {
        List<String[]> result = new ArrayList<>();
        DefinitionService definitionService = Delegates.getDefinitionService();
        List<WfDefinition> definitions = definitionService.getProcessDefinitions(user, BatchPresentationFactory.DEFINITIONS.createDefault(), false);
        for (WfDefinition definition : definitions) {
            try {
                List<SwimlaneDefinition> swimlanes = definitionService.getSwimlaneDefinitions(user, definition.getId());
                for (SwimlaneDefinition swimlaneDefinition : swimlanes) {
                    String swimlaneName = definition.getName() + "." + swimlaneDefinition.getName();
                    result.add(new String[] { swimlaneName, swimlaneName });
                }
            } catch (Exception e) {
                log.debug(e);
            }
        }
        return result;
    }

    @Override
    public String getDisplayLabel(User user, String value) {
        return value;
    }

    @Override
    public boolean isValueValid(User user, String value) {
        return value.trim().length() > 0;
    }

}
