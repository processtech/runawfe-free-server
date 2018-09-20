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
 * aLong with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.cache;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import lombok.var;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.hibernate.HibernateUtil;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionVersionDao;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@CommonsLog
class ProcessDefCacheImpl extends BaseCacheImpl {

    public static final String versionIdToParsedCacheName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String definitionNameToVersionIdCacheName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";
    public static final String definitionIdToVersionIdCacheName = "ru.runa.wfe.definition.cache.deploymentIdToDeploymentVersionId";

    /**
     * Key is ProcessDefinitionVersion.id.
     */
    private final Cache<Long, ParsedProcessDefinition> versionIdToParsed;

    /**
     * Key is ProcessDefinition.name, value is ProcessDefinitionVersion.id.
     */
    private final Cache<String, Long> definitionNameToVersionId;

    /**
     * Key is ProcessDefinition.name, value is ProcessDefinitionVersion.id.
     */
    private final Cache<Long, Long> definitionIdToVersionId;

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public ProcessDefCacheImpl() {
        versionIdToParsed = createCache(versionIdToParsedCacheName);
        definitionNameToVersionId = createCache(definitionNameToVersionIdCacheName);
        definitionIdToVersionId = createCache(definitionIdToVersionIdCacheName);
    }

    private ProcessDefCacheImpl(ProcessDefCacheImpl source) {
        versionIdToParsed = source.versionIdToParsed;
        definitionNameToVersionId = source.definitionNameToVersionId;
        definitionIdToVersionId = source.definitionIdToVersionId;
    }

    public ParsedProcessDefinition getDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, long processDefinitionVersionId
    ) {
        ParsedProcessDefinition parsed;
        // synchronized (this) {
        parsed = versionIdToParsed.get(processDefinitionVersionId);
        if (parsed != null) {
            return parsed;
        }
        // }
        var dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        var d = dwv.processDefinition;
        var dv = dwv.processDefinitionVersion;

        // TODO Do we really need to unproxy? Maybe Hibernate.initialize(d), ...(dv) would be enoug? Cannot ParsedProcessDefinition hold detached proxies?
        dv = HibernateUtil.unproxy(dv);
        d = HibernateUtil.unproxy(d);

        val archive = new ProcessArchive(d, dv);
        parsed = archive.parseProcessDefinition();
        // synchronized (this) {
        versionIdToParsed.put(processDefinitionVersionId, parsed);
        // }
        return parsed;
    }

    public ParsedProcessDefinition getLatestDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, @NonNull String definitionName
    ) {
        Long definitionVersionId;
        // synchronized (this) {
        definitionVersionId = definitionNameToVersionId.get(definitionName);
        if (definitionVersionId != null) {
            return getDefinition(processDefinitionDao, processDefinitionVersionDao, definitionVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        definitionVersionId = processDefinitionDao.findLatestDefinition(definitionName).processDefinitionVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                definitionNameToVersionId.put(definitionName, definitionVersionId);
            }
        }
        return getDefinition(processDefinitionDao, processDefinitionVersionDao, definitionVersionId);
    }

    public ParsedProcessDefinition getLatestDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, long definitionId
    ) {
        Long processDefinitionVersionId;
        // synchronized (this) {
        processDefinitionVersionId = definitionIdToVersionId.get(definitionId);
        if (processDefinitionVersionId != null) {
            return getDefinition(processDefinitionDao, processDefinitionVersionDao, processDefinitionVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        processDefinitionVersionId = processDefinitionDao.findLatestDefinition(definitionId).processDefinitionVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                definitionIdToVersionId.put(definitionId, processDefinitionVersionId);
            }
        }
        return getDefinition(processDefinitionDao, processDefinitionVersionDao, processDefinitionVersionId);
    }

    @Override
    public CacheImplementation unlock() {
        return new ProcessDefCacheImpl(this);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        if (changedObject.changeType == Change.CREATE) {
            return true;
        }

        if (changedObject.object instanceof ProcessDefinition) {

            val d = (ProcessDefinition) changedObject.object;
            Preconditions.checkArgument(d.getId() != null);
            isLocked.set(true);
            onChangeDeploymentImpl(d);
            return true;

        } else if (changedObject.object instanceof ProcessDefinitionVersion) {

            var dv = (ProcessDefinitionVersion) changedObject.object;
            Preconditions.checkArgument(dv.getId() != null);
            isLocked.set(true);
            versionIdToParsed.remove(dv.getId());
            dv = HibernateUtil.unproxyWithoutInitialize(dv);
            if (dv != null && dv.getDefinition() != null) {
                onChangeDeploymentImpl(dv.getDefinition());
            }
            return true;

        } else {

            log.error("Unexpected object " + changedObject.object);
            return false;
        }
    }

    private void onChangeDeploymentImpl(ProcessDefinition d) {
        versionIdToParsed.remove(definitionIdToVersionId.getAndRemove(d.getId()));
        versionIdToParsed.remove(definitionNameToVersionId.getAndRemove(d.getName()));
    }
}
