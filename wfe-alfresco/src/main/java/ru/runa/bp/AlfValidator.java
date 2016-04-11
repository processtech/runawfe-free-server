package ru.runa.bp;

import ru.runa.alfresco.RemoteAlfConnection;
import ru.runa.alfresco.RemoteAlfConnector;
import ru.runa.wfe.validation.FieldValidator;

/**
 * Base class for RunaWFE validator.
 * 
 * @author dofs
 */
public abstract class AlfValidator extends FieldValidator {

    protected abstract void validate(RemoteAlfConnection alfConnection);

    @Override
    public final void validate() {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                validate(alfConnection);
                return null;
            }

        }.runInSession();
    }

}
