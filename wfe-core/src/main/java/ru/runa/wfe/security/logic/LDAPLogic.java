/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.security.logic;

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
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Imports users and group from LDAP directory.
 * 
 * @since 4.0.4
 */
@SuppressWarnings("unchecked")
public class LDAPLogic extends TransactionalExecutor {

    private static final String OBJECT_CLASS_USER = "user";
    private static final String OBJECT_CLASS_GROUP = "group";

    private static final String OBJECT_CLASS_FILTER = "(objectclass={0})";
    private static final String OBJECT_CLASS_USER_FILTER = MessageFormat.format(OBJECT_CLASS_FILTER, OBJECT_CLASS_USER);
    private static final String OBJECT_CLASS_GROUP_FILTER = MessageFormat.format(OBJECT_CLASS_FILTER, OBJECT_CLASS_GROUP);

    private static final String LOGIN_FIRST_LETTER_FILTER = "(&(|({0}={1}*)({0}={2}*)){3})";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_SAM_ACCOUNT_NAME = "sAMAccountName"; // domain account (login/username) for Win NT/98/earlier
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_EMAIL = "mail";
    private static final String ATTR_MEMBER = "member";
    private static final String ATTR_PHONE = "telephoneNumber";
    private static final String ATTR_DEPARTMENT = "department";

