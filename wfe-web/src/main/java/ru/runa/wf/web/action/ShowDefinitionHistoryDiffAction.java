package ru.runa.wf.web.action;

import com.cloudbees.diff.Diff;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/show_definitions_history_diff"
 */
public class ShowDefinitionHistoryDiffAction extends ActionBase {
    public static final String ACTION = "/show_definitions_history_diff";
    public static final String DEFINITION_NAME = "definitionName";
    public static final String NUM_CONTEXT_LINES = "numContextLines";
    private static final Set<String> EXTENSIONS = Sets.newHashSet("xml", "ftl", "quick", "html", "css", "js");

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String definitionName = request.getParameter(DEFINITION_NAME);
        String version1String = request.getParameter("version1");
        String version2String = request.getParameter("version2");
        int numContextLines = Integer.parseInt(request.getParameter(NUM_CONTEXT_LINES));
        if (version1String == null || version2String == null || version1String.equals(version2String)) {
            addMessage(request, new ActionMessage(MessagesProcesses.FAILED_VIEW_DIFFERENCES.getKey()));
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        WfDefinition definition1 = Delegates.getDefinitionService().getProcessDefinitionVersion(Commons.getUser(request.getSession()), definitionName, Long.valueOf(version1String));
        WfDefinition definition2 = Delegates.getDefinitionService().getProcessDefinitionVersion(Commons.getUser(request.getSession()), definitionName, Long.valueOf(version2String));
        ProcessDefinition processDefinition1 = Delegates.getDefinitionService().getParsedProcessDefinition(Commons.getUser(request.getSession()), definition1.getId());
        ProcessDefinition processDefinition2 = Delegates.getDefinitionService().getParsedProcessDefinition(Commons.getUser(request.getSession()), definition2.getId());
        Set<String> unsortedFileNames = new HashSet<>();
        unsortedFileNames.addAll(processDefinition1.getProcessFiles().keySet());
        unsortedFileNames.addAll(processDefinition2.getProcessFiles().keySet());
        List<String> sortedFileNames = new ArrayList<>(unsortedFileNames);
        Collections.sort(sortedFileNames);
        StringBuilder b = new StringBuilder();
        for (String fileName : sortedFileNames) {
            try {
                if (!EXTENSIONS.contains(Files.getFileExtension(fileName))) {
                    continue;
                }
                byte[] fileData1 = processDefinition1.getFileData(fileName);
                byte[] fileData2 = processDefinition2.getFileData(fileName);
                String content1 = fileData1 != null ? new String(fileData1, Charsets.UTF_8) : "";
                String content2 = fileData2 != null ? new String(fileData2, Charsets.UTF_8) : "";
                Diff diff = Diff.diff(new StringReader(content1), new StringReader(content2), true);
                if (diff.isEmpty()) {
                    continue;
                }
                String unifiedDiff = diff.toUnifiedDiff(version1String + "/" + fileName, version2String + "/" + fileName, //
                        new StringReader(content1), new StringReader(content2), numContextLines);
                String[] lines = unifiedDiff.split("\n", -1);
                for (String line : lines) {
                    boolean fileHeader = line.startsWith("+++") || line.startsWith("---");
                    String tdClassName;
                    if (fileHeader || line.startsWith("@")) {
                        tdClassName = "";
                    } else if (line.startsWith("+")) {
                        tdClassName = "added";
                    } else if (line.startsWith("-")) {
                        tdClassName = "deleted";
                    } else if (line.startsWith("\\")) {
                        tdClassName = "comment";
                    } else {
                        tdClassName = "unchanged";
                    }
                    b.append("<tr><td class=\"").append(tdClassName).append("\">");
                    b.append("<pre>");
                    if (fileHeader) {
                        b.append("<b>");
                    }
                    b.append(StringEscapeUtils.escapeHtml(line));
                    if (fileHeader) {
                        b.append("</b>");
                    }
                    b.append("</pre>");
                    b.append("</td></tr>");
                }
            } catch (Exception e) {
               log.error("diff", e);
                b.append("<tr><td class=\"error\">");
                b.append(fileName).append(": ");
                b.append(e.toString());
                b.append("</td></tr>");
            }
        }
        if (b.length() == 0) {
            addMessage(request, new ActionMessage(MessagesProcesses.LABEL_NO_DIFFERENCES_FOUND.getKey()));
        }
        String diffContent = "<table>" + b + "</table>";
        request.setAttribute("diffContent", diffContent);
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
