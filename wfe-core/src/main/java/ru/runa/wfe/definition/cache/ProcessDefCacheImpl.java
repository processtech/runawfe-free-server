package ru.runa.wfe.definition.cache;

import lombok.NonNull;
import lombok.val;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.hibernate.HibernateUtil;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionVersionDao;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ParsedProcessDefinition;

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

    public ProcessDefCacheImpl() {
        versionIdToParsed = createCache(versionIdToParsedCacheName);
        definitionNameToVersionId = createCache(definitionNameToVersionIdCacheName);
        definitionIdToVersionId = createCache(definitionIdToVersionIdCacheName);
    }

    public ParsedProcessDefinition getDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, long processDefinitionVersionId
    ) {
        ParsedProcessDefinition parsed;
        parsed = versionIdToParsed.get(processDefinitionVersionId);
        if (parsed != null) {
            return parsed;
        }
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        ProcessDefinition d = dwv.processDefinition;
        ProcessDefinitionVersion dv = dwv.processDefinitionVersion;

        // TODO Do we really need to unproxy? Maybe Hibernate.initialize(d), ...(dv) would be enough? Cannot ParsedProcessDefinition hold detached
        // proxies?
        dv = HibernateUtil.unproxy(dv);
        d = HibernateUtil.unproxy(d);

        val archive = new ProcessArchive(d, dv);
        parsed = archive.parseProcessDefinition();
        versionIdToParsed.put(processDefinitionVersionId, parsed);
        return parsed;
    }

    public ParsedProcessDefinition getLatestDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, @NonNull String definitionName
    ) {
        Long definitionVersionId = definitionNameToVersionId.get(definitionName);
        if (definitionVersionId != null) {
            return getDefinition(processDefinitionDao, processDefinitionVersionDao, definitionVersionId);
        }
        // TODO Suboptimal: can we use whole entities instead of just id?
        definitionVersionId = processDefinitionDao.findLatestDefinition(definitionName).processDefinitionVersion.getId();
        definitionNameToVersionId.put(definitionName, definitionVersionId);
        return getDefinition(processDefinitionDao, processDefinitionVersionDao, definitionVersionId);
    }

    public ParsedProcessDefinition getLatestDefinition(
            ProcessDefinitionDao processDefinitionDao, ProcessDefinitionVersionDao processDefinitionVersionDao, long definitionId
    ) {
        Long processDefinitionVersionId = definitionIdToVersionId.get(definitionId);
        if (processDefinitionVersionId != null) {
            return getDefinition(processDefinitionDao, processDefinitionVersionDao, processDefinitionVersionId);
        }
        // TODO Suboptimal: can we use whole entities instead of just id?
        processDefinitionVersionId = processDefinitionDao.findLatestDefinition(definitionId).processDefinitionVersion.getId();
        definitionIdToVersionId.put(definitionId, processDefinitionVersionId);
        return getDefinition(processDefinitionDao, processDefinitionVersionDao, processDefinitionVersionId);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
