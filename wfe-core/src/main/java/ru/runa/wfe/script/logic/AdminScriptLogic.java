package ru.runa.wfe.script.logic;

import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.script.AdminScript;
import ru.runa.wfe.script.QAdminScript;
import ru.runa.wfe.script.dao.AdminScriptDao;

@Component
public class AdminScriptLogic extends CommonLogic {

    @Autowired
    private AdminScriptDao scriptDAO;
    @Autowired
    private HibernateQueryFactory queryFactory;

    public void updateScript(AdminScript script) {
        scriptDAO.update(script);
    }

    public List<String> getScriptsNames() {
        val as = QAdminScript.adminScript;
        return queryFactory.select(as.name).from(as).orderBy(as.name.asc()).fetch();
    }

    public List<AdminScript> getScripts() {
        return scriptDAO.getAll();
    }

    public AdminScript getScript(Long scriptId) {
        return scriptDAO.get(scriptId);
    }

    public AdminScript getScriptByName(String name) {
        return scriptDAO.getByName(name);
    }

    public void deleteScript(Long scriptId) {
        scriptDAO.delete(scriptId);
    }

    public void save(String name, byte[] script) {
        AdminScript adminScript = scriptDAO.getByName(name);
        if (null == adminScript) {
            adminScript = new AdminScript();
            adminScript.setName(name);
            adminScript.setContent(script);
            scriptDAO.create(adminScript);
        } else {
            adminScript.setContent(script);
            scriptDAO.update(adminScript);
        }
    }

    public void delete(String name) {
        AdminScript adminScript = scriptDAO.getByName(name);
        if (null == adminScript) {
            throw new InternalApplicationException("No admin script found by " + name);
        }
        scriptDAO.delete(adminScript);
    }
}
