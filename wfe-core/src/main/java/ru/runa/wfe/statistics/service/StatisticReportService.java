package ru.runa.wfe.statistics.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.bot.dao.BotTaskDao;
import ru.runa.wfe.commons.GitProperties;
import ru.runa.wfe.commons.InstallationProperties;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.relation.dao.RelationDao;
import ru.runa.wfe.report.dao.ReportDefinitionDao;
import ru.runa.wfe.statistics.StatisticReportLog;
import ru.runa.wfe.statistics.dao.StatisticReportLogDao;
import ru.runa.wfe.user.dao.ExecutorDao;

@Service
public class StatisticReportService {

    private static Log logger = LogFactory.getLog(StatisticReportService.class);
    private final String urlStr;

    @Autowired
    private BotTaskDao botTaskDao;
    @Autowired
    private ReportDefinitionDao reportDefinitionDao;
    @Autowired
    private RelationDao relationDao;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private ProcessDefinitionDao processDefinitionDao;
    @Autowired
    private StatisticReportLogDao statisticReportLogDao;

    public StatisticReportService() {
        urlStr = InstallationProperties.getStatisticReportRootUrl();
    }

    @Transactional
    public void saveStatisticReportLog(boolean success) {
        StatisticReportLog log = new StatisticReportLog();
        log.setVersion(SystemProperties.getVersion());
        log.setUuid(InstallationProperties.getInstallationUuid());
        log.setCreateDate(Calendar.getInstance().getTime());
        log.setSuccessExecution(success);
        statisticReportLogDao.create(log);
    }

    @Transactional
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>(12);

        info.put("version", SystemProperties.getVersion());
        info.put("edition", GitProperties.getCommit());
        info.put("uuid", InstallationProperties.getInstallationUuid());
        info.put("installDate", InstallationProperties.getInstallationDateString());
        info.put("referrerUrl", InstallationProperties.getReferrerUrl());

        info.put("botTaskCount", botTaskDao.getAllCount());
        info.put("reportsCount", reportDefinitionDao.getAllCount());
        info.put("actorsCount", executorDao.getAllActorsCount());
        info.put("relationsCount", relationDao.getAllCount());
        info.put("activeProcessesCount", currentProcessDao.getAllActiveProcessesCount());
        info.put("completedProcessesCount", currentProcessDao.getAllCompletedProcessesCount());
        info.put("processDefinitionsCount", processDefinitionDao.findAllDefinitionIds().size());
        info.put("dataSourcesCount", DataSourceStorage.getAllDataSourcesCount());

        return info;
    }

    public void sendInfo(Map<String, Object> info) throws Exception {
        String infoStr = JSONObject.toJSONString(info);
        URL u = new URL(urlStr + "/wfe/statistic");
        sendRequest(u, infoStr.getBytes("UTF-8"));
    }

    private SSLSocketFactory createSslSocketFactory() throws Exception {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{ new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } }, new SecureRandom());
        return context.getSocketFactory();
    }

    private void sendRequest(URL url, byte[] data) throws Exception {

        HttpURLConnection c = null;
        try {

            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setSSLSocketFactory(this.createSslSocketFactory());
                https.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
                c = https;
            } else {
                c = (HttpURLConnection) url.openConnection();
            }

            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setUseCaches(false);
            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            os.write(data);
            os.flush();
            c.connect();
            int status = c.getResponseCode();
            if (status != 200) {
                throw new Exception("Can't send statistic report to endpoint");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }
}
