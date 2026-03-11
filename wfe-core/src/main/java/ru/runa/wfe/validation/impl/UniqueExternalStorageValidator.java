package ru.runa.wfe.validation.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.VariableDefinition;

public class UniqueExternalStorageValidator extends FieldValidator {

    @Override
    public void validate() throws Exception {
        Object value = getFieldValue();
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        VariableDefinition vd = getVariableProvider().getParsedProcessDefinition().getVariable(getFieldName(), true);

        String tableName;
        String columnName;

        if(vd.getUserType() != null){
            tableName = vd.getUserType().getName();
        }else {
            tableName = getFieldName();
        }

        if(getFieldName().contains(".")){
            columnName = getFieldName().split("\\.")[1];
        }else {
            columnName = getFieldName();
        }
        String sql = String.format("SELECT COUNT(*) FROM \"%s\" WHERE \"%s\" = :val", tableName, columnName);

        Session session = ApplicationContextFactory.getSessionFactory().getCurrentSession();
        int count = (int) session.createSQLQuery(sql)
                .setParameter("val", value.toString().trim())
                .uniqueResult();

        if (count > 0) {
            addError("Дублирование данных!");
        }
    }

}