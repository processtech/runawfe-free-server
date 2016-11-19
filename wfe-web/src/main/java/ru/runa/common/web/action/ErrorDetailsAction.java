package ru.runa.common.web.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.BotTaskIdentifier;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

@SuppressWarnings("unchecked")
public class ErrorDetailsAction extends ActionBase {

    private static final String HTML = "html";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        JSONObject rootObject = new JSONObject();
        try {
            IdNameForm form = (IdNameForm) actionForm;
            String action = form.getAction();
            if ("getBotTaskConfigurationError".equals(action)) {
                for (Map.Entry<BotTaskIdentifier, Throwable> entry : ProcessExecutionErrors.getBotTaskConfigurationErrors().entrySet()) {
                    if (Objects.equal(entry.getKey().getBot().getId(), form.getId())
                            && Objects.equal(entry.getKey().getBotTaskName(), form.getName())) {
                        String html = "<form id='supportForm'>";
                        html += "<input type='hidden' name='botId' value='" + form.getId() + "' />";
                        html += "<input type='hidden' name='botTaskName' value='" + form.getName() + "' />";
                        html += "</form>";
                        html += Throwables.getStackTraceAsString(entry.getValue());
                        rootObject.put(HTML, html);
                        break;
                    }
                }
            } else if ("getProcessError".equals(action)) {
                List<ProcessError> errorDetails = ProcessExecutionErrors.getProcessErrors(form.getId());
                if (errorDetails != null) {
                    for (ProcessError detail : errorDetails) {
                        if (Objects.equal(detail.getNodeId(), form.getName())) {
                            String html = "<form id='supportForm'>";
                            html += "<input type='hidden' name='processId' value='" + form.getId() + "' />";
                            html += "</form>";
                            html += detail.getThrowableDetails();
                            rootObject.put(HTML, html);
                        }
                    }
                }
            } else if ("showSupportFiles".equals(action)) {
                boolean fileIncluded = false;
                request.getParameter("botId");
                User user = getLoggedUser(request);
                JSONArray tabs = new JSONArray();
                Map<String, byte[]> supportFiles = Maps.newHashMap();
                // TODO privileges are required for successful operation!
                // TODO zip file name encoding (introduce zip encoding after
                // moving to Java7)
                Map<Long, List<Long>> processHierarchies = Maps.newHashMap();
                if (request.getParameter("processId") != null) {
                    initProcessHierarchy(user, processHierarchies, Long.parseLong(request.getParameter("processId")));
                } else if (request.getParameter("botTaskName") != null) {
                    Long botId = Long.parseLong(request.getParameter("botId"));
                    String botTaskName = request.getParameter("botTaskName");
                    BotTaskIdentifier botTaskIdentifier = ProcessExecutionErrors.getBotTaskIdentifierNotNull(botId, botTaskName);
                    addBotTabError(request, tabs, supportFiles, botTaskIdentifier);
                } else {
                    for (Long processId : ProcessExecutionErrors.getProcessErrors().keySet()) {
                        initProcessHierarchy(user, processHierarchies, processId);
                    }
                    for (BotTaskIdentifier botTaskIdentifier : ProcessExecutionErrors.getBotTaskConfigurationErrors().keySet()) {
                        addBotTabError(request, tabs, supportFiles, botTaskIdentifier);
                    }
                }
                for (Entry<Long, List<Long>> processesEntry : processHierarchies.entrySet()) {
                    Long rootProcessId = processesEntry.getKey();
                    JSONObject tab = new JSONObject();
                    tab.put("key", "process" + rootProcessId);
                    tab.put("title", getResources(request).getMessage("label.process") + " " + rootProcessId);
                    List<WfProcess> processes = Delegates.getExecutionService().getSubprocesses(user, rootProcessId, true);
                    processes.add(0, Delegates.getExecutionService().getProcess(user, rootProcessId));
                    Map<String, byte[]> processFiles = Maps.newHashMap();
                    JSONArray files = new JSONArray();
                    for (Long processId : processesEntry.getValue()) {
                        String exceptions = "";
                        List<ProcessError> errorDetails = ProcessExecutionErrors.getProcessErrors().get(processId);
                        for (ProcessError detail : errorDetails) {
                            exceptions += "\r\n---------------------------------------------------------------";
                            exceptions += "\r\n" + CalendarUtil.formatDateTime(detail.getOccurredDate()) + " " + detail.getNodeId() + "/"
                                    + detail.getTaskName();
                            if (detail.getBotTask() != null) {
                                String botTaskIdentifier = detail.getBotTask().getId() + "." + detail.getBotTask().getName();
                                exceptions += "\r\nbot task = " + detail.getBotTask().getTaskHandlerClassName() + "/" + botTaskIdentifier;
                                if (!processFiles.containsKey(botTaskIdentifier)) {
                                    processFiles.put(botTaskIdentifier, detail.getBotTask().getConfiguration());
                                    addSupportFileInfo(files, MessageFormat.format(
                                            getResources(request).getMessage("support.file.bottask.configuration"), detail.getBotTask().getName()),
                                            true);
                                }
                            }
                            exceptions += "\r\n" + detail.getThrowableDetails();
                        }
                        processFiles.put("exceptions." + processId + ".txt", exceptions.getBytes(Charsets.UTF_8));
                    }
                    addSupportFileInfo(files, getResources(request).getMessage("support.file.exceptions"), true);
                    for (WfProcess process : processes) {
                        String processDefinitionFileName = process.getName() + ".par";
                        if (!processFiles.containsKey(processDefinitionFileName)) {
                            try {
                                processFiles.put(
                                        processDefinitionFileName,
                                        Delegates.getDefinitionService().getProcessDefinitionFile(user, process.getDefinitionId(),
                                                IFileDataProvider.PAR_FILE));
                                fileIncluded = true;
                            } catch (Exception e) {
                                fileIncluded = false;
                                log.warn("definition for " + process, e);
                            }
                            addSupportFileInfo(files, MessageFormat.format(getResources(request).getMessage("support.file.process.definition"),
                                    processDefinitionFileName), fileIncluded);
                        }
                        try {
                            processFiles.put(process.getId() + ".graph.png",
                                    Delegates.getExecutionService().getProcessDiagram(user, process.getId(), null, null, null));
                            fileIncluded = true;
                        } catch (Exception e) {
                            fileIncluded = false;
                            log.warn("process graph for " + process, e);
                        }
                        addSupportFileInfo(files,
                                MessageFormat.format(getResources(request).getMessage("support.file.process.graph"), process.getId()), fileIncluded);
                        try {
                            byte[] logs = getProcessLogs(request, user, process.getId()).getBytes(Charsets.UTF_8);
                            processFiles.put(process.getId() + ".log.html", logs);
                            fileIncluded = true;
                        } catch (Exception e) {
                            fileIncluded = false;
                            log.warn("process logs for " + process, e);
                        }
                        addSupportFileInfo(files,
                                MessageFormat.format(getResources(request).getMessage("support.file.process.logs"), process.getId()), fileIncluded);
                    }
                    supportFiles.put("process." + rootProcessId + ".zip", createZip(processFiles));
                    tab.put("files", files);
                    tabs.add(tab);
                }
                rootObject.put("tabs", tabs);
                if (supportFiles.size() > 0) {
                    JSONArray files = new JSONArray();
                    addLogFile(files, supportFiles, "boot.log");
                    addLogFile(files, supportFiles, "server.log");
                    rootObject.put("files", files);
                }
                String supportFileName = null;
                byte[] supportFile = null;
                if (supportFiles.size() == 1) {
                    supportFileName = supportFiles.keySet().iterator().next();
                    supportFile = supportFiles.values().iterator().next();
                } else if (supportFiles.size() > 1) {
                    supportFileName = "support.files." + CalendarUtil.formatDate(new Date()) + ".zip";
                    supportFile = createZip(supportFiles);
                }
                if (supportFileName != null && supportFile != null) {
                    request.getSession().setAttribute(supportFileName, supportFile);
                    rootObject.put("downloadUrl", "/wfe/getSessionFile.do?fileName=" + supportFileName);
                    rootObject.put("downloadTitle", getResources(request).getMessage("support.files.download"));
                    rootObject.put("supportUrl", getResources(request).getMessage("support.url"));
                    rootObject.put("supportTitle", getResources(request).getMessage("support.title"));
                }
            } else {
                rootObject.put(HTML, "Unknown action: " + action);
                log.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            log.error("", e);
            rootObject.put(HTML, String.format(getResources(request).getMessage("unknown.exception"), e));
        }
        try {
            OutputStream os = response.getOutputStream();
            os.write(rootObject.toJSONString().getBytes(Charsets.UTF_8));
            os.flush();
        } catch (Exception e) {
            log.error("Unable to write ajax output", e);
        }
        return null;
    }

