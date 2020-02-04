package ru.runa.wfe.security.logic;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.PropertyResources;

public class LdapProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("ldap.properties", false, false);

    public static Map<String, String> getAllProperties() {
        return RESOURCES.getAllProperties();
    }

    public static String getAuthenticationUsernameFormat() {
        return RESOURCES.getStringProperty("authentication.username.format");
    }

    public static String getSynchronizationImportGroupName() {
        return RESOURCES.getStringProperty("synchronization.import.group.name");
    }

    public static String getSynchronizationImportGroupDescription() {
        return RESOURCES.getStringProperty("synchronization.import.group.description");
    }

    public static String getSynchronizationWasteGroupName() {
        return RESOURCES.getStringProperty("synchronization.waste.group.name");
    }

    public static String getSynchronizationWasteGroupDescription() {
        return RESOURCES.getStringProperty("synchronization.waste.group.description");
    }

    public static boolean isSynchronizationEnabled() {
        return RESOURCES.getBooleanProperty("synchronization.enabled", false);
    }

    public static boolean isSynchronizationCreateExecutors() {
        return RESOURCES.getBooleanProperty("synchronization.create.executors.enabled", false);
    }

    public static boolean isSynchronizationUpdateExecutors() {
        return RESOURCES.getBooleanProperty("synchronization.update.executors.enabled", false);
    }

    public static boolean isSynchronizationDeleteExecutors() {
        return RESOURCES.getBooleanProperty("synchronization.delete.executors.enabled", false);
    }

    public static List<String> getSynchronizationOrganizationUnits() {
        return RESOURCES.getMultipleStringProperty("synchronization.organization.units");
    }

    public static String getSynchronizationObjectClassFilter() {
        return RESOURCES.getStringProperty("synchronization.object.class.filter", "(objectclass={0})");
    }

    public static String getSynchronizationUserObjectClass() {
        return RESOURCES.getStringProperty("synchronization.user.object.class", "user");
    }

    public static String getSynchronizationGroupObjectClass() {
        return RESOURCES.getStringProperty("synchronization.group.object.class", "group");
    }

    public static String getSynchronizationAccountNameAttribute() {
        return RESOURCES.getStringProperty("synchronization.account.name.attribute");
    }

    public static String getSynchronizationGroupNameAttribute() {
        return RESOURCES.getStringProperty("synchronization.group.name.attribute", getSynchronizationAccountNameAttribute());
    }

    public static String getSynchronizationUserFullNameAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.fullName.attribute");
    }

    public static String getSynchronizationUserDescriptionAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.description.attribute");
    }

    public static String getSynchronizationUserTitleAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.title.attribute");
    }

    public static String getSynchronizationUserEmailAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.email.attribute");
    }

    public static String getSynchronizationUserPhoneAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.phone.attribute");
    }

    public static String getSynchronizationUserDepartmentAttribute() {
        return RESOURCES.getStringProperty("synchronization.user.department.attribute");
    }

    public static String getSynchronizationGroupDescriptionAttribute() {
        return RESOURCES.getStringProperty("synchronization.group.description.attribute");
    }

    public static String getSynchronizationGroupMemberAttribute() {
        return RESOURCES.getStringProperty("synchronization.group.member.attribute");
    }

    public static boolean isSynchronizationEmptyAttributeEnabled() {
        return RESOURCES.getBooleanProperty("synchronization.empty.attribute.enabled", false);
    }

    public static boolean isSynchronizationUserStatusEnabled() {
        return RESOURCES.getBooleanProperty("synchronization.user.status.enabled", false);
    }

}
