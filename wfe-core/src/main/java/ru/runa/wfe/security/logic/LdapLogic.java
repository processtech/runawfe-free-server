package ru.runa.wfe.security.logic;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Imports users and group from LDAP directory.
 * 
 * @since 4.0.4
 */
@Component
@CommonsLog
@SuppressWarnings("unchecked")
public class LdapLogic {
    private static final String OBJECT_CLASS_USER_FILTER = MessageFormat.format(LdapProperties.getSynchronizationObjectClassFilter(),
            LdapProperties.getSynchronizationUserObjectClass());
    private static final String OBJECT_CLASS_GROUP_FILTER = MessageFormat.format(LdapProperties.getSynchronizationObjectClassFilter(),
            LdapProperties.getSynchronizationGroupObjectClass());

    private static final String ATTR_ACCOUNT_NAME = LdapProperties.getSynchronizationAccountNameAttribute();
    private static final String ATTR_GROUP_NAME = LdapProperties.getSynchronizationGroupNameAttribute();
    private static final String ATTR_GROUP_MEMBER = LdapProperties.getSynchronizationGroupMemberAttribute();
    private static int pageSize = LdapProperties.getLdapPageSize();
    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    private Pattern patternForMissedPeople;
    private Group importGroup = null;
    private Group wasteGroup = null;

    public synchronized int synchronizeExecutors() {
        if (!LdapProperties.isSynchronizationEnabled()) {
            log.debug("Synchronization is disabled");
            return -1;
        }
        log.info("Synchronizing executors");
        try {
            importGroup = loadGroup(
                    new Group(LdapProperties.getSynchronizationImportGroupName(), LdapProperties.getSynchronizationImportGroupDescription()));
            wasteGroup = loadGroup(
                    new Group(LdapProperties.getSynchronizationWasteGroupName(), LdapProperties.getSynchronizationWasteGroupDescription()));
            LdapContext ldapContext = getContext();
            Map<String, Actor> actorsByDistinguishedName = Maps.newHashMap();
            int changesCount = synchronizeActors(ldapContext, actorsByDistinguishedName);
            changesCount += synchronizeGroups(ldapContext, actorsByDistinguishedName);
            ldapContext.close();
            return changesCount;
        } catch (Exception e) {
            log.error("", e);
            // prevent java.io.NotSerializableException: com.sun.jndi.ldap.LdapCtx
            throw new InternalApplicationException(e.getMessage());
        }
    }

