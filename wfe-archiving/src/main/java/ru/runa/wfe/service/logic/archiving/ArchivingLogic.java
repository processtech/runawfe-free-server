package ru.runa.wfe.service.logic.archiving;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Table;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.service.ArchivingService;
import ru.runa.wfe.service.exceptions.DefinitionHasProcessesException;
import ru.runa.wfe.service.exceptions.PermissionDeniedException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.Variable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ArchivingLogic extends WFCommonLogic implements ArchivingService {

    private static final String UNDEFINED_NULL_VALUE = "undefined";

    private static final String ASSIGNED_STRATEGY = "assigned";

    private static Map<String, SessionFactory> factoryMap = Maps.newConcurrentMap();

    private static Map<String, MappingInfo> MAPPING_INFO;

    protected final Log log = LogFactory.getLog(ArchivingLogic.class);

    @Autowired
    @Qualifier("processDAO")
    private ProcessDAO processDAO;
    @Autowired
    @Qualifier("archProcessDAO")
    private ProcessDAO archProcessDAO;
    @Autowired
    @Qualifier("deploymentDAO")
    private DeploymentDAO deploymentDAO;
    @Autowired
    @Qualifier("archDeploymentDAO")
    private DeploymentDAO archDeploymentDAO;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backupProcess(User user, Long processId) {
        processLogic(user, processId, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backupProcessDefinition(User user, String definitionName, Long version) {
        deploymentLogic(user, definitionName, version, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreProcess(User user, Long processId) {
        processLogic(user, processId, false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreProcessDefinition(User user, String definitionName, Long version) {
        deploymentLogic(user, definitionName, version, false);
    }

    private void processLogic(User user, Long processId, boolean toArchive) {
        try {
            HibernateTemplate targetTemplate = null;
            ProcessDAO targetProcessDAO = null;
            ProcessDAO sourceProcessDAO = null;
            DeploymentDAO targetDeploymentDAO = null;
            Set<Swimlane> swimlanes = null;

            if (toArchive) {
                targetProcessDAO = archProcessDAO;
                targetDeploymentDAO = archDeploymentDAO;
                sourceProcessDAO = processDAO;
            } else {
                targetProcessDAO = processDAO;
                targetDeploymentDAO = deploymentDAO;
                sourceProcessDAO = archProcessDAO;
            }
            Process process = sourceProcessDAO.get(processId);
            if (process == null) {
                Process processFromTarget = targetProcessDAO.get(processId);
                if (processFromTarget == null) {
                    throw new EntityNotFoundException();
                }
                return;
            }
            boolean isParent = isParent(process, sourceProcessDAO.getHibernateTemplate());
            log.info("PROCESS WITH ID = " + processId + " IS PARENT - " + isParent);
            if (!isParent) {
                return;
            }
            targetTemplate = targetProcessDAO.getHibernateTemplate();
            targetTemplate.setSessionFactory(getSessionFactory(toArchive, true));
            Session session = targetTemplate.getSessionFactory().getCurrentSession();

            targetProcessDAO.setHibernateTemplate(targetTemplate);
            targetDeploymentDAO.setHibernateTemplate(targetTemplate);

            if (toArchive) {
                boolean isAllowed = isPermissionAllowed(user, process, ProcessPermission.CANCEL_PROCESS);
                if (!isAllowed) {
                    throw new PermissionDeniedException();
                }
            }
            HibernateTemplate srcTemplate = sourceProcessDAO.getHibernateTemplate();
            LinkedList<Process> linkedList = getLinkedProcesses(process.getId(), srcTemplate, sourceProcessDAO);
            for (Process p : linkedList) {
                processReplicateWithoutSubprocesses(p.getId(), session, targetDeploymentDAO, sourceProcessDAO, targetTemplate, srcTemplate, toArchive);
            }
            List<NodeProcess> nodeProcesses = getNodeProcesses(srcTemplate, process, null, null, null);
            for (NodeProcess nodeProcess : nodeProcesses) {
                replicateSubprocess(nodeProcess, toArchive, session, targetTemplate);
            }
            swimlanes = sourceProcessDAO.get(processId).getSwimlanes();
            for (Swimlane swimlane : swimlanes) {
                Executor executor = swimlane.getExecutor();
                if (executor != null) {
                    replicateExecutor(executor, toArchive, session, targetTemplate);
                }
                replicateSwimlane(swimlane, toArchive, session, targetTemplate);
            }
            replicateLinkedRecords(toArchive, targetTemplate, session, srcTemplate, linkedList);

            Process toDelete = sourceProcessDAO.get(processId);
            deleteProcess(toDelete, sourceProcessDAO.getHibernateTemplate(), toArchive);

            targetTemplate.setSessionFactory(getSessionFactory(toArchive, false));
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (PermissionDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("error backup process with id = %s", processId));
            log.error("", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void deploymentLogic(User user, String definitionName, Long version, boolean toArchive) {
        try {
            HibernateTemplate template = null;
            DeploymentDAO sourceDeploymentDAO = null;
            DeploymentDAO targetDeploymentDAO = null;
            Deployment source = null;
            Deployment target = null;
            boolean hasProcesses = false;

            if (toArchive) {
                sourceDeploymentDAO = deploymentDAO;
                targetDeploymentDAO = archDeploymentDAO;
            } else {
                sourceDeploymentDAO = archDeploymentDAO;
                targetDeploymentDAO = deploymentDAO;
            }

            try {
                source = sourceDeploymentDAO.findDeployment(definitionName, version);
                if (source != null) {
                    List<Deployment> deployments = sourceDeploymentDAO.getHibernateTemplate().find("from Process p where p.deployment = ?", source);
                    if (deployments != null && deployments.size() > 0) {
                        hasProcesses = true;
                        if (toArchive) {
                            throw new DefinitionHasProcessesException();
                        }
                    }
                }

            } catch (DefinitionDoesNotExistException e) {
                // ignore
            }
            if (source != null) {
                template = targetDeploymentDAO.getHibernateTemplate();
                template.setSessionFactory(getSessionFactory(toArchive, true));
                Session session = template.getSessionFactory().getCurrentSession();
                targetDeploymentDAO.setHibernateTemplate(template);

                if (toArchive) {
                    if (!isPermissionAllowed(user, source, DefinitionPermission.UNDEPLOY_DEFINITION)) {
                        throw new PermissionDeniedException();
                    }
                }
                try {
                    target = targetDeploymentDAO.findDeployment(definitionName, version);
                } catch (DefinitionDoesNotExistException e) {
                }
                if (toArchive) {
                    if (target != null) {
                        sourceDeploymentDAO.delete(source);
                        return;
                    }
                }
                replicateDeployment(source, toArchive, session, targetDeploymentDAO.getHibernateTemplate());

                if (!hasProcesses) {
                    sourceDeploymentDAO.delete(source);
                }
                template.setSessionFactory(getSessionFactory(toArchive, false));
                targetDeploymentDAO.setHibernateTemplate(template);
            }
        } catch (DefinitionHasProcessesException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("error backup process definition with name = %s and version = %s", definitionName, version));
            log.error("", e);
        }
    }

    private void replicateLinkedRecords(boolean toArchive, HibernateTemplate targetTemplate, Session session, HibernateTemplate srcTemplate,
            LinkedList<Process> linkedList) {
        try {
            for (Process p : linkedList) {
                List<ProcessLog> logs = getProcessLogs(p.getId(), srcTemplate);
                for (ProcessLog processLog : logs) {
                    replicateProcessLog(processLog, toArchive, session, targetTemplate);
                }
                List<Job> jobs = getJobs(p, srcTemplate);
                for (Job job : jobs) {
                    replicateJob(job, toArchive, session, targetTemplate);
                }
                List<Variable<?>> variables = getVariables(p, srcTemplate);
                for (Variable<?> variable : variables) {
                    replicateVariable(variable, toArchive, session, targetTemplate);
                }
            }
        } catch (Exception e) {
            log.error("error replicate processlog or job or variable");
            log.error("", e);
        }
    }

    private LinkedList<Process> getLinkedProcesses(Long processId, HibernateTemplate srcTemplate, ProcessDAO srcProcessDAO) {
        LinkedList<Process> linkedList = Lists.newLinkedList();
        Process process = srcProcessDAO.get(processId);
        if (process != null) {
            addSubProcessesToLinkedList(linkedList, process, srcTemplate);
        }
        return linkedList;
    }

    private void addSubProcessesToLinkedList(LinkedList<Process> linkedList, Process process, HibernateTemplate srcTemplate) {
        linkedList.add(process);
        List<Process> subprocesses = getSubprocesses(srcTemplate, process);
        for (Process subProcess : subprocesses) {
            addSubProcessesToLinkedList(linkedList, subProcess, srcTemplate);
        }
    }

    private void processReplicateWithoutSubprocesses(Long processId, Session session, DeploymentDAO targetDeploymentDAO, ProcessDAO sourceProcessDAO,
            HibernateTemplate targetTemplate, HibernateTemplate srcTemplate, boolean toArchive) {
        Process process = sourceProcessDAO.get(processId);
        Token rootToken = process.getRootToken();
        clearChild(rootToken);
        rootToken.setProcess(null);
        Deployment deployment = process.getDeployment();
        Deployment existDep = null;
        try {
            existDep = targetDeploymentDAO.findDeployment(deployment.getName(), deployment.getVersion());
        } catch (DefinitionDoesNotExistException e) {
            // ignore
        }
        if (existDep != null) {
            deployment = existDep;
        } else {
            replicateDeployment(deployment, toArchive, session, targetTemplate);
        }
        replicateToken(rootToken, toArchive, session, targetTemplate);
        process.setDeployment(deployment);
        replicateProcess(process, toArchive, session, targetTemplate);
    }

    private void replicateProcess(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Process.class, toArchive, session, targetTemplate);
    }

    private void replicateDeployment(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Deployment.class, toArchive, session, targetTemplate);
    }

    private void replicateToken(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Token.class, toArchive, session, targetTemplate);
    }

    private void replicateSubprocess(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, NodeProcess.class, toArchive, session, targetTemplate);
    }

    private void replicateSwimlane(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Swimlane.class, toArchive, session, targetTemplate);
    }

    private void replicateProcessLog(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, ProcessLog.class, toArchive, session, targetTemplate);
    }

    private void replicateJob(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Job.class, toArchive, session, targetTemplate);
    }

    private void replicateVariable(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Variable.class, toArchive, session, targetTemplate);
    }

    private void replicateExecutor(Object entity, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        replicate(entity, Executor.class, toArchive, session, targetTemplate);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void replicate(Object entity, Class<?> entityClass, boolean toArchive, Session session, HibernateTemplate targetTemplate) {
        String tableName = getTableName(entityClass);
        try {
            setIdentityInsert(session, true, tableName);
            prepareReplicate(toArchive);
            targetTemplate.replicate(entity, ReplicationMode.OVERWRITE);
            finishReplicate(toArchive);
        } finally {
            setIdentityInsert(session, false, tableName);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ProcessLog> getProcessLogs(Long processId, HibernateTemplate template) {
        return template.find("from ProcessLog where processId=? order by id asc", processId);
    }

    @SuppressWarnings("unchecked")
    private List<Job> getJobs(Process process, HibernateTemplate template) {
        return template.find("from Job where process=?", process);
    }

    @SuppressWarnings("unchecked")
    private List<Variable<?>> getVariables(Process process, HibernateTemplate template) {
        return template.find("from Variable where process=?", process);
    }

    private String getTableName(Class<?> clazz) {
        Table table = null;
        try {
            table = clazz.getAnnotation(Table.class);
        } catch (Exception e) {
            log.warn("class " + clazz.getName() + " has no annotation javax.persistence.Table");
            return clazz.getSimpleName();
        }
        return table.name();
    }

    private Configuration getConfiguration(boolean toArchive) {
        if (toArchive) {
            return getArchiveConfiguration();
        }
        return ApplicationContextFactory.getConfiguration();
    }

    private void prepareReplicate(boolean toArchive) {
        if (toArchive) {
            prepareReplicate(processDAO.getHibernateTemplate(), archProcessDAO.getHibernateTemplate());
        } else {
            prepareReplicate(archProcessDAO.getHibernateTemplate(), processDAO.getHibernateTemplate());
        }
    }

    private void finishReplicate(boolean toArchive) {
        if (toArchive) {
            finishReplicate(archProcessDAO.getHibernateTemplate());
        } else {
            finishReplicate(processDAO.getHibernateTemplate());
        }
    }

    private void prepareReplicate(HibernateTemplate src, HibernateTemplate target) {
        src.clear();
        target.flush();
        target.clear();
    }

    private void finishReplicate(HibernateTemplate target) {
        target.flush();
        target.clear();
    }

    private void clearChild(Token rootToken) {
        if (rootToken.getChildren() == null) {
            return;
        }
        for (Token childToken : rootToken.getChildren()) {
            clearChild(childToken);
            childToken.setProcess(null);
        }
    }

    public static synchronized Configuration getArchiveConfiguration() {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) ApplicationContextFactory.getContext().getBean("&sessionFactoryA");
        return factoryBean.getConfiguration();
    }

    @SuppressWarnings("unchecked")
    private void initMappingInfo(Configuration configuration) {
        if (MAPPING_INFO != null && MAPPING_INFO.size() > 0) {
            return;
        }
        configuration.buildMappings();
        Iterator<PersistentClass> iter = configuration.getClassMappings();
        Set<PersistentClass> persistentSet = Sets.newHashSet(iter);
        MAPPING_INFO = getMappingInfo(persistentSet);
    }

    private Map<String, MappingInfo> getMappingInfo(Set<PersistentClass> set) {
        Map<String, MappingInfo> info = Maps.newHashMap();
        for (PersistentClass persistentClass : set) {
            KeyValue val = persistentClass.getIdentifier();
            if (val instanceof SimpleValue) {
                SimpleValue vv = (SimpleValue) val;
                info.put(persistentClass.getClassName(),
                        new MappingInfo(vv.getIdentifierGeneratorStrategy(), vv.getNullValue(), persistentClass.isLazy()));
            }
        }
        return info;
    }

    private void setMapping(Set<PersistentClass> persistentSet, String strategy, String nullValue, boolean isLazy) {
        for (PersistentClass persistentClass : persistentSet) {
            KeyValue val = persistentClass.getIdentifier();
            if (val instanceof SimpleValue) {
                SimpleValue vv = (SimpleValue) val;
                vv.setIdentifierGeneratorStrategy(strategy);
                vv.setNullValue(nullValue);
            }
            persistentClass.setLazy(isLazy);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized SessionFactory getSessionFactory(boolean toArchive, boolean isReplicate) {
        String key = getConfigKey(toArchive, isReplicate);
        SessionFactory factory = factoryMap.get(key);
        if (factory != null) {
            return factory;
        } else {
            Configuration cfg = getConfiguration(toArchive);
            initMappingInfo(cfg);
            if (isReplicate) {
                setMapping(Sets.newHashSet(cfg.getClassMappings()), ASSIGNED_STRATEGY, UNDEFINED_NULL_VALUE, false);
            } else {
                resetMapping(MAPPING_INFO, cfg);
            }
            factory = cfg.buildSessionFactory();
            factoryMap.put(key, factory);
        }
        return factory;
    }

    @SuppressWarnings("unchecked")
    private Configuration resetMapping(Map<String, MappingInfo> info, Configuration configuration) {
        configuration.buildMappings();
        Iterator<PersistentClass> iter = configuration.getClassMappings();
        while (iter.hasNext()) {
            PersistentClass persistentClass = iter.next();
            KeyValue val = persistentClass.getIdentifier();
            if (val instanceof SimpleValue) {
                SimpleValue vv = (SimpleValue) val;
                vv.setIdentifierGeneratorStrategy(info.get(persistentClass.getClassName()).getStrategy());
                vv.setNullValue(info.get(persistentClass.getClassName()).getNullValue());
            }
            persistentClass.setLazy(info.get(persistentClass.getClassName()).isLazy());
        }
        return configuration;
    }

    @SuppressWarnings("deprecation")
    private synchronized void setIdentityInsert(Session session, boolean enableInsert, String tableName) {
        try {
            // TODO: session.connection() is depricated (scheduled for removal in 4.x). Replacement depends on need; for doing direct JDBC stuff use
            // TODO: doWork(org.hibernate.jdbc.Work); for opening a 'temporary Session' use (TBD).
            session.connection().createStatement().execute(String.format("SET IDENTITY_INSERT %s %s", tableName, (enableInsert ? "ON" : "OFF")));
        } catch (HibernateException e) {
            log.error("", e);
        } catch (SQLException e) {
            log.error("", e);
        }
    }

    private void deleteProcess(Process process, HibernateTemplate src, boolean toArchive) {
        src.bulkUpdate("delete from PermissionMapping where type=? and identifiableId=?", process.getSecuredObjectType(), process.getIdentifiableId());
        List<Process> subProcesses = getSubprocesses(src, process);
        src.bulkUpdate("delete from NodeProcess where process=?", process);
        for (Process subProcess : subProcesses) {
            deleteProcess(subProcess, src, toArchive);
        }
        src.bulkUpdate("delete from ProcessLog where processId=?", process.getId());
        src.bulkUpdate("delete from Job where process=?", process);
        src.bulkUpdate("delete from Variable where process=?", process);
        if (toArchive) {
            processDAO.delete(process);
        } else {
            archProcessDAO.delete(process);
        }
    }

    private List<Process> getSubprocesses(HibernateTemplate template, Process process) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(template, process, null, null, null);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<NodeProcess> getNodeProcesses(HibernateTemplate template, final Process process, final Token parentToken, final String nodeId,
            final Boolean active) {
        return template.executeFind(new HibernateCallback<List<Process>>() {

            @Override
            public List<Process> doInHibernate(Session session) {
                List<String> conditions = Lists.newArrayList();
                Map<String, Object> parameters = Maps.newHashMap();
                if (process != null) {
                    conditions.add("process=:process");
                    parameters.put("process", process);
                }
                if (parentToken != null) {
                    conditions.add("parentToken=:parentToken");
                    parameters.put("parentToken", parentToken);
                }
                if (nodeId != null) {
                    conditions.add("nodeId=:nodeId");
                    parameters.put("nodeId", nodeId);
                }
                if (active != null) {
                    if (active) {
                        conditions.add("subProcess.endDate is null");
                    } else {
                        conditions.add("subProcess.endDate is not null");
                    }
                }
                if (conditions.size() == 0) {
                    throw new IllegalArgumentException("Filter should be specified");
                }
                String hql = "from NodeProcess where " + Joiner.on(" and ").join(conditions) + " order by id asc";
                Query query = session.createQuery(hql);
                for (Entry<String, Object> param : parameters.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private boolean isParent(Process process, HibernateTemplate srcTemplate) {
        List<NodeProcess> list = srcTemplate.find("from NodeProcess where subProcess.id = ?", process.getId());
        if (list != null && list.size() > 0) {
            return false;
        }
        return true;
    }

    public DataSource getArchivingDataSource() throws NamingException {
        String dsName = getConfiguration(true).getProperty("hibernate.connection.datasource");
        return (DataSource) new InitialContext().lookup(dsName);
    }

    private String getConfigKey(boolean toArchive, boolean isReplicate) {
        return new StringBuilder().append("config_").append(toArchive).append("_").append(isReplicate).toString();
    }

    class MappingInfo {
        private String strategy;
        private String nullValue;
        private boolean lazy;

        public MappingInfo(String strategy, String nullValue, boolean lazy) {
            this.strategy = strategy;
            this.nullValue = nullValue;
            this.lazy = lazy;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getNullValue() {
            return nullValue;
        }

        public void setNullValue(String nullValue) {
            this.nullValue = nullValue;
        }

        public boolean isLazy() {
            return lazy;
        }

        public void setLazy(boolean lazy) {
            this.lazy = lazy;
        }
    }

}
