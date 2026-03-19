package ru.runa.wfe.var.logic;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.VariableLoader;

@CommonsLog
class ByReferenceWriter {

    private final VariableLoader variableLoader;
    private final Process process;
    private final ByReferenceLogger logger;

    ByReferenceWriter(VariableLoader variableLoader, Process process, ByReferenceLogger logger) {
        this.variableLoader = variableLoader;
        this.process = process;
        this.logger = logger;
    }

    ByReferenceWriteResult write(VariableDefinition variableDefinition, Object value) {
        UserType userType = variableDefinition.getUserType();
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();

        if (value == null) {
            Long oldId = getExistingId(variableDefinition);
            if (oldId != null) {
                UserTypeMap beforeDelete = refService.loadById(userType, oldId);
                refService.delete(userType, oldId);
                log.info("byReference: implicit delete from Excel for variable '" + variableDefinition.getName()
                        + "', type=" + userType.getName() + ", old id=" + oldId);
                UserTypeMap toLog = beforeDelete != null ? beforeDelete : new UserTypeMap(userType);
                toLog.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, oldId);
                logger.logVariable("DELETE", variableDefinition, oldId, toLog);
            }
            return ByReferenceWriteResult.save(null);
        }
        if (!(value instanceof UserTypeMap)) {
            throw new InternalApplicationException(
                    "byReference variable '" + variableDefinition.getName() + "' expects UserTypeMap, got " + value.getClass());
        }

        UserTypeMap fullMap = (UserTypeMap) value;
        Long id = getExistingId(variableDefinition);

        if (id == null) {
            if (!hasNonIdAttributes(fullMap)) {
                log.debug("byReference: skipping INSERT for variable '" + variableDefinition.getName()
                        + "' — all attributes are null (uninitialized variable)");
                return ByReferenceWriteResult.SKIP;
            }
            long newId = refService.insert(userType, fullMap);
            logger.logVariable("INSERT", variableDefinition, newId, fullMap);
            return ByReferenceWriteResult.save(buildIdOnlyMap(userType, newId));
        } else {
            if (hasNonIdAttributes(fullMap)) {
                refService.update(userType, id, fullMap);
                logger.logVariable("UPDATE", variableDefinition, id, fullMap);
            }
            return ByReferenceWriteResult.save(buildIdOnlyMap(userType, id));
        }
    }

    @SuppressWarnings("unchecked")
    ByReferenceWriteResult writeContainer(VariableDefinition variableDefinition, Object value) {
        UserType[] componentUserTypes = variableDefinition.getFormatComponentUserTypes();
        if (value instanceof List) {
            return ByReferenceWriteResult.save(writeListContainer(variableDefinition, (List<Object>) value, componentUserTypes));
        } else if (value == null) {
            return ByReferenceWriteResult.save(null);
        } else {
            throw new InternalApplicationException(
                    "byReference container variable '" + variableDefinition.getName() + "' expects List, got " + value.getClass());
        }
    }

    private List<Object> writeListContainer(VariableDefinition containerDef, List<Object> list, UserType[] componentUserTypes) {
        UserType componentUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
        List<Object> idOnlyList = Lists.newArrayListWithCapacity(list.size());
        if (componentUserType != null && componentUserType.isByReference()) {
            InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                if (element instanceof UserTypeMap) {
                    UserTypeMap idOnly = processListElement((UserTypeMap) element, i, containerDef, componentUserType, refService);
                    if (idOnly != null) {
                        idOnlyList.add(idOnly);
                    }
                } else {
                    idOnlyList.add(element);
                }
            }
        } else {
            idOnlyList.addAll(list);
        }
        return idOnlyList;
    }

    private UserTypeMap processListElement(UserTypeMap fullMap, int index, VariableDefinition containerDef,
            UserType componentUserType, InternalStorageReferenceService refService) {
        Long id = extractId(fullMap, index);
        if (id != null && id > 0) {
            if (hasNonIdAttributes(fullMap)) {
                refService.update(componentUserType, id, fullMap);
                logger.logContainerElement("UPDATE", containerDef, index, componentUserType, id, fullMap);
            }
        } else {
            if (!hasNonIdAttributes(fullMap)) {
                log.debug("byReference container: skipping empty element [" + index + "]");
                return null;
            }
            id = refService.insert(componentUserType, fullMap);
            logger.logContainerElement("INSERT", containerDef, index, componentUserType, id, fullMap);
        }
        return buildIdOnlyMap(componentUserType, id);
    }

    private Long extractId(UserTypeMap fullMap, int index) {
        Object rawId = fullMap.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
        if (rawId == null) {
            return null;
        }
        try {
            return TypeConversionUtil.convertTo(Long.class, rawId);
        } catch (Exception e) {
            log.warn("byReference container: cannot parse id '" + rawId + "' for [" + index + "], treating as new element", e);
            return null;
        }
    }

    private Long getExistingId(VariableDefinition variableDefinition) {
        String idVariableName = variableDefinition.getName() + UserType.DELIM
                + InternalStorageReferenceService.ID_ATTRIBUTE_NAME;
        Variable<?, ?> idVar = variableLoader.get(process, idVariableName);
        if (idVar != null && idVar.getValue() != null) {
            return TypeConversionUtil.convertTo(Long.class, idVar.getValue());
        }
        return null;
    }

    private UserTypeMap buildIdOnlyMap(UserType userType, Long id) {
        UserTypeMap idOnlyMap = new UserTypeMap(userType);
        idOnlyMap.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, id);
        return idOnlyMap;
    }

    private boolean hasNonIdAttributes(UserTypeMap map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (!InternalStorageReferenceService.ID_ATTRIBUTE_NAME.equals(entry.getKey())
                    && val != null
                    && !(val instanceof String && ((String) val).isEmpty())) {
                return true;
            }
        }
        return false;
    }
}
