package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Transient;

public interface IProcessLog extends IAttributes {

    @Transient
    Long getId();
    void setId(Long id);

    @Transient
    Long getProcessId();
    void setProcessId(Long processId);

    @Transient
    Date getCreateDate();
    void setCreateDate(Date date);

    @Transient
    Severity getSeverity();
    void setSeverity(Severity severity);

    @Transient
    String getContent();
    void setContent(String content);
    @Transient
    String getAttribute(String name);
    @Transient
    String getAttributeNotNull(String name);

    @Transient
    String getNodeId();
    void setNodeId(String nodeId);

    @Transient
    Long getTokenId();
    void setTokenId(Long tokenId);

    @Transient
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
