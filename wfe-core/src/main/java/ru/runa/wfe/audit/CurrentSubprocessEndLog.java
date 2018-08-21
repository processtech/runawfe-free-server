package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.lang.Node;

/**
 * Logging sub-process creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "Z")
public class CurrentSubprocessEndLog extends CurrentNodeLeaveLog implements SubprocessEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentSubprocessEndLog() {
    }

    public CurrentSubprocessEndLog(Node processStateNode, CurrentToken parentToken, CurrentProcess subProcess) {
        super(processStateNode);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
        addAttribute(ATTR_TOKEN_ID, parentToken.getId().toString());
    }

    @Override
    @Transient
    public Long getParentTokenId() {
        return TypeConversionUtil.convertTo(long.class, getAttribute(ATTR_TOKEN_ID));
    }
}