    private void addLogFile(JSONArray files, Map<String, byte[]> supportFiles, String fileName) throws IOException {
        File file = new File(IOCommons.getLogDirPath(), fileName);
        if (!file.exists()) {
            log.error("No log file found at " + file.getAbsolutePath());
            return;
        }
        long serverLogSizeInMb = file.length() / (1024 * 1024) + 1;
        boolean logFileIncluded = serverLogSizeInMb <= 100;
        if (logFileIncluded) {
            supportFiles.put(fileName, Files.toByteArray(file));
        }
        addSupportFileInfo(files, fileName + " (" + serverLogSizeInMb + " Mb)", logFileIncluded);
    }

    private void addBotTabError(HttpServletRequest request, JSONArray tabs, Map<String, byte[]> supportFiles, BotTaskIdentifier botTaskIdentifier)
            throws IOException {
        JSONObject tab = new JSONObject();
        String type = botTaskIdentifier.getBotTask() != null ? "bottask" : "bot";
        tab.put("key", "b" + botTaskIdentifier.getUniqueId());
        tab.put("title", getResources(request).getMessage("errors." + type + ".name") + " " + botTaskIdentifier.getUniqueId());
        Map<String, byte[]> botFiles = Maps.newHashMap();
        JSONArray files = new JSONArray();
        String exceptions = "";
        if (botTaskIdentifier.getBotTask() != null) {
            exceptions += "\r\nbot task = " + botTaskIdentifier.getBotTask();
            botFiles.put(botTaskIdentifier.getBotTaskName(), botTaskIdentifier.getBotTask().getConfiguration());
            addSupportFileInfo(files, MessageFormat.format(getResources(request).getMessage("support.file.bottask.configuration"), botTaskIdentifier
                    .getBotTask().getName()), true);
        } else {
            exceptions += "\r\nbot = " + botTaskIdentifier.getBot();
        }
        Throwable throwable = ProcessExecutionErrors.getBotTaskConfigurationErrors().get(botTaskIdentifier);
        exceptions += "\r\n" + Throwables.getStackTraceAsString(throwable);
        botFiles.put("exception." + botTaskIdentifier.getUniqueId() + ".txt", exceptions.getBytes(Charsets.UTF_8));
        addSupportFileInfo(files, getResources(request).getMessage("support.file.exceptions"), true);
        supportFiles.put(type + "." + botTaskIdentifier.getUniqueId() + ".zip", createZip(botFiles));
        tab.put("files", files);
        tabs.add(tab);
    }

