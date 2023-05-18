package ru.runa.wfe.definition.cache;

import lombok.NonNull;
import lombok.val;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionPackDao;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ParsedProcessDefinition;

class ProcessDefCacheImpl extends BaseCacheImpl {

    public static final String definitionIdToParsedCacheName = "ru.runa.wfe.definition.cache.definitionIdToParsed";
    public static final String definitionNameToLatestCacheName = "ru.runa.wfe.definition.cache.definitionNameToLatest";
    public static final String packIdToDefinitionIdCacheName = "ru.runa.wfe.definition.cache.packIdToDefinitionId";

    /**
     * Key is ProcessDefinition.id.
     */
    private final Cache<Long, ParsedProcessDefinition> definitionIdToParsed;

    /**
     * Key is ProcessDefinitionPack.name, value is ProcessDefinition.id.
     */
    private final Cache<String, Long> definitionNameToLatest;

    /**
     * Key is ProcessDefinitionPack.id, value is ProcessDefinitionPack.latest.id.
     */
    private final Cache<Long, Long> packIdToDefinitionId;

    public ProcessDefCacheImpl() {
        definitionIdToParsed = createCache(definitionIdToParsedCacheName);
        definitionNameToLatest = createCache(definitionNameToLatestCacheName);
        packIdToDefinitionId = createCache(packIdToDefinitionIdCacheName);
    }

    public ParsedProcessDefinition getDefinition(ProcessDefinitionDao processDefinitionDao, Long processDefinitionId
    ) {
        ParsedProcessDefinition parsed = definitionIdToParsed.get(processDefinitionId);
        if (parsed != null) {
            return parsed;
        }
        ProcessDefinition processDefinition = processDefinitionDao.get(processDefinitionId);
        val archive = new ProcessArchive(processDefinition);
        parsed = archive.parseProcessDefinition();
        definitionIdToParsed.put(processDefinitionId, parsed);
        return parsed;
    }

    public ParsedProcessDefinition getLatestDefinition(
            ProcessDefinitionPackDao processDefinitionPackDao, ProcessDefinitionDao processDefinitionDao, @NonNull String definitionName
    ) {
        Long definitionId = definitionNameToLatest.get(definitionName);
        if (definitionId != null) {
            return getDefinition(processDefinitionDao, definitionId);
        }
        definitionId = processDefinitionPackDao.getByName(definitionName).getLatest().getId();
        definitionNameToLatest.put(definitionName, definitionId);
        return getDefinition(processDefinitionDao, definitionId);
    }

    public ParsedProcessDefinition getLatestDefinitionByPackId(
            ProcessDefinitionPackDao processDefinitionPackDao, ProcessDefinitionDao processDefinitionDao, Long packId
    ) {
        Long processDefinitionId = packIdToDefinitionId.get(packId);
        if (processDefinitionId != null) {
            return getDefinition(processDefinitionDao, processDefinitionId);
        }
        processDefinitionId = processDefinitionPackDao.get(packId).getLatest().getId();
        packIdToDefinitionId.put(packId, processDefinitionId);
        return getDefinition(processDefinitionDao, processDefinitionId);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
