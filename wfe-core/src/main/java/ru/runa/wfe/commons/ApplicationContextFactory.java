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
import ru.runa.wfe.commons.hibernate.Converters;
import ru.runa.wfe.definition.dao.DeploymentDao;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.async.INodeAsyncExecutor;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.relation.dao.RelationDao;
import ru.runa.wfe.relation.dao.RelationPairDao;
import ru.runa.wfe.report.dao.ReportDao;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.ss.dao.SubstitutionDao;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.logic.ITaskNotifier;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.logic.VariableLogic;

@Component
public class ApplicationContextFactory implements ApplicationContextAware {
    private static ApplicationContext context;
    private static DBType dbType;

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

    public static JobDao getJobDAO() {
        return getContext().getBean(JobDao.class);
    }

    public static TaskDao getTaskDAO() {
        return getContext().getBean(TaskDao.class);
    }

    public static SwimlaneDao getSwimlaneDAO() {
        return getContext().getBean(SwimlaneDao.class);
    }

    public static TokenDao getTokenDAO() {
        return getContext().getBean(TokenDao.class);
    }

    public static SettingDao getSettingDAO() {
        return getContext().getBean(SettingDao.class);
    }

    public static ProcessDao getProcessDAO() {
        return getContext().getBean(ProcessDao.class);
    }

    public static NodeProcessDao getNodeProcessDAO() {
        return getContext().getBean(NodeProcessDao.class);
    }

    public static ProcessLogDao getProcessLogDAO() {
        return getContext().getBean(ProcessLogDao.class);
    }

    public static Converters getConverters() {
        return getContext().getBean(Converters.class);
    }

    public static BusinessCalendar getBusinessCalendar() {
        return getContext().getBean(BusinessCalendar.class);
    }

    public static IProcessDefinitionLoader getProcessDefinitionLoader() {
        return getContext().getBean(IProcessDefinitionLoader.class);
    }

    public static INodeAsyncExecutor getNodeAsyncExecutor() {
        return getContext().getBean(INodeAsyncExecutor.class);
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

    public static DBType getDBType() {
        if (dbType == null) {
            String hibernateDialect = getConfiguration().getProperty("hibernate.dialect");
            if (hibernateDialect.contains("HSQL")) {
                dbType = DBType.HSQL;
            } else if (hibernateDialect.contains("Oracle")) {
                dbType = DBType.ORACLE;
            } else if (hibernateDialect.contains("Postgre")) {
                dbType = DBType.POSTGRESQL;
            } else if (hibernateDialect.contains("MySQL")) {
                dbType = DBType.MYSQL;
            } else if (hibernateDialect.contains("SQLServer")) {
                dbType = DBType.MSSQL;
            } else if (hibernateDialect.contains("H2")) {
                dbType = DBType.H2;
            } else {
                dbType = DBType.GENERIC;
            }
        }
        return dbType;
    }

    public static ExecutorDAO getExecutorDAO() {
        return getContext().getBean(ExecutorDAO.class);
    }

    public static DeploymentDao getDeploymentDAO() {
        return getContext().getBean(DeploymentDao.class);
    }

    public static PermissionDAO getPermissionDAO() {
        return getContext().getBean(PermissionDAO.class);
    }

    public static RelationDao getRelationDAO() {
        return getContext().getBean(RelationDao.class);
    }

    public static RelationPairDao getRelationPairDAO() {
        return getContext().getBean(RelationPairDao.class);
    }

    public static SubstitutionDao getSubstitutionDAO() {
        return getContext().getBean(SubstitutionDao.class);
    }

    public static ReportDao getReportDAO() {
        return getContext().getBean(ReportDao.class);
    }

    public static List<ITaskNotifier> getTaskNotifiers() {
        return Lists.newArrayList(getContext().getBeansOfType(ITaskNotifier.class).values());
    }

    public static ExecutorLogic getExecutorLogic() {
        return getContext().getBean(ExecutorLogic.class);
    }

    public static VariableLogic getVariableLogic() {
        return getContext().getBean(VariableLogic.class);
    }

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
