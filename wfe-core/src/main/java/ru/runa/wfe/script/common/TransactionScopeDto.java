package ru.runa.wfe.script.common;

import javax.xml.bind.annotation.XmlAttribute;

public class TransactionScopeDto extends OperationsListContainer {
    @XmlAttribute(name = "transactionScope")
    public TransactionScopeType transactionScope;

}
