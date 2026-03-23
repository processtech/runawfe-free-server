package ru.runa.wfe.validation.impl;

import org.hibernate.Session;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

public class UniqueStorageUserTypeAttributeValidator extends FieldValidator {

    @Override
    public void validate() throws Exception {
        Object value = getFieldValue();
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String fieldName = getFieldName();

        if (!fieldName.contains(UserType.DELIM)) {
            throw new InternalApplicationException("Validator should be attached to a UserType variable " + fieldName);
        }

        int dotIndex = fieldName.indexOf(UserType.DELIM);
        String parentName = fieldName.substring(0, dotIndex);
        VariableDefinition parentVd = getVariableProvider().getParsedProcessDefinition().getVariable(parentName, false);

        if (parentVd == null || parentVd.getUserType() == null) {
            throw new InternalApplicationException("Parent variable should be a UserType for field: " + fieldName);
        }

        String tableName = parentVd.getUserType().getName();
        String columnName = fieldName.substring(dotIndex + 1);
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = :val", tableName, columnName);

        Session session = ApplicationContextFactory.getSessionFactory().getCurrentSession();
        Number count = (Number) session.createSQLQuery(sql)
                .setParameter("val", value)
                .uniqueResult();

        if (count.longValue() > 0) {
            addError();
        }
    }
}
