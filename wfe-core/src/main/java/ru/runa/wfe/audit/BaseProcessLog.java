package ru.runa.wfe.audit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Archived entities are read-lony.
 * <p>
 * But to avoid (at least for now) problems with field-based access in complex entity hierarchies, Archived* entity setters are defined private,
 * or protected if inherited (I defined abstract setters in this class just in case, because Hibernate requires both getters and setters).
 *
 * @see ru.runa.wfe.commons.hibernate.WfeInterceptor
 */
@MappedSuperclass
public abstract class BaseProcessLog implements ProcessLog {

    public static int getAttributeMaxLength() {
        return SystemProperties.getLogMaxAttributeValueLength();
    }

    protected Long tokenId;
    protected Date createDate;
    protected Severity severity = Severity.DEBUG;
    @XmlTransient
    protected HashMap<String, String> attributes = Maps.newHashMap();
    protected byte[] bytes;
    protected String nodeId;

    @Override
    @Column(name = "TOKEN_ID")
    public Long getTokenId() {
        return tokenId;
    }

    protected abstract void setTokenId(Long tokenId);

    @Override
    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    protected abstract void setCreateDate(Date date);

    @Override
    @Column(name = "SEVERITY", nullable = false, length = 1024)
    @Enumerated(EnumType.STRING)
    public Severity getSeverity() {
        return severity;
    }

    protected abstract void setSeverity(Severity severity);

    @Override
    @Column(name = "CONTENT", length = 4000)
    public String getContent() {
        return XmlUtils.serialize(attributes);
    }

    protected abstract void setContent(String content);

    @Override
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public String getAttributeNotNull(String name) {
        String s = getAttribute(name);
        Preconditions.checkNotNull(s, name);
        return s;
    }

    @Override
    @Column(name = "NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    protected abstract void setNodeId(String nodeId);

    @Override
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 16777216, name = "BYTES")
    public byte[] getBytes() {
        return bytes;
    }

    protected abstract void setBytes(byte[] bytes);

    @Override
    @Transient
    public String getPatternName() {
        return getClass().getSimpleName();
    }

    @Override
    public int compareTo(ProcessLog o) {
        int dateCompare = createDate.compareTo(o.getCreateDate());
        if (dateCompare != 0) {
            return dateCompare;
        }
        return getId().compareTo(o.getId());
    }

    @Override
    public final String toString(String pattern, Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }
}
