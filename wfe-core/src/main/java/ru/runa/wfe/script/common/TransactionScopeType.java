package ru.runa.wfe.script.common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(value = String.class)
public enum TransactionScopeType {

    @XmlEnumValue(value = "operation")
    TRANSACTION_PER_OPERATION {
        @Override
        public boolean isPerOperation() {
            return true;
        }
    },

    @XmlEnumValue(value = "all")
    TRANSACTION_PER_SCOPE {
        @Override
        public boolean isPerOperation() {
            return false;
        }
    };

    public abstract boolean isPerOperation();
}
