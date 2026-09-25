package ru.runa.wfe.var.logic;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;

public class ByReferenceResolvingVariableLoader extends BaseProcessVariableLoader {

    private final ByReferenceVariableHandler byReferenceHandler;
    private final InternalStorageReferenceServiceRouter router;

    public ByReferenceResolvingVariableLoader(
            VariableLoader variableLoader,
            ParsedProcessDefinition parsedProcessDefinition,
            Process process,
            ByReferenceVariableHandler byReferenceHandler,
            InternalStorageReferenceServiceRouter router
    ) {
        super(variableLoader, parsedProcessDefinition, process);
        this.byReferenceHandler = byReferenceHandler;
        this.router = router;
    }

    @Override
    public WfVariable get(String name) {
        WfVariable wfVariable = super.get(name);
        if (wfVariable == null) {
            return null;
        }
        VariableDefinition definition = wfVariable.getDefinition();
        if (definition.isUserType() && definition.getUserType().isByReference()) {
            InternalStorageReferenceService refService = router.forUserType(definition.getUserType());
            wfVariable = byReferenceHandler.resolve(wfVariable, refService);
        }
        if (wfVariable != null && ByReferenceVariableHandler.isContainerOfByReference(wfVariable.getDefinition())) {
            InternalStorageReferenceService componentRefService =
                    ByReferenceVariableHandler.resolveContainerComponentService(wfVariable.getDefinition(), router);
            wfVariable = byReferenceHandler.resolveContainer(wfVariable, componentRefService);
        }
        return wfVariable;
    }
}
