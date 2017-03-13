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
import java.util.Set;
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
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
            if ("getSystemError".equals(action)) {
                for (SystemError systemError : Delegates.getSystemService().getSystemErrors(getLoggedUser(request))) {
                    if (Objects.equal(systemError.getMessage(), form.getName())) {
                        rootObject.put(HTML, systemError.getStackTrace());
                        break;
                    }
                }
            } else if ("deleteSystemError".equals(action)) {
                Errors.removeSystemError(form.getName());
            } else if ("getProcessError".equals(action)) {
                List<ProcessError> processErrors = Delegates.getSystemService().getProcessErrors(getLoggedUser(request), form.getId());
                ProcessErrorType type = ProcessErrorType.valueOf(request.getParameter("type"));
                ProcessError patternError = new ProcessError(type, form.getId(), form.getName());
                for (ProcessError processError : processErrors) {
                    if (Objects.equal(processError, patternError)) {
                        String html = "<form id='supportForm'>";
                        html += "<input type='hidden' name='processId' value='" + form.getId() + "' />";
                        html += "</form>";
                        html += processError.getStackTrace() != null ? processError.getStackTrace() : processError.getMessage();
                        rootObject.put(HTML, html);
                    }
                }
            } else if ("deleteProcessError".equals(action)) {
                ProcessErrorType type = ProcessErrorType.valueOf(request.getParameter("type"));
                ProcessError patternError = new ProcessError(type, form.getId(), form.getName());
                Errors.removeProcessError(patternError);
            } else if ("showSupportFiles".equals(action)) {
                boolean fileIncluded = false;
                request.getParameter("botId");
                User user = getLoggedUser(request);
                JSONArray tabs = new JSONArray();
                Map<String, byte[]> supportFiles = Maps.newHashMap();
                // privileges are required for successful operation!
                Map<Long, List<Long>> processHierarchies = Maps.newHashMap();
                if (request.getParameter("processId") != null) {
                    Long processId = Long.parseLong(request.getParameter("processId"));
                    initProcessHierarchy(user, processHierarchies, processId);
                } else {
                    Set<Long> processIds = Sets.newHashSet();
                    for (ProcessError processError : Delegates.getSystemService().getAllProcessErrors(getLoggedUser(request))) {
                        processIds.add(processError.getProcessId());
                    }
                    for (Long processId : processIds) {
                        initProcessHierarchy(user, processHierarchies, processId);
                    }
                    int index = 1;
                    for (SystemError systemError : Delegates.getSystemService().getSystemErrors(getLoggedUser(request))) {
                        addSystemError(request, tabs, supportFiles, index++, systemError);
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
                        StringBuilder exceptions = new StringBuilder();
                        List<ProcessError> processErrors = Delegates.getSystemService().getProcessErrors(getLoggedUser(request), processId);
                        for (ProcessError processError : processErrors) {
                            exceptions.append("\r\n---------------------------------------------------------------");
                            exceptions.append("\r\n").append(CalendarUtil.formatDateTime(processError.getOccurredDate()))
                                    .append(" ").append(processError.getNodeId()).append("/")
                                    .append(processError.getNodeName())
                                    .append("\r\n").append(processError.getStackTrace());
                        }
                        processFiles.put("exceptions." + processId + ".txt", exceptions.toString().getBytes(Charsets.UTF_8));
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

    private void addSystemError(HttpServletRequest request, JSONArray tasks, Map<String, byte[]> supportFiles, int index, SystemError systemError)
            throws IOException {
        JSONObject tab = new JSONObject();
        tab.put("key", String.valueOf(index));
        tab.put("title", systemError.getMessage());
        JSONArray files = new JSONArray();
        addSupportFileInfo(files, getResources(request).getMessage("support.file.exceptions"), true);
        supportFiles.put("exception." + index + ".txt", systemError.getStackTrace().getBytes(Charsets.UTF_8));
        tab.put("files", files);
        tasks.add(tab);
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
            String eventDateString = CalendarUtil.format(log.getCreateDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT_STR);
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
                MessagesOther.LABEL_HISTORY_DATE.getKey()), getResources(request).getMessage(MessagesOther.LABEL_HISTORY_EVENT.getKey()));
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder).toString();
    }

}
