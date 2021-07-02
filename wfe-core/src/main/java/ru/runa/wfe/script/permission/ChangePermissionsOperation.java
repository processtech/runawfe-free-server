package ru.runa.wfe.script.permission;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;

@XmlTransient()
public abstract class ChangePermissionsOperation extends ScriptOperation {

    @XmlAttribute(name = "executor", required = true)
    protected String xmlExecutor;

    @XmlAttribute(name = "id")
    protected String xmlId;

    @XmlAttribute(name = "type")
    protected String xmlType;

    @XmlAttribute(name = "name")
    private String xmlName;

    @XmlElement(name = "namedIdentitySet", namespace = AdminScriptConstants.NAMESPACE)
    protected List<NamedIdentitySet> xmlNamedIdentitySets = Lists.newArrayList();

    @XmlElement(name = "permission", namespace = AdminScriptConstants.NAMESPACE)
    protected List<ru.runa.wfe.script.permission.Permission> xmlPermissions = new ArrayList<>();

    protected Map<SecuredObjectType, Set<String>> objectNames = null;
    protected Set<Permission> permissions = null;

    @Override
    public void validate(ScriptExecutionContext context) {
        if (xmlExecutor == null || xmlExecutor.isEmpty()) {
            throw new ScriptValidationException("Attribute 'executor' is missing");
        }

        if (xmlNamedIdentitySets.isEmpty() == (xmlType == null || xmlType.isEmpty())) {
            throw new ScriptValidationException("Either attribute 'type' or child 'namedIdentitySet' must be present, but not both");
        }

        if (xmlNamedIdentitySets.isEmpty()) {
            objectNames = new HashMap<>(1);
            SecuredObjectType type = SecuredObjectType.valueOf(xmlType);
            if (type.isSingleton()) {
                if (xmlName != null && !xmlName.isEmpty()) {
                    throw new ScriptValidationException("Attribute 'name' is NOT expected for singleton type " + type.getName());
                }
                objectNames.put(type, null);
            } else {
                if (xmlName == null || xmlName.isEmpty()) {
                    throw new ScriptValidationException("Attribute 'name' is expected for non-singleton type " + type.getName());
                }
                objectNames.put(type, new HashSet<String>(1) {{
                    if (!type.isSingleton()) {
                        add(xmlName);
                    }
                }});
            }
        } else {
            objectNames = new HashMap<>();
            for (NamedIdentitySet nis : xmlNamedIdentitySets) {
                SecuredObjectType type = xmlNamedIdentitySets.get(0).type.getSecuredObjectType();
                Set<String> names = nis.get(context);
                if (type.isSingleton()) {
                    if (!names.isEmpty()) {
                        throw new ScriptValidationException("Non-empty 'namedIdentitySet' is not applicable to singleton type " + type.getName());
                    }
                    objectNames.put(type, null);
                } else {
                    if (!names.isEmpty()) {
                        CollectionUtil.mapGetOrPutDefault(objectNames, type, new HashSet<>()).addAll(names);
                    }
                }
            }
        }

        permissions = new HashSet<>(xmlPermissions.size());
        for (ru.runa.wfe.script.permission.Permission xp : xmlPermissions) {
            Permission p = Permission.valueOf(xp.name);
            for (SecuredObjectType type : objectNames.keySet()) {
                ApplicablePermissions.check(type, p);
            }
            permissions.add(p);
        }
    }
}
