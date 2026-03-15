package ru.runa.wfe.validation.impl;

import java.util.Map;
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
        String tableName;
        String columnName;

        if (fieldName.contains(UserType.DELIM)) {
            int dotIndex = fieldName.indexOf(UserType.DELIM);
            String parentName = fieldName.substring(0, dotIndex);
            VariableDefinition parentVd = getVariableProvider().getParsedProcessDefinition().getVariable(parentName, false);

            if (parentVd == null || parentVd.getUserType() == null) {
                throw new InternalApplicationException("Parent variable should be a UserType for field: " + fieldName);
            }

            tableName = parentVd.getUserType().getName();
            columnName = fieldName.substring(dotIndex + 1);
        } else {
            VariableDefinition vd = getVariableProvider().getParsedProcessDefinition().getVariable(fieldName, false);

            if (vd == null || vd.getUserType() == null) {
                throw new InternalApplicationException("Validator should be attached to a UserType variable: " + fieldName);
            }

            tableName = vd.getUserType().getName();
            columnName = fieldName;

            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(columnName);
            }
        }

        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = :val", tableName, columnName);

        Session session = ApplicationContextFactory.getSessionFactory().getCurrentSession();
        int count = (int) session.createSQLQuery(sql)
                .setParameter("val", value.toString().trim())
                .uniqueResult();

        if (count > 0) {
            addError();
        }
    }
}
