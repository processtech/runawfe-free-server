package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Transient;

public interface IProcessLog extends IAttributes {
    Long getId();
    void setId(Long id);

    Long getProcessId();
    void setProcessId(Long processId);

    Date getCreateDate();
    void setCreateDate(Date date);

    Severity getSeverity();
    void setSeverity(Severity severity);

    String getContent();
    void setContent(String content);
    String getAttribute(String name);
    String getAttributeNotNull(String name);

    String getNodeId();
    void setNodeId(String nodeId);

    Long getTokenId();
    void setTokenId(Long tokenId);

    byte[] getBytes();
    void setBytes(byte[] bytes);

    /**
     * Applies some operation to process log instance.
     *
     * @param visitor
     *            Operation to apply.
     */
    void processBy(ProcessLogVisitor visitor);

    @Transient
    String getPatternName();

    /**
     * @return Arguments for localized pattern to format log message description.
     */
    @Transient
    Object[] getPatternArguments();

    /**
     * Formats log message description.
     *
     * @param pattern
     *            localized pattern
     * @return formatted message
     */
    String toString(String pattern, Object... arguments);
}
