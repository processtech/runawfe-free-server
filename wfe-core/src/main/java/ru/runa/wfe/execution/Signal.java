package ru.runa.wfe.execution;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import javax.jms.JMSException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;

@Entity
@Table(name = "BPM_SIGNAL")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Signal implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int STRING_LENGTH = 1024;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SIGNAL", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
    @Version
    @Column(name = "VERSION")
    private Long version;
    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;
    @Column(name = "EXPIRY_DATE")
    private Date expiryDate;
    @Lob
    @Column(length = 16777216, name = "MESSAGE_SELECTORS_MAP", nullable = false)
    private byte[] messageSelectorsMap;
    @Lob
    @Column(length = 16777216, name = "MESSAGE_DATA_MAP", nullable = false)
    private byte[] messageDataMap;
    @Column(name = "MESSAGE_SELECTORS", length = STRING_LENGTH)
    @Index(name = "IX_MESSAGE_SELECTORS")
    private String messageSelectorsValue;
    @Column(name = "MESSAGE_DATA", length = STRING_LENGTH)
    private String messageDataValue;

    public Signal() {
    }

    public Signal(Date createDate, Map<String, String> routingData, Map<String, Object> payloadData, Date expiryDate) throws JMSException {
        this.createDate = createDate;
        this.messageSelectorsMap = (byte[]) new SerializableToByteArrayConverter().convert(null, null, routingData);
        this.messageDataMap = (byte[]) new SerializableToByteArrayConverter().convert(null, null, payloadData);
        if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                this.messageSelectorsValue = Utils.getObjectMessageStrictSelector(routingData);
            } else {
                // skipNulls
                this.messageSelectorsValue = Joiner.on("; ").join(Utils.getObjectMessageCombinationSelectors(routingData));
            }
            if (STRING_LENGTH / 2 < this.messageSelectorsValue.length()) {
                this.messageSelectorsValue = null;
            }
        }
        this.messageDataValue = Utils.getCuttedString(payloadData.toString(), STRING_LENGTH / 2);
        this.expiryDate = expiryDate;
    }

    public Map<String, String> getRoutingData() {
        return (Map<String, String>) new SerializableToByteArrayConverter().revert(messageSelectorsMap);
    }

    public Map<String, Object> getPayloadData() {
        return (Map<String, Object>) new SerializableToByteArrayConverter().revert(messageDataMap);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("selectors", messageSelectorsValue).add("data", messageDataValue).toString();
    }

}
