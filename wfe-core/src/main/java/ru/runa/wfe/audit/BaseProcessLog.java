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

    @Override
    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public void setCreateDate(Date date) {
        this.createDate = date;
    }

    @Override
    @Column(name = "SEVERITY", nullable = false, length = 1024)
    @Enumerated(EnumType.STRING)
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    @Column(name = "CONTENT", length = 4000)
    public String getContent() {
        return XmlUtils.serialize(attributes);
    }

    @Override
    public void setContent(String content) {
        attributes = XmlUtils.deserialize(content);
    }

    protected void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    protected void addAttributeWithTruncation(String name, String value) {
        if (value.length() > getAttributeMaxLength()) {
            value = value.substring(0, getAttributeMaxLength()) + "...";
        }
        addAttribute(name, value);
    }

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

    @Override
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 16777216, name = "BYTES")
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

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
