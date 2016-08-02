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

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.script.AdmScript;
import ru.runa.wfe.script.dao.ScriptDAO;

public class ScriptLogic extends CommonLogic {
    @Autowired
    private ScriptDAO scriptDAO;

    public void updateSript(AdmScript script) {
        scriptDAO.update(script);
    }

    public List<AdmScript> getScripts() {
        return scriptDAO.getAll();
    }

    public AdmScript getScript(Long scriptId) {
        return scriptDAO.get(scriptId);
    }

    public void deleteScript(Long scriptId) {
        final AdmScript script = scriptDAO.get(scriptId);
        scriptDAO.delete(script);
    }
}
