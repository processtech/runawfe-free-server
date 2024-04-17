package ru.runa.wf.web.tag;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.PRE;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.Strong;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowDefinitionHistoryDiffAction;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processDefinitionFileAnnotationChanges")
public class ProcessDefinitionFileAnnotationChangesTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getStartElement() {
        Div content = new Div();
        String fileName = pageContext.getRequest().getParameter(ProcessDefinitionFileAnnotationFormTag.FILE_NAME_PARAMETER);
        if (fileName == null) {
            return content;
        }
        String limitAsString = pageContext.getRequest().getParameter(ProcessDefinitionFileAnnotationFormTag.LIMIT_PARAMETER);
        Integer limit = Strings.isNullOrEmpty(limitAsString) ? 0 : Integer.parseInt(limitAsString);
        Long processDefinitionId = Long.parseLong(pageContext.getRequest()
                .getParameter(ProcessDefinitionFileAnnotationFormTag.PROCESS_DEFINITION_ID_PARAMETER));
        String processDefinitionName = Delegates.getDefinitionService().getProcessDefinition(getUser(), processDefinitionId).getName();

        List<WfDefinition> versions = Delegates.getDefinitionService().getProcessDefinitionHistory(getUser(), processDefinitionId, limit);
        // versions отсортированы по убыванию; с ними будет удобней работать, если они будет отсортированы по возрастанию
        Collections.reverse(versions);

        if (versions.get(0).getVersion() != 1) {
            content.addElement(new BR());
            content.addElement(
                    new Div(Commons.getMessage("note_about_last_version_in_annotation", pageContext, 
                            new Object[] { versions.get(0).getVersion() }))
                                    .setStyle("font-style: italic;"));
        }

        content.addElement(new BR());

        Table resultTable = new Table();
        resultTable.setClass(Resources.CLASS_LIST_TABLE);
        content.addElement(resultTable);
        Map<Long, WfDefinition> versionToWfDefinition = new HashMap<>(versions.size());
        for (WfDefinition version : versions) {
            versionToWfDefinition.put(version.getVersion(), version);
        }
        BlameResult blameResult = gitBlame(fileName, versions);
        RawText resultContents = blameResult.getResultContents();
        int linesNumber = resultContents.size();
        String commonTextForVersionLinks = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.VERSION, pageContext);
        String commonTextAboutLoading = " " + MessagesProcesses.LABEL_LOADED.message(pageContext) + " ";
        String versionNumberAsStringForPreviousRow = null;
        TD versionInfo = null;
        TD rowNumber = null;
        TD textLine = null;
        String tagPreClass = "tagPreForTextLines";
        String tagDivClass = "tagDivForSpaceBetweenTextLines";
        for (int i = 0; i < linesNumber; i++) {
            String versionNumberAsString = blameResult.getSourceCommit(i).getFullMessage();
            if (versionNumberAsStringForPreviousRow != null && versionNumberAsStringForPreviousRow.equals(versionNumberAsString)) {
                rowNumber.addElement(new Div().setClass(tagDivClass));
                textLine.addElement(new Div().setClass(tagDivClass));
            } else {
                TR tableRow = new TR();
                resultTable.addElement(tableRow);
                versionInfo = new TD();
                versionInfo.setClass(Resources.CLASS_LIST_TABLE_TD);
                tableRow.addElement(versionInfo);
                WfDefinition version = versionToWfDefinition.get(Long.parseLong(versionNumberAsString));
                int versionIndex = versions.indexOf(version);
                WfDefinition previousVersion = versionIndex == 0 ? null : versions.get(versionIndex - 1);
                String linkToChangesInVersion = createLinkToChangesInVersion(previousVersion, version, processDefinitionName);
                A linkToChangesInVersionHTMLTag = new A(linkToChangesInVersion).setTarget("_blank");
                versionInfo.addElement(linkToChangesInVersionHTMLTag);
                Strong textForLinkToChangesInVersion = new Strong().addElement(commonTextForVersionLinks + Entities.NBSP + versionNumberAsString);
                linkToChangesInVersionHTMLTag.addElement(textForLinkToChangesInVersion);
                Span tooltip = new Span().addElement(
                        (version.getCreateActor() != null ? version.getCreateActor().getFullName() : "") + commonTextAboutLoading
                                + CalendarUtil.formatDateTime(version.getCreateDate()));
                textForLinkToChangesInVersion.addElement(tooltip.setClass("tooltip")).setClass("haveTooltip");
                rowNumber = new TD();
                rowNumber.setClass(Resources.CLASS_LIST_TABLE_TD);
                tableRow.addElement(rowNumber);
                textLine = new TD();
                textLine.setClass(Resources.CLASS_LIST_TABLE_TD);
                tableRow.addElement(textLine);
            }
            PRE rowNumberPRE = new PRE().addElement(Integer.toString(i + 1)); // PRE - как в textLine,
            // для выравнивания номеров строк и линий текста
            rowNumber.addElement(rowNumberPRE.setClass(tagPreClass));
            String escapedTextLine = StringEscapeUtils.escapeHtml(resultContents.getString(i));
            PRE textLinePRE = new PRE().addElement(escapedTextLine.length() == 0 ? " " : escapedTextLine); // пустые строки заменяем пробелом
            // для выравнивания с номерами строк, потому что пробел дает высоту, а отсутствие текста - нет
            textLine.addElement(textLinePRE.setClass(tagPreClass));
            versionNumberAsStringForPreviousRow = versionNumberAsString;
        }
        return content;
    }

    @Override
    protected ConcreteElement getEndElement() {
        return new StringElement();
    }

    private BlameResult gitBlame(String fileName, List<WfDefinition> versions) {
        String requestId = this.createUniqueRequestId();
        Path workingTreePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(Paths.get(requestId));
        File workingTree = workingTreePath.toFile();
        try {
            Files.createDirectories(workingTreePath);
            Git git = Git.init().setDirectory(workingTree).call();
            Path filePath = workingTreePath.resolve(fileName);
            for (WfDefinition version : versions) {
                Files.write(filePath, Delegates.getDefinitionService().getProcessDefinitionFile(getUser(), version.getId(), fileName));
                git.add().addFilepattern(fileName).call();
                git.commit().setMessage(version.getVersion().toString())
                        .setAuthor(version.getCreateActor() != null ? version.getCreateActor().getName() : "", "").call();
            }
            return git.blame().setFilePath(fileName).setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (Files.exists(workingTreePath)) {
                    FileUtils.deleteDirectory(workingTree);
                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private String createLinkToChangesInVersion(WfDefinition previousVersion, WfDefinition version, String processDefinitionName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ShowDefinitionHistoryDiffAction.DEFINITION_NAME, processDefinitionName);
        if (previousVersion != null) {
            parameters.put(ShowDefinitionHistoryDiffAction.VERSION_1, previousVersion.getVersion().toString());
        }
        parameters.put(ShowDefinitionHistoryDiffAction.VERSION_2, version.getVersion().toString());
        parameters.put(ShowDefinitionHistoryDiffAction.CONTEXT_LINES_COUNT, WebResources.getProcessDefinitionDiffContextLinesCount());
        return Commons.getActionUrl(ShowDefinitionHistoryDiffAction.ACTION, parameters, pageContext, PortletUrlType.Render);
    }

    private String createUniqueRequestId() {
        return this.pageContext.getSession().getId() + "_" + String.valueOf(System.currentTimeMillis());
    }
}
