package ru.runa.wfe.statistics.dao;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.statistics.QStatisticReportLog;
import ru.runa.wfe.statistics.StatisticReportLog;


/**
 * DAO level interface for server statistic report log
 */
@Component
@Transactional
public class StatisticReportLogDao extends GenericDao<StatisticReportLog> {

    public StatisticReportLogDao() {
        super(StatisticReportLog.class);
    }

    @Override
    public List<StatisticReportLog> getAll() {
        QStatisticReportLog srl = QStatisticReportLog.statisticReportLog;
        return queryFactory.selectFrom(srl)
                .orderBy(srl.createDate.desc())
                .fetch();
    }

    public StatisticReportLog getLastLogByUuid(String uuid) {
        QStatisticReportLog srl = QStatisticReportLog.statisticReportLog;
        return queryFactory.selectFrom(srl)
                .where(srl.uuid.eq(uuid))
                .orderBy(srl.createDate.desc())
                .fetchFirst();
    }
}
