package ru.runa.wf.web.datafile.builder;

import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class PermissionsDataFileBuilder implements DataFileBuilder {

    /**
     * Parameter "user" is unused: if someone has "export datafile" permissions, they should not get permission check exceptions
     * in the middle of the process.
     */
    public PermissionsDataFileBuilder(@SuppressWarnings("unused") User user) {
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        Delegates.getAuthorizationService().exportDataFile(script);
    }
}
