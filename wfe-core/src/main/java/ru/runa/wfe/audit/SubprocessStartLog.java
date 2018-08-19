package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;

/**
 * Logging sub-process creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "B")
public class SubprocessStartLog extends NodeEnterLog implements ISubprocessStartLog {
    private static final long serialVersionUID = 1L;

    public SubprocessStartLog() {
    }

    public SubprocessStartLog(Node processStateNode, Token parentToken, Process subProcess) {
        super(processStateNode);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
        addAttribute(ATTR_TOKEN_ID, parentToken.getId().toString());
    }
}
