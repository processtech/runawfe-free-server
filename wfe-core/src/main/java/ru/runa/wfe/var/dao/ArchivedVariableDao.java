package ru.runa.wfe.var.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.var.ArchivedVariable;

@Component
public class ArchivedVariableDao extends BaseVariableDao<ArchivedVariable> {

    public ArchivedVariableDao() {
        super(ArchivedVariable.class);
    }
}
