package ru.runa.wfe.var.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.var.ArchivedVariable;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.CurrentVariable;

@Component
public class VariableDao extends GenericDao2<BaseVariable, CurrentVariable, CurrentVariableDao, ArchivedVariable, ArchivedVariableDao> {

    @Autowired
    VariableDao(CurrentVariableDao dao1, ArchivedVariableDao dao2) {
        super(dao1, dao2);
    }
}
