package ru.runa.wfe.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.runa.wfe.commons.xml.SecuredObejctTypeXmlAdapter;

/**
 * @see Permission
 * @see ApplicablePermissions
 */
@XmlJavaTypeAdapter(SecuredObejctTypeXmlAdapter.class)
public final class SecuredObjectType implements Serializable {

    private static HashMap<String, SecuredObjectType> instancesByName = new HashMap<>();
    private static List<SecuredObjectType> instancesList = Collections.unmodifiableList(new ArrayList<SecuredObjectType>());

    /**
     * Mimics enum's valueOf() method, including thrown exception type.
     * TODO Delete this method in favor of nullSafeValueOf().
     */
    public static SecuredObjectType valueOf(String name) {
        SecuredObjectType result;
        try {
            result = instancesByName.get(name);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Illegal SecuredObjectType name");
        }
        if (result == null) {
            throw new IllegalArgumentException("Unknown SecuredObjectType name \"" + name + "\"");
        }
        return result;
    }

    /**
     * Returns unmodifiable set.
     */
    public static List<SecuredObjectType> values() {
        return instancesList;
    }


    private String name;

    public SecuredObjectType(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            // permission_mapping.object_type is varchar(32)
            throw new RuntimeException("Null, empty or too large SecuredObjectType name");
        }
        this.name = name;
        if (instancesByName.put(name, this) != null) {
            throw new RuntimeException("Duplicate SecuredObjectType name \"" + name + "\"");
        }
        ArrayList<SecuredObjectType> newInstancesList = new ArrayList<>(instancesList);
        newInstancesList.add(this);
        instancesList = Collections.unmodifiableList(newInstancesList);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


    // TODO Delete if really unused. If used, register with READ and UPDATE_PERMISSIONS permissions (as it was previously).
//    public static final SecuredObjectType NONE = new SecuredObjectType(0, "NONE");

    public static final SecuredObjectType SYSTEM = new SecuredObjectType("SYSTEM");
    public static final SecuredObjectType BOTSTATION = new SecuredObjectType("BOTSTATION");
    public static final SecuredObjectType ACTOR = new SecuredObjectType("ACTOR");
    public static final SecuredObjectType GROUP = new SecuredObjectType("GROUP");
    public static final SecuredObjectType RELATION = new SecuredObjectType("RELATION");
    public static final SecuredObjectType RELATIONGROUP = new SecuredObjectType("RELATIONGROUP");
    public static final SecuredObjectType RELATIONPAIR = new SecuredObjectType("RELATIONPAIR");
    public static final SecuredObjectType DEFINITION = new SecuredObjectType("DEFINITION");
    public static final SecuredObjectType PROCESS = new SecuredObjectType("PROCESS");
    public static final SecuredObjectType REPORT = new SecuredObjectType("REPORT");
}
