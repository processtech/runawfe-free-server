package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.lang.Node;

/**
 * Logging sub-process creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "B")
public class CurrentSubprocessStartLog extends CurrentNodeEnterLog implements SubprocessStartLog {
    private static final long serialVersionUID = 1L;

    public CurrentSubprocessStartLog() {
    }

    public CurrentSubprocessStartLog(Node processStateNode, CurrentToken parentToken, CurrentProcess subProcess) {
        super(processStateNode);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
        addAttribute(ATTR_TOKEN_ID, parentToken.getId().toString());
    }
}
