package ru.runa.wfe.execution.dao;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.QSignal;
import ru.runa.wfe.execution.Signal;

@Component
public class SignalDao extends GenericDao<Signal> {

    public List<Signal> findByMessageSelectorsContainsOrEmpty(String messageSelector) {
        QSignal s = QSignal.signal;
        return queryFactory.selectFrom(s).where(s.messageSelectorsValue.contains(messageSelector).or(s.messageSelectorsValue.isNull()))
                .orderBy(s.createDate.asc()).fetch();
    }

    public long deleteAllExpired() {
        QSignal s = QSignal.signal;
        return queryFactory.delete(s).where(s.expiryDate.before(new Date())).execute();
    }
}
