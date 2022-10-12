package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
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
@DiscriminatorValue(value = "B")
public class CurrentSubprocessStartLog extends CurrentNodeLog implements SubprocessStartLog {
    private static final long serialVersionUID = 1L;

    public CurrentSubprocessStartLog() {
    }

    public CurrentSubprocessStartLog(Node processStateNode, CurrentToken parentToken, CurrentProcess subProcess) {
        super(processStateNode);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
        addAttribute(ATTR_TOKEN_ID, parentToken.getId().toString());
    }

    @Override
    @Transient
    public Type getType() {
        return Type.SUBPROCESS_START;
    }

    @Override
    @Transient
    public Long getSubprocessId() {
        return TypeConversionUtil.convertTo(Long.class, getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Override
    @Transient
    public Long getParentTokenId() {
        return TypeConversionUtil.convertTo(long.class, getAttribute(ATTR_TOKEN_ID));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ProcessIdValue(getSubprocessId()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSubprocessStartLog(this);
    }
}
