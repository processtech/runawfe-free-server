package ru.runa.wfe.datafile.builder;

import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.user.User;

@Component
public class PermissionsDataFileBuilder implements DataFileBuilder {

    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Override
    public void build(ZipOutputStream zos, Document script, User user) {
        authorizationLogic.exportDataFile(user, script);
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
