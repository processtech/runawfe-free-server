package ru.runa.wfe.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.DateFilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.ArchivingService;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.ArchivingServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ArchivingApplication {

    private static final String SUCCESS_RESULT = "SUCCESS";

    protected static final Log log = LogFactory.getLog(ArchivingApplication.class);

    private static final SecuredObjectType[] DEFINITION_CLASSESS = { SecuredObjectType.DEFINITION };

    private static Properties properties;

    private static String USER_NAME;
    private static String PASSWORD;

    static {
        properties = new Properties();
        try {
            URL url = ArchivingApplication.class.getProtectionDomain().getCodeSource().getLocation();
            File parent = (new File(url.getPath())).getParentFile();
            String path = parent.getPath() + "/archiving-client.properties";
            File file = new File(path);
            if (file.exists()) {
                properties.load(new FileInputStream(path));
            } else {
                properties.load(ArchivingApplication.class.getClassLoader().getResourceAsStream("archiving-client.properties"));
            }
            USER_NAME = properties.getProperty("client.username");
            PASSWORD = properties.getProperty("client.password");
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public void backupProcesses(String[] args) {
        try {
            int minutesCount = 10;
            int daysCount = 0;
            if (args != null && args.length == 2) {
                minutesCount = Integer.parseInt(args[0]);
                if (minutesCount <= 0) {
                    throw new IllegalArgumentException("wrong minutes argument");
                }
                daysCount = Integer.parseInt(args[1]);
                if (daysCount < 0) {
                    throw new IllegalArgumentException("wrong days argument");
                }

                AuthenticationService authenticationService = Delegates.getAuthenticationService();
                User user = authenticationService.authenticateByLoginPassword(USER_NAME, PASSWORD);

                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.DATE, (-1) * daysCount);

                BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
                int endDateFieldIndex = ProcessClassPresentation.getInstance().getFieldIndex(ProcessClassPresentation.PROCESS_END_DATE);
                batchPresentation.getFilteredFields().put(endDateFieldIndex, new DateFilterCriteria(null, c.getTime()));

                long startTime = System.currentTimeMillis();
                List<WfProcess> processes = Delegates.getExecutionService().getProcesses(user, batchPresentation);

                for (WfProcess wfProcess : processes) {
                    try {
                        log.info(String.format("start backup process with id = %s ...", wfProcess.getId()));
                        ArchivingServiceDelegate.getArchivingServiceStatic().backupProcess(user, wfProcess.getId());
                        log.info(String.format(SUCCESS_RESULT));
                    } catch (Exception e) {
                        log.error(String.format("error execute backup process with id = %s", wfProcess.getId()));
                        log.error("", e);
                    }
                    if (isEnd(startTime, minutesCount)) {
                        log.info("time expired");
                        break;
                    }
                }
                log.info("End backup processes");
            }
        } catch (Exception e) {
            log.error("", e);
            System.exit(-1);
        }
    }

    public void backupProcessDefinition(String[] args) {
        try {
            int minutesCount = 10;
            int daysCount = 0;
            if (args != null && args.length == 2) {
                minutesCount = Integer.parseInt(args[0]);
                if (minutesCount <= 0) {
                    throw new IllegalArgumentException("wrong minutes argument");
                }
                daysCount = Integer.parseInt(args[1]);
                if (daysCount < 0) {
                    throw new IllegalArgumentException("wrong days argument");
                }

                AuthenticationService authenticationService = Delegates.getAuthenticationService();
                User user = authenticationService.authenticateByLoginPassword(USER_NAME, PASSWORD);

                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.DATE, (-1) * daysCount);

                long startTime = System.currentTimeMillis();

                AuthorizationService authorizationService = Delegates.getAuthorizationService();

                ru.runa.wfe.presentation.BatchPresentation presentation = new ru.runa.wfe.presentation.BatchPresentation(
                        ru.runa.wfe.presentation.ClassPresentationType.DEFINITION, BatchPresentationConsts.DEFAULT_ID, null);
                Map<Integer, FilterCriteria> map = new HashMap<Integer, FilterCriteria>();
                DateFilterCriteria criteria = new DateFilterCriteria();
                String[] dates = { "", CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT.format(c.getTime()) };
                criteria.applyFilterTemplates(dates);
                map.put(3, criteria);
                presentation.setFilteredFields(map);

                List<Deployment> defs = authorizationService.getPersistentObjects(user, presentation, Deployment.class,
                        DefinitionPermission.REDEPLOY_DEFINITION, DEFINITION_CLASSESS, false);

                ArchivingService archivingService = ArchivingServiceDelegate.getArchivingServiceStatic();

                for (Deployment deployment : defs) {
                    try {
                        log.info(String.format("start backup process definition with name = %s and version = %s ...", deployment.getName(),
                                deployment.getVersion()));
                        archivingService.backupProcessDefinition(user, deployment.getName(), deployment.getVersion());
                        log.info(String.format(SUCCESS_RESULT));
                    } catch (Exception e) {
                        log.error(String.format("error execute backup process definition with name = %s and version = %s", deployment.getName(),
                                deployment.getVersion()));
                        log.error("", e);
                    }
                    if (isEnd(startTime, minutesCount)) {
                        log.info("time expired");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void restoreProcess(String[] args) {
        if (args != null && args.length > 0) {

            Long processId = Long.parseLong(args[0]);
            AuthenticationService authenticationService = Delegates.getAuthenticationService();
            ArchivingService archivingService = ArchivingServiceDelegate.getArchivingServiceStatic();

            User user = authenticationService.authenticateByLoginPassword(USER_NAME, PASSWORD);

            try {
                log.info(String.format("start restore process with id = %s ...", processId));
                archivingService.restoreProcess(user, processId);
                log.info(String.format(SUCCESS_RESULT));
            } catch (Exception e) {
                log.error(String.format("error of restore process with id = %s", processId));
                log.error("", e);
            }
        }
    }

    public void restoreProcessDefinition(String[] args) {
        String definitionName = "";
        Long version = 0L;
        if (args != null && args.length > 2) {

            if ((args[0].startsWith("\'") || args[0].startsWith("\""))
                    && (args[args.length - 2].endsWith("\'") || args[args.length - 2].endsWith("\""))) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length - 1; i++) {
                    if (i == 0) {
                        sb.append(args[i].substring(1));
                        continue;
                    }
                    if (i == args.length - 2) {
                        sb.append(" ");
                        sb.append(args[i].substring(0, args[i].length() - 1));
                        definitionName = sb.toString();
                        break;
                    }
                    sb.append(" ");
                    sb.append(args[i]);
                }
                version = Long.parseLong(args[args.length - 1]);
            }
        } else if (args != null && args.length == 2) {

            definitionName = args[0];
            version = Long.parseLong(args[1]);
        }
        if (!"".equals(definitionName) && version != 0) {
            AuthenticationService authenticationService = Delegates.getAuthenticationService();
            ArchivingService archivingService = ArchivingServiceDelegate.getArchivingServiceStatic();

            User user = authenticationService.authenticateByLoginPassword(USER_NAME, PASSWORD);
            try {
                log.info(String.format("start restore process definition with name = %s and version = %s ...", definitionName, version));
                archivingService.restoreProcessDefinition(user, definitionName, version);
                log.info(String.format(SUCCESS_RESULT));
            } catch (Exception e) {
                log.error(String.format("error of restore process definition with definitionName = %s and version = %s", definitionName, version));
                log.error("", e);
            }
        }
    }

    private static boolean isEnd(long start, int minutes) {
        long currentTime = System.currentTimeMillis();
        return currentTime >= start + minutes * 60 * 1000;
    }

}
