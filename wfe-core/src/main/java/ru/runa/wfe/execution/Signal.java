/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.execution;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
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

    public Signal(ObjectMessage message) throws JMSException {
        this.createDate = new Date();
        if (message.getJMSExpiration() != 0) {
            this.expiryDate = new Date(message.getJMSExpiration());
        }
        // TODO testing
        this.messageSelectorsMap = (byte[]) new SerializableToByteArrayConverter().convert(null, null, getMessageSelectorsMap(message));
        this.messageDataMap = (byte[]) new SerializableToByteArrayConverter().convert(null, null, message.getObject());
        if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                this.messageSelectorsValue = Utils.getObjectMessageStrictSelector(message);
            } else {
                // skipNulls
                this.messageSelectorsValue = Joiner.on("; ").join(Utils.getObjectMessageCombinationSelectors(message));
            }
            if (STRING_LENGTH / 2 < this.messageSelectorsValue.length()) {
                this.messageSelectorsValue = null;
            }
        }
        this.messageDataValue = Utils.getCuttedString(message.getObject().toString(), STRING_LENGTH / 2);
    }

    public Map<String, String> getMessageSelectorsMap() {
        return (Map<String, String>) new SerializableToByteArrayConverter().revert(messageSelectorsMap);
    }

    public Map<String, Object> getMessageData() {
        return (Map<String, Object>) new SerializableToByteArrayConverter().revert(messageDataMap);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("selectors", messageSelectorsValue).add("data", messageDataValue).toString();
    }

    private Map<String, String> getMessageSelectorsMap(ObjectMessage message) throws JMSException {
        Map<String, String> result = new HashMap<>();
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            if (!propertyName.startsWith("JMS")) {
                result.put(propertyName, message.getStringProperty(propertyName));
            }
        }
        return result;
    }

}