    private void initProcessHierarchy(User user, Map<Long, List<Long>> processHierarchies, Long processId) {
        try {
            Long parentProcessId = processId;
            while (true) {
                WfProcess process = Delegates.getExecutionService().getParentProcess(user, parentProcessId);
                if (process == null) {
                    break;
                }
                parentProcessId = process.getId();
            }
            List<Long> processIdsWithErrors = processHierarchies.get(parentProcessId);
            if (processIdsWithErrors == null) {
                processIdsWithErrors = Lists.newArrayList();
                processHierarchies.put(parentProcessId, processIdsWithErrors);
            }
            processIdsWithErrors.add(processId);
        } catch (Exception e) {
            log.warn("for process " + processId, e);
        }
    }

    private void addSupportFileInfo(JSONArray array, String fileInfo, boolean fileIncluded) {
        JSONObject object = new JSONObject();
        object.put("name", fileInfo);
        object.put("included", fileIncluded);
        array.add(object);
    }

    private byte[] createZip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipStream = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            if (entry.getValue() == null || entry.getValue().length == 0) {
                continue;
            }
            zipStream.putNextEntry(new ZipEntry(entry.getKey()));
            zipStream.write(entry.getValue());
        }
        zipStream.close();
        baos.flush();
        return baos.toByteArray();
    }

    private String getProcessLogs(HttpServletRequest request, User user, Long processId) {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(user, filter);
        int maxLevel = logs.getMaxSubprocessLevel();
        List<TR> rows = Lists.newArrayList();
        TD mergedEventDateTD = null;
        String mergedEventDateString = null;
        int mergedRowsCount = 0;
        for (ProcessLog log : logs.getLogs()) {
            String description;
            try {
                Object[] arguments = log.getPatternArguments();
                String format = getResources(request).getMessage("history.log." + log.getPatternName());
                Object[] substitutedArguments = HTMLUtils.substituteArguments(user, null, arguments);
                description = log.toString(format, substitutedArguments);
            } catch (Exception e) {
                description = log.toString();
            }
            TR tr = new TR();
            List<Long> processIds = logs.getSubprocessIds(log);
            for (Long subprocessId : processIds) {
                tr.addElement(new TD().addElement(subprocessId.toString()).setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            for (int i = processIds.size(); i < maxLevel; i++) {
                tr.addElement(new TD().addElement("").setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            String eventDateString = CalendarUtil.format(log.getCreateDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
            if (!Objects.equal(mergedEventDateString, eventDateString)) {
                if (mergedEventDateTD != null) {
                    mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
                }
                mergedRowsCount = 0;
                mergedEventDateTD = (TD) new TD().addElement(eventDateString).setClass(Resources.CLASS_LIST_TABLE_TD);
                mergedEventDateString = eventDateString;
                tr.addElement(mergedEventDateTD);
            } else {
                mergedRowsCount++;
            }
            tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }
        if (mergedEventDateTD != null) {
            mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
        }
        HeaderBuilder tasksHistoryHeaderBuilder = new ru.runa.wf.web.html.HistoryHeaderBuilder(maxLevel, getResources(request).getMessage(
                MessagesOther.LABEL_HISTORY_DATE.getKey()), getResources(request).getMessage(
                MessagesOther.LABEL_HISTORY_EVENT.getKey()));
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder).toString();
    }

}