    private int synchronizeActors(LdapContext ldapContext, Map<String, Actor> actorsByDistinguishedName) throws Exception {
        int changesCount = 0;
        List<Actor> existingActorsList = executorDao.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        Map<String, Actor> existingActorsMap = Maps.newHashMap();
        for (Actor actor : existingActorsList) {
            existingActorsMap.put(actor.getName().toLowerCase(), actor);
        }
        Set<Actor> ldapActorsToDelete = Sets.newHashSet();
        if (LdapProperties.isSynchronizationDeleteExecutors()) {
            ldapActorsToDelete.addAll(executorDao.getGroupActors(importGroup));
        }
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        for (String ou : LdapProperties.getSynchronizationOrganizationUnits()) {
            List<SearchResult> resultList = Lists.newArrayList();
            byte[] cookie = null;
            ldapContext.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
            do {
                NamingEnumeration<SearchResult> results = ldapContext.search(ou, OBJECT_CLASS_USER_FILTER, searchControls);
                while (results.hasMoreElements()) {
                    SearchResult result = results.nextElement();
                    resultList.add(result);
                }
                results.close();
                Control[] controls = ldapContext.getResponseControls();
                if (controls != null) {
                    for (Control control : controls) {
                        if (control instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl response = (PagedResultsResponseControl) control;
                            cookie = response.getCookie();
                        }
                    }
                } else {
                    log.warn("Ldap server did not send controls for paging");
                }
                ldapContext.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
            } while (cookie != null);
            for (SearchResult searchResult : resultList) {
                String name = getStringAttribute(searchResult, ATTR_ACCOUNT_NAME);
                String description = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserDescriptionAttribute());
                String fullName = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserFullNameAttribute());
                String email = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserEmailAttribute());
                String phone = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserPhoneAttribute());
                String title = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserTitleAttribute());
                String department = getStringAttribute(searchResult, LdapProperties.getSynchronizationUserDepartmentAttribute());
                ToStringHelper toStringHelper = MoreObjects.toStringHelper("user info");
                toStringHelper.add("name", name).add("description", description).add("fullName", fullName).add("email", email);
                toStringHelper.add("phone", phone).add("title", title).add("department", department).omitNullValues();
                log.debug("Read " + toStringHelper.toString());
                Actor actor = existingActorsMap.get(name.toLowerCase());
                if (actor == null) {
                    if (!LdapProperties.isSynchronizationCreateExecutors()) {
                        continue;
                    }
                    actor = new Actor(name, description, fullName, null, email, phone, title, department);
                    log.info("Creating " + actor);
                    executorDao.create(actor);
                    executorDao.addExecutorsToGroup(Lists.newArrayList(actor), importGroup);
                    permissionDao.setPermissions(importGroup, Lists.newArrayList(Permission.READ), actor);
                    changesCount++;
                } else {
                    ldapActorsToDelete.remove(actor);
                    if (LdapProperties.isSynchronizationUpdateExecutors()) {
                        List<IChange> changes = Lists.newArrayList();
                        if (isAttributeNeedsChange(description, actor.getDescription())) {
                            changes.add(new AttributeChange("description", actor.getDescription(), description));
                            actor.setDescription(description);
                        }
                        if (isAttributeNeedsChange(fullName, actor.getFullName())) {
                            changes.add(new AttributeChange("fullName", actor.getFullName(), fullName));
                            actor.setFullName(fullName);
                        }
                        if (isAttributeNeedsChange(email, actor.getEmail())) {
                            changes.add(new AttributeChange("email", actor.getEmail(), email));
                            actor.setEmail(email);
                        }
                        if (isAttributeNeedsChange(phone, actor.getPhone())) {
                            changes.add(new AttributeChange("phone", actor.getPhone(), phone));
                            actor.setPhone(phone);
                        }
                        if (isAttributeNeedsChange(title, actor.getTitle())) {
                            changes.add(new AttributeChange("title", actor.getTitle(), title));
                            actor.setTitle(title);
                        }
                        if (isAttributeNeedsChange(department, actor.getDepartment())) {
                            changes.add(new AttributeChange("department", actor.getDepartment(), department));
                            actor.setDepartment(department);
                        }
                        if (!actor.isActive()) {
                            if (LdapProperties.isSynchronizationUserStatusEnabled()) {
                                actor.setActive(true);
                                changes.add(new AttributeChange("active", "false", "true"));
                            }
                            if (executorDao.removeExecutorFromGroup(actor, wasteGroup)) {
                                changes.add(new Change("waste group removal"));
                            }
                            if (executorDao.addExecutorToGroup(actor, importGroup)) {
                                changes.add(new Change("import group addition"));
                            }
                        }
                        if (!changes.isEmpty()) {
                            executorDao.update(actor);
                            log.info("Updating " + actor + ": " + changes);
                            changesCount++;
                        }
                    }
                }
                actorsByDistinguishedName.put(searchResult.getNameInNamespace(), actor);
            }
        }
        if (LdapProperties.isSynchronizationDeleteExecutors() && ldapActorsToDelete.size() > 0) {
            if (LdapProperties.isSynchronizationUserStatusEnabled()) {
                for (Actor actor : ldapActorsToDelete) {
                    actor.setActive(false);
                    executorDao.update(actor);
                    log.info("Inactivating " + actor);
                    changesCount++;
                }
            }
            executorDao.removeExecutorsFromGroup(ldapActorsToDelete, importGroup);
            executorDao.addExecutorsToGroup(ldapActorsToDelete, wasteGroup);
            changesCount += ldapActorsToDelete.size();
        }
        return changesCount;
    }

    private int synchronizeGroups(LdapContext ldapContext, Map<String, Actor> actorsByDistinguishedName) throws Exception {
        int changesCount = 0;
        List<Group> existingGroupsList = executorDao.getAllGroups();
        Map<String, Group> existingGroupsByLdapNameMap = Maps.newHashMap();
        for (Group group : existingGroupsList) {
            if (!Strings.isNullOrEmpty(group.getLdapGroupName())) {
                existingGroupsByLdapNameMap.put(group.getLdapGroupName(), group);
            }
        }
        Set<Group> ldapGroupsToDelete = Sets.newHashSet();
        if (LdapProperties.isSynchronizationDeleteExecutors()) {
            Set<Executor> ldapExecutors = executorDao.getGroupChildren(importGroup);
            for (Executor executor : ldapExecutors) {
                if (executor instanceof Group) {
                    ldapGroupsToDelete.add((Group) executor);
                }
            }
        }
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Map<String, SearchResult> groupResultsByDistinguishedName = Maps.newHashMap();
        for (String ou : LdapProperties.getSynchronizationOrganizationUnits()) {
            byte[] cookie = null;
            ldapContext.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
            do {
                NamingEnumeration<SearchResult> results = ldapContext.search(ou, OBJECT_CLASS_GROUP_FILTER, searchControls);
                while (results.hasMoreElements()) {
                    SearchResult searchResult = results.nextElement();
                    if (searchResult.getAttributes().get(ATTR_GROUP_MEMBER) == null) {
                        continue;
                    }
                    groupResultsByDistinguishedName.put(searchResult.getNameInNamespace(), searchResult);
                }
                results.close();
                Control[] controls = ldapContext.getResponseControls();
                if (controls != null) {
                    for (Control control : controls) {
                        if (control instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl response = (PagedResultsResponseControl) control;
                            cookie = response.getCookie();
                        }
                    }
                } else {
                    log.warn("Ldap server did not send controls for paging");
                }
                ldapContext.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
            } while (cookie != null);
        }
        for (SearchResult searchResult : groupResultsByDistinguishedName.values()) {
            String name = getStringAttribute(searchResult, ATTR_GROUP_NAME);
            String description = getStringAttribute(searchResult, LdapProperties.getSynchronizationGroupDescriptionAttribute());
            ToStringHelper toStringHelper = MoreObjects.toStringHelper("group info");
            toStringHelper.add("name", name).add("description", description).omitNullValues();
            log.debug("Read " + toStringHelper.toString());
            Group group = existingGroupsByLdapNameMap.get(name);
            if (group == null) {
                if (!LdapProperties.isSynchronizationCreateExecutors()) {
                    continue;
                }
                group = new Group(name, description);
                group.setLdapGroupName(name);
                log.info("Creating " + group);
                executorDao.create(group);
                executorDao.addExecutorsToGroup(Lists.newArrayList(group), importGroup);
                permissionDao.setPermissions(importGroup, Lists.newArrayList(Permission.READ), group);
                changesCount++;
            } else {
                ldapGroupsToDelete.remove(group);
                if (LdapProperties.isSynchronizationUpdateExecutors()) {
                    List<IChange> changes = Lists.newArrayList();
                    if (isAttributeNeedsChange(description, group.getDescription())) {
                        changes.add(new AttributeChange("description", group.getDescription(), description));
                        group.setDescription(description);
                        executorDao.update(group);
                    }
                    if (executorDao.removeExecutorFromGroup(group, wasteGroup)) {
                        changes.add(new Change("waste group removal"));
                    }
                    if (executorDao.addExecutorToGroup(group, importGroup)) {
                        changes.add(new Change("import group addition"));
                    }
                    if (!changes.isEmpty()) {
                        log.info("Updating " + group + ": " + changes);
                        changesCount++;
                    }
                }
            }

            Set<Actor> actorsToDelete = Sets.newHashSet(executorDao.getGroupActors(group));
            Set<Actor> actorsToAdd = Sets.newHashSet();
            Set<Actor> groupTargetActors = Sets.newHashSet();
            fillTargetActorsRecursively(ldapContext, groupTargetActors, searchResult, groupResultsByDistinguishedName, actorsByDistinguishedName);
            for (Actor targetActor : groupTargetActors) {
                if (!actorsToDelete.remove(targetActor)) {
                    actorsToAdd.add(targetActor);
                }
            }
            if (actorsToAdd.size() > 0) {
                log.info("Adding to " + group + ": " + actorsToAdd);
                executorDao.addExecutorsToGroup(actorsToAdd, group);
                changesCount++;
            }
            if (actorsToDelete.size() > 0) {
                executorDao.removeExecutorsFromGroup(Lists.newArrayList(actorsToDelete), group);
                changesCount++;
            }
        }
        if (LdapProperties.isSynchronizationDeleteExecutors() && ldapGroupsToDelete.size() > 0) {
            executorDao.removeExecutorsFromGroup(ldapGroupsToDelete, importGroup);
            executorDao.addExecutorsToGroup(ldapGroupsToDelete, wasteGroup);
            log.info("Inactivating " + ldapGroupsToDelete);
            changesCount += ldapGroupsToDelete.size();
        }
        return changesCount;
    }

    private boolean isAttributeNeedsChange(String oldValue, String newValue) {
        if (LdapProperties.isSynchronizationEmptyAttributeEnabled() || !Strings.isNullOrEmpty(oldValue)) {
            return !Utils.stringsEqual(oldValue, newValue);
        }
        return false;
    }

    private Pattern getPatternForMissedPeople() {
        if (patternForMissedPeople == null) {
            String providerUrl = LdapProperties.getAllProperties().get(Context.PROVIDER_URL);
            String dc = providerUrl.substring(providerUrl.lastIndexOf("/") + 1);
            patternForMissedPeople = Pattern.compile("," + dc, Pattern.CASE_INSENSITIVE);
        }
        return patternForMissedPeople;
    }

    private LdapContext getContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>(LdapProperties.getAllProperties());
        return new InitialLdapContext(env, null);
    }

    private Group loadGroup(Group group) {
        if (executorDao.isExecutorExist(group.getName())) {
            group = executorDao.getGroup(group.getName());
        } else {
            group = executorDao.create(group);
            permissionDao.setPermissions(group, Lists.newArrayList(Permission.LOGIN), SecuredSingleton.SYSTEM);
        }
        return group;
    }

    private String getStringAttribute(SearchResult searchResult, String name) throws NamingException {
        if (Utils.isNullOrEmpty(name)) {
            return null;
        }
        Attribute attribute = searchResult.getAttributes().get(name);
        if (attribute != null) {
            return attribute.get().toString();
        }
        return null;
    }

    private void fillTargetActorsRecursively(LdapContext ldapContext, Set<Actor> recursiveActors, SearchResult searchResult,
            Map<String, SearchResult> groupResultsByDistinguishedName, Map<String, Actor> actorsByDistinguishedName) throws NamingException {
        NamingEnumeration<String> namingEnum = (NamingEnumeration<String>) searchResult.getAttributes().get(ATTR_GROUP_MEMBER).getAll();
        while (namingEnum.hasMore()) {
            String executorDistinguishedName = namingEnum.next();
            SearchResult groupSearchResult = groupResultsByDistinguishedName.get(executorDistinguishedName);
            if (groupSearchResult != null) {
                fillTargetActorsRecursively(ldapContext, recursiveActors, groupSearchResult, groupResultsByDistinguishedName,
                        actorsByDistinguishedName);
            } else {
                Actor actor = actorsByDistinguishedName.get(executorDistinguishedName);
                if (actor != null) {
                    recursiveActors.add(actor);
                } else {
                    Matcher m = getPatternForMissedPeople().matcher(executorDistinguishedName);
                    String executorPath = m.replaceAll("");
                    Attribute samAttribute = ldapContext.getAttributes(executorPath).get(ATTR_ACCOUNT_NAME);
                    if (samAttribute != null) {
                        String executorName = samAttribute.get().toString();
                        log.debug("Executor name " + executorDistinguishedName + " fetched by invocation: " + executorName);
                        try {
                            Executor executor = executorDao.getExecutor(executorName);
                            if (executor instanceof Actor) {
                                recursiveActors.add((Actor) executor);
                            }
                        } catch (ExecutorDoesNotExistException e) {
                            log.warn(e.getMessage() + " for '" + executorDistinguishedName + "'");
                        }
                    } else {
                        log.warn("Not found '" + executorDistinguishedName + "' neither in group or actor maps or by invocation");
                    }
                }
            }
        }
    }

    private interface IChange {

    }

    private static class Change implements IChange {
        final String message;

        public Change(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private static class AttributeChange implements IChange {
        final String attributeName;
        final String oldValue;
        final String newValue;

        AttributeChange(String attributeName, String oldValue, String newValue) {
            this.attributeName = attributeName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public String toString() {
            return attributeName + ": " + oldValue + " -> " + newValue;
        }
    }

}
