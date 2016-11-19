package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ProcessIdValue;
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
@DiscriminatorValue(value = "B")
public class SubprocessStartLog extends NodeEnterLog {
    private static final long serialVersionUID = 1L;

    public SubprocessStartLog() {
    }

    public SubprocessStartLog(Node processStateNode, Token parentToken, Process subProcess) {
        super(processStateNode);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
        addAttribute(ATTR_TOKEN_ID, parentToken.getId().toString());
    }

    @Transient
    public Long getSubprocessId() {
        return TypeConversionUtil.convertTo(Long.class, getAttributeNotNull(ATTR_PROCESS_ID));
    }

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
