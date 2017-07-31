package ru.runa.wfe.commons.hibernate;

import org.hibernate.dialect.OracleDialect;
import org.hibernate.id.SequenceIdentityGenerator;

public class OracleIdentityDialect extends OracleDialect {

    @Override
    public Class getNativeIdentifierGeneratorClass() {
        return SequenceIdentityGenerator.class;
    }

}
