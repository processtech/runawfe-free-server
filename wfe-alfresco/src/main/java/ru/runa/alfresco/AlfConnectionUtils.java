package ru.runa.alfresco;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;

import ru.runa.wfe.InternalApplicationException;

public class AlfConnectionUtils {

    public static RetryingTransactionHelper getTransactionHelper(AlfConnection alfConnection) {
        if (alfConnection instanceof LocalAlfConnection) {
            return ((LocalAlfConnection) alfConnection).getRegistry().getRetryingTransactionHelper();
        }
        throw new InternalApplicationException("Transaction helper is defined only for local alf connection");
    }

    public static ServiceRegistry getServiceRegistry(AlfConnection alfConnection) {
        if (alfConnection instanceof LocalAlfConnection) {
            return ((LocalAlfConnection) alfConnection).getRegistry();
        }
        throw new InternalApplicationException("Service registry is defined only for local alf connection");
    }

}
