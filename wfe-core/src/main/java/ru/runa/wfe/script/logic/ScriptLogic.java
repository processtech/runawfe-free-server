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

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.LobStorage;
import ru.runa.wfe.commons.dao.LobStorageDAO;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.script.AdmScript;
import ru.runa.wfe.script.dao.ScriptDAO;

public class ScriptLogic extends CommonLogic {
    @Autowired
    private ScriptDAO scriptDAO;
    @Autowired
    private LobStorageDAO lobStorageDAO;

    public void updateSript(AdmScript script) {
        scriptDAO.update(script);
    }

    public List<AdmScript> getScripts() {
        return scriptDAO.getAll();
    }

    public AdmScript getScript(Long scriptId) {
        return scriptDAO.get(scriptId);
    }

    public AdmScript getScriptByName(String name) {
        return scriptDAO.getByName(name);
    }

    public void deleteScript(Long scriptId) {
        final AdmScript script = scriptDAO.get(scriptId);
        scriptDAO.delete(script);
    }

    public void save(String name, byte[] script) {
        try {
            AdmScript admScript = scriptDAO.getByName(name);
            if (null == admScript) {
                LobStorage storage = new LobStorage();
                storage.setValue(new String(script, "UTF-8"));
                storage = lobStorageDAO.create(storage);
                admScript = new AdmScript();
                admScript.setName(name);
                admScript.setStorage(storage);
                admScript = scriptDAO.create(admScript);
            } else {
                LobStorage storage = admScript.getStorage();
                storage.setValue(new String(script, "UTF-8"));
                storage = lobStorageDAO.update(storage);
                admScript.setStorage(storage);
                scriptDAO.update(admScript);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String name) {
        AdmScript admScript = scriptDAO.getByName(name);
        if (null == admScript) {
            return false;
        }
        LobStorage lobStorage = admScript.getStorage();
        scriptDAO.delete(admScript);
        lobStorageDAO.delete(lobStorage);
        return true;
    }
}
