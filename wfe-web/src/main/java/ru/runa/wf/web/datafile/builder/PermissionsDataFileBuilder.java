package ru.runa.wf.web.datafile.builder;

import java.util.List;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class PermissionsDataFileBuilder implements DataFileBuilder {
    private final List<? extends SecuredObject> identifiablies;
    private final String xmlElement;
    private final User user;
    private final boolean handleName;

    public PermissionsDataFileBuilder(User user, List<? extends SecuredObject> identifiablies, String xmlElement, boolean handleName) {
        this.user = user;
        this.identifiablies = identifiablies;
        this.xmlElement = xmlElement;
        this.handleName = handleName;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        for (SecuredObject securedObject : identifiablies) {
            List<Executor> executors = Delegates.getAuthorizationService().getExecutorsWithPermission(user, securedObject,
                    BatchPresentationFactory.EXECUTORS.createDefault(), true);
            for (Executor executor : executors) {
                List<Permission> permissions = Delegates.getAuthorizationService().getIssuedPermissions(user, executor, securedObject);
                if (permissions.isEmpty()) {
                    // this is the case for privileged executors
                    continue;
                }
                Element element = script.getRootElement().addElement(xmlElement, XmlUtils.RUNA_NAMESPACE);
                if (handleName) {
                    element.addAttribute("name", getSecuredObjectName(securedObject));
                }
                element.addAttribute("executor", executor.getName());
                for (Permission permission : permissions) {
                    Element permissionElement = element.addElement("permission", XmlUtils.RUNA_NAMESPACE);
                    // TODO This is temporary hack, until rm659 & rm660 are done; then replace this line with commented out next one.
                    //      See also related TO_DO in Permission.valueOf().
                    permissionElement.addAttribute("name", "permission." + permission.getName().toLowerCase());
                    //permissionElement.addAttribute("name", permission.getName());
                }
            }
        }
    }

    private String getSecuredObjectName(SecuredObject securedObject) {
        if (securedObject instanceof Actor) {
            return ((Actor) securedObject).getName();
        }
        if (securedObject instanceof Group) {
            return ((Group) securedObject).getName();
        }
        if (securedObject instanceof WfDefinition) {
            return ((WfDefinition) securedObject).getName();
        }
        if (securedObject instanceof BotStation) {
            return ((BotStation) securedObject).getName();
        }
        if (securedObject instanceof Relation) {
            return ((Relation) securedObject).getName();
        }
        return "";
    }
}
