package ru.runa.wfe.user.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.beanutils.BeanUtils;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.Executor;

@CommonsLog
public class ExecutorAdapter extends XmlAdapter<WfExecutor, Executor> {

    @Override
    public WfExecutor marshal(Executor executor) {
        WfExecutor wfExecutor = new WfExecutor();
        wfExecutor.setId(executor.getId());
        wfExecutor.setExecutorClassName(executor.getClass().getName());
        try {
            BeanUtils.copyProperties(wfExecutor, executor);
        } catch (Exception e) {
            log.error("ws conversion error", e);
        }
        return wfExecutor;
    }

    @Override
    public Executor unmarshal(WfExecutor executor) {
        return ApplicationContextFactory.getExecutorDAO().getExecutor(executor.getId());
    }

}