    private static final String IMPORTED_FROM_LDAP_GROUP_NAME = "ldap users";
    private static final String IMPORTED_FROM_LDAP_GROUP_DESCRIPION = "users imported from ldap server";
    private static final String DELETED_FROM_LDAP_GROUP_NAME = "ldap waste";
    private static final String DELETED_FROM_LDAP_GROUP_DESCRIPION = "users and groups deleted from ldap server";
    private static final String[] ALPHABETS = { "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У",
            "Ф", "Х", "Ч", "Ц", "Ш", "Щ", "Э", "Ю", "Я", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z" };

    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;

    private Pattern patternForMissedPeople;

    private Pattern getPatternForMissedPeople() {
        if (patternForMissedPeople == null) {
            String providerUrl = SystemProperties.getResources().getStringProperty("ldap.connection.provider.url");
            String dc = providerUrl.substring(providerUrl.lastIndexOf("/") + 1);
            patternForMissedPeople = Pattern.compile("," + dc, Pattern.CASE_INSENSITIVE);
        }
        return patternForMissedPeople;
    }

    private DirContext getContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                SystemProperties.getResources().getStringProperty("ldap.context.factory", "com.sun.jndi.ldap.LdapCtxFactory"));
        env.put(Context.PROVIDER_URL, SystemProperties.getResources().getStringProperty("ldap.connection.provider.url"));
        env.put(Context.SECURITY_AUTHENTICATION, SystemProperties.getResources().getStringProperty("ldap.connection.authentication", "simple"));
        env.put(Context.SECURITY_PRINCIPAL, SystemProperties.getResources().getStringPropertyNotNull("ldap.connection.principal"));
        env.put(Context.SECURITY_CREDENTIALS, SystemProperties.getResources().getStringPropertyNotNull("ldap.connection.password"));
        env.put(Context.REFERRAL, SystemProperties.getResources().getStringProperty("ldap.connection.referral", "follow"));
        env.put("java.naming.ldap.version", SystemProperties.getResources().getStringProperty("ldap.connection.version", "3"));
        return new InitialDirContext(env);
    }

    private boolean createExecutors;
    private boolean updateExecutors;
    private boolean deleteExecutors;

    public void synchronizeExecutors(boolean inNewTransaction, boolean createExecutors, boolean updateExecutors, boolean deleteExecutors) {
        // TODO avoid class member
        this.createExecutors = createExecutors;
        this.updateExecutors = updateExecutors;
        this.deleteExecutors = deleteExecutors;
        if (inNewTransaction) {
            executeInTransaction(false);
        } else {
            doExecuteInTransaction();
        }
    }

    @Override
    protected void doExecuteInTransaction() {
        if (!SystemProperties.isLDAPSynchronizationEnabled()) {
            log.debug("Synchronization is disabled");
            return;
        }
        Preconditions.checkNotNull(SystemProperties.getResources().getStringProperty("ldap.connection.provider.url"),
                "LDAP property is not configured 'ldap.connection.provider.url'");
        Preconditions.checkNotNull(SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou"),
                "LDAP property is not configured 'ldap.synchronizer.ou'");
        log.info("Synchronization mode: " + (createExecutors ? "creation " : "") + (updateExecutors ? "modification " : "")
                + (deleteExecutors ? "deletion" : ""));
        try {
            Group wfeImportFromLdapGroup = new Group(IMPORTED_FROM_LDAP_GROUP_NAME, IMPORTED_FROM_LDAP_GROUP_DESCRIPION);
            if (!executorDAO.isExecutorExist(wfeImportFromLdapGroup.getName())) {
                wfeImportFromLdapGroup = executorDAO.create(wfeImportFromLdapGroup);
                permissionDAO.setPermissions(wfeImportFromLdapGroup, Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM),
                        ASystem.INSTANCE);
            } else {
                wfeImportFromLdapGroup = executorDAO.getGroup(wfeImportFromLdapGroup.getName());
            }
            DirContext dirContext = getContext();
            Map<String, Actor> actorsByDistinguishedName = synchronizeActors(dirContext, wfeImportFromLdapGroup);
            synchronizeGroups(dirContext, wfeImportFromLdapGroup, actorsByDistinguishedName);
            dirContext.close();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Map<String, Actor> synchronizeActors(DirContext dirContext, Group wfeImportFromLdapGroup) throws Exception {
        List<Actor> existingActorsList = executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        Map<String, Actor> existingActorsMap = Maps.newHashMap();
        for (Actor actor : existingActorsList) {
            existingActorsMap.put(actor.getName().toLowerCase(), actor);
        }
        Set<Actor> ldapActorsToDelete = Sets.newHashSet();
        if (deleteExecutors) {
            ldapActorsToDelete.addAll(executorDAO.getGroupActors(wfeImportFromLdapGroup));
        }
        Map<String, Actor> actorsByDistinguishedName = Maps.newHashMap();
        // Attributes attributes = new BasicAttributes();
        // attributes.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        for (String ou : SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou")) {
            List<SearchResult> resultList = Lists.newArrayList();
            try {
                NamingEnumeration<SearchResult> list = dirContext.search(ou, OBJECT_CLASS_USER_FILTER, controls);
                while (list.hasMore()) {
                    SearchResult searchResult = list.next();
                    resultList.add(searchResult);
                }
                list.close();
            } catch (SizeLimitExceededException e) {
                resultList.clear();
                for (String y : ALPHABETS) {
                    NamingEnumeration<SearchResult> list = dirContext.search(ou,
                            MessageFormat.format(LOGIN_FIRST_LETTER_FILTER, ATTR_SAM_ACCOUNT_NAME, y, y.toLowerCase(), OBJECT_CLASS_USER_FILTER),
                            controls);
                    while (list.hasMore()) {
                        SearchResult searchResult = list.next();
                        resultList.add(searchResult);
                    }
                    list.close();
                }
            }
            for (SearchResult searchResult : resultList) {
                String name = getStringAttribute(searchResult, ATTR_SAM_ACCOUNT_NAME);
                String fullName = getStringAttribute(searchResult, ATTR_NAME);
                String email = getStringAttribute(searchResult, ATTR_EMAIL);
                String department = getStringAttribute(searchResult, ATTR_DEPARTMENT);
                String title = getStringAttribute(searchResult, ATTR_TITLE);
                String description = title; // getStringAttribute(searchResult, DESCRIPTION);
                String phone = getStringAttribute(searchResult, ATTR_PHONE);
                if (phone != null && phone.length() > 32) {
                    phone = phone.substring(0, 31);
                }
                Actor actor = existingActorsMap.get(name.toLowerCase());
                if (actor == null) {
                    if (!createExecutors) {
                        continue;
                    }
                    actor = new Actor(name, description, fullName, null, email, phone, title, department);
                    log.info("Importing " + actor);
                    executorDAO.create(actor);
                    executorDAO.addExecutorsToGroup(Lists.newArrayList(actor), wfeImportFromLdapGroup);
                    permissionDAO.setPermissions(wfeImportFromLdapGroup, Lists.newArrayList(Permission.READ), actor);
                } else {
                    ldapActorsToDelete.remove(actor);
                    if (updateExecutors) {
                        actor.setDescription(description);
                        actor.setFullName(fullName);
                        actor.setEmail(email);
                        actor.setPhone(phone);
                        actor.setActive(true);
                        executorDAO.update(actor);
                        executorDAO.removeExecutorFromGroup(actor, getLdapWasteGroup());
                        executorDAO.addExecutorToGroup(actor, wfeImportFromLdapGroup);
                    }
                }
                actorsByDistinguishedName.put(searchResult.getNameInNamespace(), actor);
            }
        }
        if (deleteExecutors && ldapActorsToDelete.size() > 0) {
            for (Actor body : ldapActorsToDelete) {
                body.setActive(false);
                executorDAO.update(body);
            }
            executorDAO.removeExecutorsFromGroup(ldapActorsToDelete, wfeImportFromLdapGroup);
            executorDAO.addExecutorsToGroup(ldapActorsToDelete, getLdapWasteGroup());
        }
        return actorsByDistinguishedName;
    }

    private Group ldapWasteGroup = null;

    private Group getLdapWasteGroup() {
        if (ldapWasteGroup == null) {
            if (executorDAO.isExecutorExist(DELETED_FROM_LDAP_GROUP_NAME)) {
                ldapWasteGroup = executorDAO.getGroup(DELETED_FROM_LDAP_GROUP_NAME);
            } else {
                ldapWasteGroup = executorDAO.create(new Group(DELETED_FROM_LDAP_GROUP_NAME, DELETED_FROM_LDAP_GROUP_DESCRIPION));
                permissionDAO.setPermissions(ldapWasteGroup, Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM), ASystem.INSTANCE);
            }
        }
        return ldapWasteGroup;
    }

    private String getStringAttribute(SearchResult searchResult, String name) throws NamingException {
        Attribute attribute = searchResult.getAttributes().get(name);
        if (attribute != null) {
            return attribute.get().toString();
        }
        return null;
    }

    private void synchronizeGroups(DirContext dirContext, Group wfeImportFromLdapGroup, Map<String, Actor> actorsByDistinguishedName)
            throws NamingException {
        // Attributes attributes = new BasicAttributes();
        // attributes.put(OBJECT_CLASS_ATTR_NAME,
        // OBJECT_CLASS_ATTR_GROUP_VALUE);
        List<Group> existingGroupsList = executorDAO.getAllGroups();
        Map<String, Group> existingGroupsByLdapNameMap = Maps.newHashMap();
        for (Group group : existingGroupsList) {
            if (!Strings.isNullOrEmpty(group.getLdapGroupName())) {
                existingGroupsByLdapNameMap.put(group.getLdapGroupName(), group);
            }
        }
        Set<Group> ldapGroupsToDelete = Sets.newHashSet();
        if (deleteExecutors) {
            Set<Executor> ldapExecutors = executorDAO.getGroupChildren(wfeImportFromLdapGroup);
            for (Executor e : ldapExecutors) {
                if (e instanceof Group) {
                    ldapGroupsToDelete.add((Group) e);
                }
            }
        }
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Map<String, SearchResult> groupResultsByDistinguishedName = Maps.newHashMap();
        for (String ou : SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou")) {
            NamingEnumeration<SearchResult> list = dirContext.search(ou, OBJECT_CLASS_GROUP_FILTER, controls);
            while (list.hasMore()) {
                SearchResult searchResult = list.next();
                if (searchResult.getAttributes().get(ATTR_MEMBER) == null) {
                    continue;
                }
                groupResultsByDistinguishedName.put(searchResult.getNameInNamespace(), searchResult);
            }
        }
        for (SearchResult searchResult : groupResultsByDistinguishedName.values()) {
            String name = getStringAttribute(searchResult, ATTR_SAM_ACCOUNT_NAME);
            Group group = existingGroupsByLdapNameMap.get(name);
            if (group == null) {
                if (!createExecutors) {
                    continue;
                }
                group = new Group(name, getStringAttribute(searchResult, ATTR_NAME));
                group.setLdapGroupName(name);
                log.info("Importing " + group);
                executorDAO.create(group);
                executorDAO.addExecutorsToGroup(Lists.newArrayList(group), wfeImportFromLdapGroup);
                permissionDAO.setPermissions(wfeImportFromLdapGroup, Lists.newArrayList(Permission.READ), group);
            } else {
                ldapGroupsToDelete.remove(group);
                if (updateExecutors) {
                    group.setDescription(getStringAttribute(searchResult, ATTR_NAME));
                    executorDAO.update(group);
                    executorDAO.removeExecutorFromGroup(group, getLdapWasteGroup());
                    executorDAO.addExecutorToGroup(group, wfeImportFromLdapGroup);
                }
            }

            Set<Actor> actorsToDelete = Sets.newHashSet(executorDAO.getGroupActors(group));
            Set<Actor> actorsToAdd = Sets.newHashSet();
            Set<Actor> groupTargetActors = Sets.newHashSet();
            fillTargetActorsRecursively(dirContext, groupTargetActors, searchResult, groupResultsByDistinguishedName, actorsByDistinguishedName);
            for (Actor targetActor : groupTargetActors) {
                if (!actorsToDelete.remove(targetActor)) {
                    actorsToAdd.add(targetActor);
                }
            }
            if (actorsToAdd.size() > 0) {
                log.info("Adding to " + group + ": " + actorsToAdd);
                executorDAO.addExecutorsToGroup(actorsToAdd, group);
            }
            if (actorsToDelete.size() > 0) {
                executorDAO.removeExecutorsFromGroup(Lists.newArrayList(actorsToDelete), group);
            }
        }
        if (deleteExecutors && ldapGroupsToDelete.size() > 0) {
            executorDAO.removeExecutorsFromGroup(ldapGroupsToDelete, wfeImportFromLdapGroup);
            executorDAO.addExecutorsToGroup(ldapGroupsToDelete, getLdapWasteGroup());
        }
    }

    private void fillTargetActorsRecursively(DirContext dirContext, Set<Actor> recursiveActors, SearchResult searchResult,
            Map<String, SearchResult> groupResultsByDistinguishedName, Map<String, Actor> actorsByDistinguishedName) throws NamingException {
        NamingEnumeration<String> namingEnum = (NamingEnumeration<String>) searchResult.getAttributes().get(ATTR_MEMBER).getAll();
        while (namingEnum.hasMore()) {
            String executorDistinguishedName = namingEnum.next();
            SearchResult groupSearchResult = groupResultsByDistinguishedName.get(executorDistinguishedName);
            if (groupSearchResult != null) {
                fillTargetActorsRecursively(dirContext, recursiveActors, groupSearchResult, groupResultsByDistinguishedName,
                        actorsByDistinguishedName);
            } else {
                Actor actor = actorsByDistinguishedName.get(executorDistinguishedName);
                if (actor != null) {
                    recursiveActors.add(actor);
                } else {
                    Matcher m = getPatternForMissedPeople().matcher(executorDistinguishedName);
                    String executorPath = m.replaceAll("");
                    Attribute samAttribute = dirContext.getAttributes(executorPath).get(ATTR_SAM_ACCOUNT_NAME);
                    if (samAttribute != null) {
                        String executorName = samAttribute.get().toString();
                        log.debug("Executor name " + executorDistinguishedName + " fetched by invocation: " + executorName);
                        try {
                            Executor executor = executorDAO.getExecutor(executorName);
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

}
