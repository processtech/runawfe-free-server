/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.script.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.script.AdminScript;
import ru.runa.wfe.script.dao.AdminScriptDAO;

public class AdminScriptLogic extends CommonLogic {
    @Autowired
    private AdminScriptDAO scriptDAO;

    public void updateSript(AdminScript script) {
        scriptDAO.update(script);
    }

    public List<String> getScriptsNames() {
        return scriptDAO.getHibernateTemplate().find("select o.name from AdminScript o order by o.name");
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
            adminScript = scriptDAO.create(adminScript);
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
