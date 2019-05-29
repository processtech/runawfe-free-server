package ru.runa.wfe.extension;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * General abstraction for organization function
 * 
 * @since 2.1
 */
public abstract class OrgFunction {
    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    protected ExecutorDao executorDao;

    /**
     * @param parameters
     *            - array of parameters
     * @return id's of executors
     * @throws OrgFunctionException
     */
    public abstract List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException;

}
