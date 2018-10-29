package ru.runa.wfe.script.dao;

import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.script.AdminScript;
import ru.runa.wfe.script.QAdminScript;

@Component
public class AdminScriptDao extends GenericDao<AdminScript> {

    public AdminScriptDao() {
        super(AdminScript.class);
    }

    public AdminScript getByName(String name) {
        val as = QAdminScript.adminScript;
        return queryFactory.selectFrom(as).where(as.name.eq(name)).fetchFirst();
    }
}
