package ru.runa.wfe.audit;

import com.google.common.base.Preconditions;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, String> attributes;
    private String serializedAttributes;
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
        return serializedAttributes;
    }

    protected void setContent(String serializedAttributes) {
        this.serializedAttributes = serializedAttributes;
    }

    public void serializeAttributes() {
        if (attributes != null) {
            serializedAttributes = XmlUtils.serialize(attributes);
        }
    }

    // protected does not work due to https://github.com/mapstruct/mapstruct/issues/1689
    @Transient
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            if (serializedAttributes == null) {
                attributes = new HashMap<>();
            } else {
                attributes = XmlUtils.deserialize(serializedAttributes);
            }
        }
        return attributes;
    }

    @Override
    public String getAttribute(String name) {
        return getAttributes().get(name);
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
        String s = getClass().getSimpleName();
        if (s.startsWith("Current")) {
            return s.substring(7);
        }
        if (s.startsWith("Archived")) {
            return s.substring(8);
        }
        throw new RuntimeException("Unsupported class name: " + getClass().getSimpleName());
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
