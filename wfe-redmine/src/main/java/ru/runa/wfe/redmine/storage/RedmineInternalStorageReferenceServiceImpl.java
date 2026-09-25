package ru.runa.wfe.redmine.storage;

import com.google.common.base.Strings;
import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.springframework.stereotype.Service;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.condition.ConditionProcessor;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.RedmineDataSource;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.VariableStorageKind;
import ru.runa.wfe.var.logic.InternalStorageReferenceService;

@CommonsLog
@Service
public class RedmineInternalStorageReferenceServiceImpl implements InternalStorageReferenceService {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final RedmineFieldMapper mapper = new RedmineFieldMapper();
    private volatile RedmineManager redmineManager;
    private volatile Map<String, Integer> customFieldIdByName;

    @Override
    public VariableStorageKind getKind() {
        return VariableStorageKind.REDMINE;
    }

    @PreDestroy
    public void shutdown() {
        redmineManager = null;
        customFieldIdByName = null;
    }

    @Override
    public UserTypeMap loadById(UserType userType, Long id) {
        if (id == null) {
            return null;
        }
        try {
            Issue issue = issueManager().getIssueById(id.intValue(), Include.relations);
            return mapper.toUserTypeMap(userType, issue);
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: error loading issue id=" + id + " for type " + userType.getName(), e);
        }
    }

