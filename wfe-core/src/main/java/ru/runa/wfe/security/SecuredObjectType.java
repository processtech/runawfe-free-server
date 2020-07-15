package ru.runa.wfe.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.NonNull;
import ru.runa.wfe.commons.xml.SecuredObejctTypeXmlAdapter;

/**
 * @see SecuredObject
 * @see SecuredSingleton
 * @see Permission
 * @see ApplicablePermissions
 * @see PermissionSubstitutions
 */
@XmlJavaTypeAdapter(SecuredObejctTypeXmlAdapter.class)
public final class SecuredObjectType implements Serializable, Comparable<SecuredObjectType> {
    private static final long serialVersionUID = 3L;

    private static HashMap<String, SecuredObjectType> instancesByName = new HashMap<>();
    private static ArrayList<SecuredObjectType> instancesList = new ArrayList<>();
    private static List<SecuredObjectType> unmodifiableInstancesList = Collections.unmodifiableList(instancesList);

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

    public static List<SecuredObjectType> values() {
        return unmodifiableInstancesList;
    }


    @Getter
    private String name;

    /**
     * Used for assertions. Currently, all non-singletons (those <code>permission_mapping.object_id != 0</code>) have parent singleton list types.
     */
    @Getter
    private boolean singleton;

    /**
     * Used by PermissionSubstitutions and its callers. Nullable.
     */
    @Getter
    private SecuredObjectType listType;

    private SecuredObjectType(String name, boolean singleton, SecuredObjectType listType) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            // permission_mapping.object_type is varchar(32)
            throw new RuntimeException("SecuredObjectType.name is empty or too large");
        }
        if (instancesByName.put(name, this) != null) {
            throw new RuntimeException("Duplicate SecuredObjectType.name \"" + name + "\"");
        }

        if (listType != null && singleton) {
            throw new RuntimeException("(listType.listType != null && singleton) for SecuredObjectType \"" + name + "\"");
        }
        if (listType != null && listType.listType != null) {
            throw new RuntimeException("(listType.listType != null) for SecuredObjectType \"" + name + "\"");
        }

        this.name = name;
        this.singleton = singleton;
        this.listType = listType;
        instancesList.add(this);
        Collections.sort(instancesList);
    }

    public SecuredObjectType(String name, @NonNull SecuredObjectType listType) {
        this(name, false, listType);
    }

    public SecuredObjectType(String name, boolean singleton) {
        this(name, singleton, null);
    }

    @Override
    public int compareTo(SecuredObjectType o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecuredObjectType) {
            return name.equals(((SecuredObjectType) obj).name);
        }
        return super.equals(obj);
    }


    // Alphabetically, please:

    public static final SecuredObjectType BOTSTATIONS = new SecuredObjectType("BOTSTATIONS", true);

    public static final SecuredObjectType DEFINITION = new SecuredObjectType("DEFINITION", false);

    public static final SecuredObjectType EXECUTOR = new SecuredObjectType("EXECUTOR", false);

    public static final SecuredObjectType PROCESS = new SecuredObjectType("PROCESS", false);

    public static final SecuredObjectType RELATIONS = new SecuredObjectType("RELATIONS", true);
    public static final SecuredObjectType RELATION = new SecuredObjectType("RELATION", RELATIONS);

    public static final SecuredObjectType REPORTS = new SecuredObjectType("REPORTS", true);
    public static final SecuredObjectType REPORT = new SecuredObjectType("REPORT", REPORTS);

    public static final SecuredObjectType SYSTEM = new SecuredObjectType("SYSTEM", true);
    
    public static final SecuredObjectType DATASOURCES = new SecuredObjectType("DATASOURCES", true);
}
