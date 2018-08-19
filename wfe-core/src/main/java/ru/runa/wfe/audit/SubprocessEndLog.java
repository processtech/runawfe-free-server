package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;

/**
 * Logging sub-process creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "Z")
public class SubprocessEndLog extends NodeLeaveLog implements ISubprocessEndLog {
    private static final long serialVersionUID = 1L;

    public SubprocessEndLog() {
    }

    public SubprocessEndLog(Node processStateNode, Token parentToken, Process subProcess) {
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
