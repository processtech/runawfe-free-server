package ru.runa.wfe.execution.dao;

import java.util.Collection;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.EventSubprocessTrigger;
import ru.runa.wfe.execution.QEventSubprocessTrigger;

@Component
public class EventSubprocessTriggerDao extends GenericDao<EventSubprocessTrigger> {

    public EventSubprocessTriggerDao() {
        super(EventSubprocessTrigger.class);
    }

    public List<EventSubprocessTrigger> findByMessageSelector(String messageSelector) {
        val t = QEventSubprocessTrigger.eventSubprocessTrigger;
        return queryFactory.selectFrom(t).where(t.messageSelector.eq(messageSelector)).fetch();
    }

    public List<EventSubprocessTrigger> findByMessageSelector(Collection<String> messageSelectors) {
        val t = QEventSubprocessTrigger.eventSubprocessTrigger;
        return queryFactory.selectFrom(t).where(t.messageSelector.in(messageSelectors)).fetch();
    }

    public void deleteByProcess(CurrentProcess process) {
        val t = QEventSubprocessTrigger.eventSubprocessTrigger;
        queryFactory.delete(t).where(t.process.eq(process)).execute();
    }

}
