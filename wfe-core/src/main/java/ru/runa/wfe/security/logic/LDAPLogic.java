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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

/**
 * Imports users and group from LDAP directory.
 *
 * @since 4.0.4
 */
@SuppressWarnings("unchecked")
public class LDAPLogic extends TransactionalExecutor {
    // private static final String OBJECT_CLASS_ATTR_NAME = "objectClass";
    // private static final String OBJECT_CLASS_ATTR_USER_VALUE = "user";
    // private static final String OBJECT_CLASS_ATTR_GROUP_VALUE = "group";
    private static final String IMPORTED_FROM_LDAP_GROUP_NAME = "ldap users";
    private static final String IMPORTED_FROM_LDAP_GROUP_DESCRIPION = "users imported from ldap";
    private static final String DISPLAY_NAME = "name";
    private static final String SAM_ACCOUNT_NAME = "sAMAccountName";
    private static final String TITLE = "title";
    private static final String EMAIL = "mail";
    private static final String MEMBER = "member";
    private static final String PHONE = "telephoneNumber";
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

    boolean createExecutors;

    public void synchronizeExecutors(boolean inNewTransaction, boolean createExecutors) {
        // TODO avoid class member
        this.createExecutors = createExecutors;
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
        log.info("Synchronization mode: " + (createExecutors ? "full" : "user and group relations only"));
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
            Map<String, Actor> actorsByDistinguishedName = synchronizeActors(dirContext, wfeImportFromLdapGroup, createExecutors);
            synchronizeGroups(dirContext, wfeImportFromLdapGroup, actorsByDistinguishedName, createExecutors);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Map<String, Actor> synchronizeActors(DirContext dirContext, Group wfeImportFromLdapGroup, boolean createExecutors) throws Exception {
        List<Actor> existingActorsList = executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        Map<String, Actor> existingActorsMap = Maps.newHashMap();
        for (Actor actor : existingActorsList) {
            existingActorsMap.put(actor.getName().toLowerCase(), actor);
        }
        Map<String, Actor> actorsByDistinguishedName = Maps.newHashMap();
        // Attributes attributes = new BasicAttributes();
        // attributes.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        for (String ou : SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou")) {
            List<SearchResult> resultList = Lists.newArrayList();
            try {
                NamingEnumeration<SearchResult> list = dirContext.search(ou, "(objectclass=user)", controls);
                while (list.hasMore()) {
                    SearchResult searchResult = list.next();
                    resultList.add(searchResult);
                }
            } catch (SizeLimitExceededException e) {
                resultList.clear();
                for (String y : ALPHABETS) {
                    NamingEnumeration<SearchResult> list = dirContext.search(ou,
                            "(&(|(" + SAM_ACCOUNT_NAME + "=" + y + "*)(" + SAM_ACCOUNT_NAME + "=" + y.toLowerCase() + "*))(objectclass=user))",
                            controls);
                    while (list.hasMore()) {
                        SearchResult searchResult = list.next();
                        resultList.add(searchResult);
                    }
                }
            }
            for (SearchResult searchResult : resultList) {
                String name = getStringAttribute(searchResult, SAM_ACCOUNT_NAME);
                String fullName = getStringAttribute(searchResult, DISPLAY_NAME);
                String email = getStringAttribute(searchResult, EMAIL);
                String description = getStringAttribute(searchResult, TITLE);
                String phone = getStringAttribute(searchResult, PHONE);
                if (phone != null && phone.length() > 32) {
                    phone = phone.substring(0, 31);
                }
                Actor actor = existingActorsMap.get(name.toLowerCase());
                if (actor == null) {
                    if (!createExecutors) {
                        continue;
                    }
                    actor = new Actor(name, description, fullName, null, email, phone);
                    log.info("Importing " + actor);
                    executorDAO.create(actor);
                    executorDAO.addExecutorsToGroup(Lists.newArrayList(actor), wfeImportFromLdapGroup);
                    permissionDAO.setPermissions(wfeImportFromLdapGroup, Lists.newArrayList(Permission.READ), actor);
                }
                actorsByDistinguishedName.put(searchResult.getNameInNamespace(), actor);
            }
        }
        return actorsByDistinguishedName;
    }

    private String getStringAttribute(SearchResult searchResult, String name) throws NamingException {
        Attribute attribute = searchResult.getAttributes().get(name);
        if (attribute != null) {
            return attribute.get().toString();
        }
        return null;
    }

    private void synchronizeGroups(DirContext dirContext, Group wfeImportFromLdapGroup, Map<String, Actor> actorsByDistinguishedName,
            boolean createExecutors) throws NamingException {
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
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Map<String, SearchResult> groupResultsByDistinguishedName = Maps.newHashMap();
        for (String ou : SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou")) {
            NamingEnumeration<SearchResult> list = dirContext.search(ou, "(objectclass=group)", controls);
            while (list.hasMore()) {
                SearchResult searchResult = list.next();
                if (searchResult.getAttributes().get(MEMBER) == null) {
                    continue;
                }
                groupResultsByDistinguishedName.put(searchResult.getNameInNamespace(), searchResult);
            }
        }
        for (SearchResult searchResult : groupResultsByDistinguishedName.values()) {
            String name = getStringAttribute(searchResult, SAM_ACCOUNT_NAME);
            Group group = existingGroupsByLdapNameMap.get(name);
            if (group == null) {
                if (!createExecutors) {
                    continue;
                }
                group = new Group(name, getStringAttribute(searchResult, DISPLAY_NAME));
                group.setLdapGroupName(name);
                log.info("Importing " + group);
                executorDAO.create(group);
                executorDAO.addExecutorsToGroup(Lists.newArrayList(group), wfeImportFromLdapGroup);
                permissionDAO.setPermissions(wfeImportFromLdapGroup, Lists.newArrayList(Permission.READ), group);
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
    }

    private void fillTargetActorsRecursively(DirContext dirContext, Set<Actor> recursiveActors, SearchResult searchResult,
            Map<String, SearchResult> groupResultsByDistinguishedName, Map<String, Actor> actorsByDistinguishedName) throws NamingException {
        NamingEnumeration<String> namingEnum = (NamingEnumeration<String>) searchResult.getAttributes().get(MEMBER).getAll();
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
                    Attribute samAttribute = dirContext.getAttributes(executorPath).get(SAM_ACCOUNT_NAME);
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