    @Override
    public long insert(UserType userType, UserTypeMap value) {
        String projectIdentifier = getProjectIdentifier();
        Integer projectId = resolveProjectId(projectIdentifier);
        Issue issue = IssueFactory.create(projectId, "");
        attachCustomFields(userType, issue);
        mapper.applyToIssue(userType, value, issue);
        applyMandatoryDefaults(userType, projectId, issue);
        try {
            Issue created = issueManager().createIssue(issue);
            log.info("Redmine: inserted issue id=" + created.getId() + " for type " + userType.getName());
            return created.getId().longValue();
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: error inserting issue for type " + userType.getName()
                    + ", project=" + projectIdentifier, e);
        }
    }

    @Override
    public void update(UserType userType, Long id, UserTypeMap value) {
        if (id == null) {
            throw new InternalApplicationException("Redmine: cannot update without id for type " + userType.getName());
        }
        try {
            Issue issue = issueManager().getIssueById(id.intValue(), Include.relations);
            if (issue == null) {
                throw new InternalApplicationException("Redmine: issue id=" + id + " not found for type " + userType.getName());
            }
            attachCustomFields(userType, issue);
            mapper.applyToIssue(userType, value, issue);
            try {
                issueManager().update(issue);
            } catch (IllegalArgumentException e) {
                if (!isEmptyBodyDecoderBug(e)) {
                    throw e;
                }
            }
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: error updating issue id=" + id + " for type " + userType.getName(), e);
        }
    }

    @Override
    public void delete(UserType userType, Long id) {
        if (id == null) {
            return;
        }
        try {
            issueManager().deleteIssue(id.intValue());
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: error deleting issue id=" + id + " for type " + userType.getName(), e);
        }
    }

    @Override
    public List<UserTypeMap> findByFilter(UserType userType, String condition, VariableProvider variableProvider) {
        String projectIdentifier = getProjectIdentifier();
        try {
            List<Issue> issues = issueManager().getIssues(projectIdentifier, null);
            List<UserTypeMap> result = new ArrayList<>(issues.size());
            boolean hasCondition = !Strings.isNullOrEmpty(condition);
            for (Issue issue : issues) {
                UserTypeMap map = mapper.toUserTypeMap(userType, issue);
                if (!hasCondition || ConditionProcessor.filter(condition, map, variableProvider)) {
                    result.add(map);
                }
            }
            log.debug("Redmine: findByFilter type=" + userType.getName() + ", project=" + projectIdentifier
                    + ", condition='" + condition + "', found=" + result.size());
            return result;
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: error in findByFilter for project=" + projectIdentifier, e);
        }
    }

    private IssueManager issueManager() {
        return ensureManager().getIssueManager();
    }

    private RedmineManager ensureManager() {
        RedmineManager local = redmineManager;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            local = redmineManager;
            if (local != null) {
                return local;
            }
            String url = RedmineProperties.getRedmineUrl();
            String apiKey = RedmineProperties.getRedmineApiKey();
            if (Strings.isNullOrEmpty(url) || Strings.isNullOrEmpty(apiKey)) {
                throw new InternalApplicationException("Redmine: 'redmine.url' or 'redmine.apiKey' is not configured "
                        + "(checked redmine.properties and REDMINE_URL/REDMINE_API_KEY env)");
            }
            HttpClient httpClient = RedmineManagerFactory.createDefaultHttpClient(url);
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MS);
            HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT_MS);
            local = RedmineManagerFactory.createWithApiKey(url, apiKey, httpClient);
            redmineManager = local;
            return local;
        }
    }

    private String getProjectIdentifier() {
        ru.runa.wfe.datasource.DataSource ds = DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
        if (!(ds instanceof RedmineDataSource)) {
            String actualType = ds == null ? "null" : ds.getClass().getSimpleName();
            throw new InternalApplicationException("Redmine: at least one user type is declared with storageType=REDMINE"
                    + " but the configured datasource '" + DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME
                    + "' is " + actualType
                    + ". Reconfigure the datasource to RedmineDataSource, or change the user type's referenceStorage attribute.");
        }
        String identifier = ((RedmineDataSource) ds).getProjectIdentifier();
        if (Strings.isNullOrEmpty(identifier)) {
            throw new InternalApplicationException("Redmine: RedmineDataSource '"
                    + DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME + "' has empty 'projectIdentifier'");
        }
        return identifier;
    }

    private Integer resolveProjectId(String projectIdentifier) {
        try {
            return ensureManager().getProjectManager().getProjectByKey(projectIdentifier).getId();
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: cannot resolve project '" + projectIdentifier + "'", e);
        }
    }

    private void attachCustomFields(UserType userType, Issue issue) {
        Map<String, Integer> defs = customFieldDefinitions(false);
        for (VariableDefinition attr : userType.getAttributes()) {
            String fieldName = RedmineFieldMapper.resolveFieldName(attr);
            if (Strings.isNullOrEmpty(fieldName) || RedmineFieldMapper.isBuiltinField(fieldName)) {
                continue;
            }
            if (issue.getCustomFieldByName(fieldName) != null) {
                continue;
            }
            Integer id = defs.get(fieldName);
            if (id == null) {
                defs = customFieldDefinitions(true);
                id = defs.get(fieldName);
            }
            if (id == null) {
                throw new InternalApplicationException("Redmine: custom field '" + fieldName
                        + "' is not defined in Redmine (UserType '" + userType.getName() + "', attribute '" + attr.getName() + "')");
            }
            issue.addCustomField(CustomFieldFactory.create(id, fieldName, null));
        }
    }

    private void applyMandatoryDefaults(UserType userType, Integer projectId, Issue issue) {
        if (issue.getProjectId() == null) {
            issue.setProjectId(projectId);
        }
        if (Strings.isNullOrEmpty(issue.getSubject())) {
            issue.setSubject(userType.getName());
        }
        if (issue.getTracker() == null) {
            Tracker tracker = firstTracker();
            if (tracker == null) {
                throw new InternalApplicationException("Redmine: tracker is not set on the issue and no trackers are available"
                        + " — declare 'tracker_id' attribute on UserType '" + userType.getName() + "'"
                        + " (or configure at least one tracker in Redmine)");
            }
            issue.setTracker(tracker);
        }
        if (issue.getStatusId() == null) {
            Integer statusId = firstStatusId();
            if (statusId == null) {
                throw new InternalApplicationException("Redmine: status is not set on the issue and no statuses are available"
                        + " — declare 'status_id' attribute on UserType '" + userType.getName() + "'"
                        + " (or configure at least one issue status in Redmine)");
            }
            issue.setStatusId(statusId);
        }
        log.info("Redmine: prepared issue project=" + issue.getProjectId()
                + ", subject='" + issue.getSubject() + "'"
                + ", trackerId=" + (issue.getTracker() != null ? issue.getTracker().getId() : null)
                + ", statusId=" + issue.getStatusId()
                + ", customFields=" + issue.getCustomFields().size());
    }

    private Tracker firstTracker() {
        try {
            List<Tracker> trackers = issueManager().getTrackers();
            return trackers.isEmpty() ? null : trackers.get(0);
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: cannot load trackers", e);
        }
    }

    private Integer firstStatusId() {
        try {
            List<IssueStatus> statuses = issueManager().getStatuses();
            return statuses.isEmpty() ? null : statuses.get(0).getId();
        } catch (RedmineException e) {
            throw new InternalApplicationException("Redmine: cannot load issue statuses", e);
        }
    }

    private Map<String, Integer> customFieldDefinitions(boolean forceRefresh) {
        Map<String, Integer> local = customFieldIdByName;
        if (local != null && !forceRefresh) {
            return local;
        }
        synchronized (this) {
            if (customFieldIdByName != null && !forceRefresh) {
                return customFieldIdByName;
            }
            try {
                List<CustomFieldDefinition> definitions = ensureManager().getCustomFieldManager().getCustomFieldDefinitions();
                Map<String, Integer> result = new HashMap<>();
                for (CustomFieldDefinition def : definitions) {
                    if ("issue".equalsIgnoreCase(def.getCustomizedType())) {
                        result.put(def.getName(), def.getId());
                    }
                }
                customFieldIdByName = result;
                return result;
            } catch (RedmineException e) {
                throw new InternalApplicationException("Redmine: cannot load custom field definitions", e);
            }
        }
    }

    private static boolean isEmptyBodyDecoderBug(IllegalArgumentException e) {
        if (!"Entity may not be null".equals(e.getMessage())) {
            return false;
        }
        for (StackTraceElement frame : e.getStackTrace()) {
            if (frame.getClassName().endsWith(".TransportDecoder")) {
                return true;
            }
        }
        return false;
    }
}
