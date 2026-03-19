package ru.runa.wfe.var.logic;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;

@CommonsLog
class ByReferenceReader {

    private static final String LOG_NOT_FOUND_PREFIX = "byReference: row with id=";

    private final VariableLoader variableLoader;
    private final Process process;

    ByReferenceReader(VariableLoader variableLoader, Process process) {
        this.variableLoader = variableLoader;
        this.process = process;
    }

    WfVariable resolve(WfVariable wfVariable) {
        Object value = wfVariable.getValue();
        if (value == null) {
            return wfVariable;
        }
        Long id = extractId(value);
        if (id == null) {
            return wfVariable;
        }
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
        UserTypeMap fullMap = refService.loadById(wfVariable.getDefinition().getUserType(), id);
        if (fullMap == null) {
            throw new InternalApplicationException(LOG_NOT_FOUND_PREFIX + id
                    + " not found in InternalStorage for type " + wfVariable.getDefinition().getUserType().getName());
        }
        return new WfVariable(wfVariable.getDefinition(), fullMap);
    }

    @SuppressWarnings("unchecked")
    WfVariable resolveContainer(WfVariable wfVariable) {
        Object value = wfVariable.getValue();
        if (value == null) {
            return wfVariable;
        }
        UserType[] componentUserTypes = wfVariable.getDefinition().getFormatComponentUserTypes();
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
        if (value instanceof List) {
            return resolveListContainer(wfVariable, (List<Object>) value, componentUserTypes, refService);
        }
        return wfVariable;
    }

    private WfVariable resolveListContainer(WfVariable wfVariable, List<Object> list, UserType[] componentUserTypes,
            InternalStorageReferenceService refService) {
        UserType componentUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
        if (componentUserType == null || !componentUserType.isByReference()) {
            return wfVariable;
        }
        List<Object> resolvedList = Lists.newArrayListWithCapacity(list.size());
        for (Object element : list) {
            resolvedList.add(resolveListElement(element, componentUserType, refService));
        }
        return new WfVariable(wfVariable.getDefinition(), resolvedList);
    }

    private Object resolveListElement(Object element, UserType componentUserType, InternalStorageReferenceService refService) {
        if (!(element instanceof UserTypeMap)) {
            return element;
        }
        Object rawId = ((UserTypeMap) element).get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
        if (rawId == null) {
            return element;
        }
        Long id = TypeConversionUtil.convertTo(Long.class, rawId);
        UserTypeMap fullMap = refService.loadById(componentUserType, id);
        if (fullMap != null) {
            return fullMap;
        }
        throw new InternalApplicationException(LOG_NOT_FOUND_PREFIX + id
                + " not found in InternalStorage for list component type " + componentUserType.getName());
    }

    private Long extractId(Object value) {
        if (!(value instanceof UserTypeMap)) {
            return null;
        }
        Object rawId = ((UserTypeMap) value).get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
        if (rawId == null) {
            return null;
        }
        return TypeConversionUtil.convertTo(Long.class, rawId);
    }
}
