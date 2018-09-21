package ru.runa.wfe.commons;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.dao.SettingDao;
import ru.runa.wfe.commons.dbmigration.InitializerLogic;
import ru.runa.wfe.commons.hibernate.Converters;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.async.NodeAsyncExecutor;
import ru.runa.wfe.execution.dao.CurrentNodeProcessDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.execution.dao.CurrentSwimlaneDao;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.relation.dao.RelationDao;
import ru.runa.wfe.relation.dao.RelationPairDao;
import ru.runa.wfe.report.dao.ReportDefinitionDao;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.ss.dao.SubstitutionDao;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.logic.TaskNotifier;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.logic.VariableLogic;

@Component
public class ApplicationContextFactory implements ApplicationContextAware {
    private static ApplicationContext context;
    private static DbType dbType;

    /**
     * Taken from: https://stackoverflow.com/a/28408260/4247442
     */
    @Override
    public void setApplicationContext(ApplicationContext c) throws BeansException {
        context = c;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static InitializerLogic getInitializerLogic() {
        return getContext().getBean(InitializerLogic.class);
    }

    public static JobDao getJobDao() {
        return getContext().getBean(JobDao.class);
    }

    public static TaskDao getTaskDao() {
        return getContext().getBean(TaskDao.class);
    }

    public static CurrentSwimlaneDao getCurrentSwimlaneDao() {
        return getContext().getBean(CurrentSwimlaneDao.class);
    }

    public static CurrentTokenDao getCurrentTokenDao() {
        return getContext().getBean(CurrentTokenDao.class);
    }

    public static SettingDao getSettingDao() {
        return getContext().getBean(SettingDao.class);
    }

    public static CurrentProcessDao getCurrentProcessDao() {
        return getContext().getBean(CurrentProcessDao.class);
    }

    public static CurrentNodeProcessDao getCurrentNodeProcessDao() {
        return getContext().getBean(CurrentNodeProcessDao.class);
    }

    public static ProcessLogDao getProcessLogDao() {
        return getContext().getBean(ProcessLogDao.class);
    }

    public static Converters getConverters() {
        return getContext().getBean(Converters.class);
    }

    public static BusinessCalendar getBusinessCalendar() {
        return getContext().getBean(BusinessCalendar.class);
    }

    public static ProcessDefinitionLoader getProcessDefinitionLoader() {
        return getContext().getBean(ProcessDefinitionLoader.class);
    }

    public static NodeAsyncExecutor getNodeAsyncExecutor() {
        return getContext().getBean(NodeAsyncExecutor.class);
    }

    // TODO avoid static methods, inject
    public static SessionFactory getSessionFactory() {
        return getContext().getBean(SessionFactory.class);
    }

    public static Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public static Configuration getConfiguration() {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) getContext().getBean("&sessionFactory");
        return factoryBean.getConfiguration();
    }

    public static DataSource getDataSource() throws NamingException {
        String dsName = getConfiguration().getProperty("hibernate.connection.datasource");
        return (DataSource) new InitialContext().lookup(dsName);
    }

    public static Dialect getDialect() {
        return Dialect.getDialect(getConfiguration().getProperties());
    }

    public static DbType getDbType() {
        if (dbType == null) {
            String hibernateDialect = getConfiguration().getProperty("hibernate.dialect");
            if (hibernateDialect.contains("HSQL")) {
                dbType = DbType.HSQL;
            } else if (hibernateDialect.contains("Oracle")) {
                dbType = DbType.ORACLE;
            } else if (hibernateDialect.contains("Postgre")) {
                dbType = DbType.POSTGRESQL;
            } else if (hibernateDialect.contains("MySQL")) {
                dbType = DbType.MYSQL;
            } else if (hibernateDialect.contains("SQLServer")) {
                dbType = DbType.MSSQL;
            } else if (hibernateDialect.contains("H2")) {
                dbType = DbType.H2;
            } else {
                dbType = DbType.GENERIC;
            }
        }
        return dbType;
    }

    public static ExecutorDao getExecutorDao() {
        return getContext().getBean(ExecutorDao.class);
    }

    public static ReportDefinitionDao getReportDefinitionDao() {
        return getContext().getBean(ReportDefinitionDao.class);
    }

    public static ProcessDefinitionDao getProcessDefinitionDao() {
        return getContext().getBean(ProcessDefinitionDao.class);
    }

    public static PermissionDao getPermissionDao() {
        return getContext().getBean(PermissionDao.class);
    }

    public static RelationDao getRelationDao() {
        return getContext().getBean(RelationDao.class);
    }

    public static RelationPairDao getRelationPairDao() {
        return getContext().getBean(RelationPairDao.class);
    }

    public static SubstitutionDao getSubstitutionDao() {
        return getContext().getBean(SubstitutionDao.class);
    }

    public static List<TaskNotifier> getTaskNotifiers() {
        return Lists.newArrayList(getContext().getBeansOfType(TaskNotifier.class).values());
    }

    public static ExecutorLogic getExecutorLogic() {
        return getContext().getBean(ExecutorLogic.class);
    }

    public static ExecutionLogic getExecutionLogic() {
        return getContext().getBean(ExecutionLogic.class);
    }

    public static VariableLogic getVariableLogic() {
        return getContext().getBean(VariableLogic.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createAutowiredBean(String className) {
        return (T) createAutowiredBean(ClassLoaderUtil.loadClass(className));
    }

    public static <T> T createAutowiredBean(Class<T> clazz) {
        try {
            T object = clazz.newInstance();
            autowireBean(object);
            return object;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> T autowireBean(T bean) {
        getContext().getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
    }
}
