package ru.runa.wfe.var.logic;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableStorageKind;

@CommonsLog
@Component
public class InternalStorageReferenceServiceRouter {

    private final Map<VariableStorageKind, InternalStorageReferenceService> impls;

    public InternalStorageReferenceServiceRouter(List<InternalStorageReferenceService> services) {
        Map<VariableStorageKind, InternalStorageReferenceService> map = new EnumMap<>(VariableStorageKind.class);
        for (InternalStorageReferenceService service : services) {
            VariableStorageKind kind = service.getKind();
            InternalStorageReferenceService previous = map.put(kind, service);
            if (previous != null) {
                throw new InternalApplicationException("Duplicate InternalStorageReferenceService for storage kind " + kind
                        + ": " + previous.getClass().getName() + " and " + service.getClass().getName());
            }
        }
        this.impls = Collections.unmodifiableMap(map);
        log.info("Registered InternalStorageReferenceServices: " + impls.keySet());
    }

    public InternalStorageReferenceService forUserType(UserType userType) {
        VariableStorageKind kind = userType.getStorageType();
        if (kind == null) {
            throw new InternalApplicationException(
                    "InternalStorageReferenceServiceRouter called for non-reference user type '"
                            + userType.getName() + "' (caller must guard with isByReference())");
        }
        InternalStorageReferenceService service = impls.get(kind);
        if (service == null) {
            throw new InternalApplicationException(
                    "No InternalStorageReferenceService registered for storage kind " + kind
                            + " (user type '" + userType.getName() + "'); registered kinds: " + impls.keySet());
        }
        return service;
    }
}
