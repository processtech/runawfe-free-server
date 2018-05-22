package ru.runa.wf.web.datafile.builder;

import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class PermissionsDataFileBuilder implements DataFileBuilder {

    private User user;

    public PermissionsDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        Delegates.getAuthorizationService().exportDataFile(user, script);
    }
}
