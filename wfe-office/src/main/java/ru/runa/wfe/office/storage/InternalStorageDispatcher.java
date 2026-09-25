package ru.runa.wfe.office.storage;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.logic.InternalStorageReferenceService;
import ru.runa.wfe.var.logic.InternalStorageReferenceServiceRouter;

@CommonsLog
@Component
public class InternalStorageDispatcher {

    @Autowired
    private InternalStorageReferenceServiceRouter router;

    public static InternalStorageDispatcher getInstance() {
        return ApplicationContextFactory.getContext().getBean(InternalStorageDispatcher.class);
    }

    @FunctionalInterface
    public interface StoreOperation {
        ExecutionResult call() throws Exception;
    }

    public ExecutionResult findByFilter(WfVariable variable, UserType userType, String condition,
            VariableProvider variableProvider, StoreOperation nonByReferenceFallback) {
        if (UserType.isByReferenceVariable(variable)) {
            List<UserTypeMap> found = router.forUserType(userType).findByFilter(userType, condition, variableProvider);
            return new ExecutionResult(found);
        }
        return invoke(nonByReferenceFallback);
    }

    public ExecutionResult findIdsByFilter(WfVariable variable, UserType userType, String condition,
            VariableProvider variableProvider, StoreOperation nonByReferenceFallback) {
        if (UserType.isByReferenceVariable(variable)) {
            List<UserTypeMap> found = router.forUserType(userType).findByFilter(userType, condition, variableProvider);
            List<UserTypeMap> idOnly = new ArrayList<>(found.size());
            for (UserTypeMap m : found) {
                UserTypeMap stripped = new UserTypeMap(userType);
                stripped.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME,
                        m.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME));
                idOnly.add(stripped);
            }
            return new ExecutionResult(idOnly);
        }
        return invoke(nonByReferenceFallback);
    }

    public ExecutionResult save(WfVariable variable, StoreOperation nonByReferenceFallback) {
        return skipForByReferenceOr("INSERT", variable, nonByReferenceFallback);
    }

    public ExecutionResult update(WfVariable variable, StoreOperation nonByReferenceFallback) {
        return skipForByReferenceOr("UPDATE", variable, nonByReferenceFallback);
    }

    private ExecutionResult skipForByReferenceOr(String operation, WfVariable variable, StoreOperation nonByReferenceFallback) {
        if (UserType.isByReferenceVariable(variable)) {
            log.warn("byReference: skipping " + operation + " for variable '" + variable.getDefinition().getName()
                    + "' — " + operation.toLowerCase() + " is automatic for byReference types");
            return ExecutionResult.EMPTY;
        }
        return invoke(nonByReferenceFallback);
    }

    public ExecutionResult delete(UserType userType, String condition, VariableProvider variableProvider,
            StoreOperation nonByReferenceFallback) {
        if (userType.isByReference()) {
            InternalStorageReferenceService refService = router.forUserType(userType);
            List<UserTypeMap> found = refService.findByFilter(userType, condition, variableProvider);
            for (UserTypeMap item : found) {
                Object rawId = item.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
                if (rawId instanceof Number) {
                    long id = ((Number) rawId).longValue();
                    refService.delete(userType, id);
                    log.info("byReference DELETE: type='" + userType.getName() + "', id=" + id + ", values=" + item);
                }
            }
            log.info("byReference: condition DELETE completed for type='" + userType.getName()
                    + "', deleted=" + found.size() + ", condition='" + condition + "'");
            return ExecutionResult.EMPTY;
        }
        return invoke(nonByReferenceFallback);
    }

    private ExecutionResult invoke(StoreOperation operation) {
        try {
            return operation.call();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
