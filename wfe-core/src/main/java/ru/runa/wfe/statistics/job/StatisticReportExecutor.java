package ru.runa.wfe.statistics.job;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.InstallationProperties;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.statistics.StatisticReportLog;
import ru.runa.wfe.statistics.dao.StatisticReportLogDao;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.runa.wfe.statistics.service.StatisticReportService;

public class StatisticReportExecutor implements Runnable {

    private static Log logger = LogFactory.getLog(StatisticReportExecutor.class);
    private ScheduledExecutorService pool;
    private int daysWaitAfterError;

    private StatisticReportLogDao statisticReportLogDao;
    private StatisticReportService statisticReportService;

    @Autowired
    public StatisticReportExecutor(StatisticReportLogDao statisticReportLogDao,
                                       StatisticReportService statisticReportService) {
        this.statisticReportService = statisticReportService;
        this.statisticReportLogDao = statisticReportLogDao;

        pool = Executors.newSingleThreadScheduledExecutor();
        daysWaitAfterError = InstallationProperties.getStatisticReportDaysWaitAfterError();
    }

    public void init() {
        pool.scheduleAtFixedRate(this, 2, 1440, TimeUnit.MINUTES);
    }

    public void run() {
        if (SystemProperties.isReportStatisticEnabled()) {
            logger.debug("============ Start to send  statistic report  ============");
            try {
                execute();
            } catch (Exception e) {
                logger.error("Can't execute send server statistic report. Try latter..", e);
            }
        }
    }

    private void execute() {

        String uuid = InstallationProperties.getInstallationUuid();
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("UUID must be specified for sending statistic report");
        }

        StatisticReportLog lastLog = statisticReportLogDao.getLastLogByUuid(uuid);

        if (lastLog == null) {
            statisticReportService.saveStatisticReportLog(true);
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastLogDate = convertToLocalDateTime(lastLog.getCreateDate());
            boolean success;

            boolean readyToSend =  lastLog.isSuccessExecution() ?
                    now.compareTo(lastLogDate.plus(Period.ofMonths(1))) > 0 :
                    now.compareTo(lastLogDate.plus(Period.ofDays(daysWaitAfterError))) > 0;

            if (readyToSend) {
                try {
                    Map<String, Object> usageInfo = statisticReportService.getInfo();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ready to send server statistic report: ");
                        logger.debug(usageInfo.entrySet());
                    }
                    statisticReportService.sendInfo(usageInfo);
                    success = true;
                } catch (Exception e) {
                    logger.error("Can't send server statistic report to endpoint. Try latter..");
                    success = false;
                }
                statisticReportService.saveStatisticReportLog(success);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.info("Not ready to send server statistic report. Wait...");
                }
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
