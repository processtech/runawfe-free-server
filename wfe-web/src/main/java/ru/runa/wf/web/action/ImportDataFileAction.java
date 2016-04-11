package ru.runa.wf.web.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.datafile.builder.DataFileBuilder;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * 
 * @author riven
 * @struts:action path="/importDataFileAction" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_system.do"
 * @struts.action-forward name="failure" path="/manage_system.do"
 */
public class ImportDataFileAction extends ActionBase {
    public static final String ACTION_PATH = "/importDataFileAction";
    public static final String UPLOAD_PARAM = "type";
    public static final String PASSWORD_PARAM = "passwordType";
    public static final String PASSWORD_VALUE_PARAM = "passwordValue";
    public static final String CLEAR_BEFORE_UPLOAD = "clearBeforeUpload";
    public static final String UPLOAD_ONLY = "uploadOnly";
    public static final String SET_PASSWORD = "setPassword";
    public static final String CLEAR_PASSWORD = "clearPassword";
    private static final String DEPLOY_PROCESS_DEFINITION_TAG_NAME = "deployProcessDefinition";
    private static final String FILE_ATTRIBUTE_NAME = "file";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileForm fileForm = (FileForm) form;
        byte[] archive = fileForm.getFile().getFileData();

        try {
            if (archive == null || archive.length == 0) {
                throw new DataFileNotPresentException();
            }

            boolean clearBeforeUpload = false;

            String paramType = request.getParameter(UPLOAD_PARAM);
            if (CLEAR_BEFORE_UPLOAD.equals(paramType)) {
                clearBeforeUpload = true;
            }

            String defaultPasswordValue = null;
            final String passwordParamType = request.getParameter(PASSWORD_PARAM);
            if (SET_PASSWORD.equals(passwordParamType)) {
                defaultPasswordValue = request.getParameter(PASSWORD_VALUE_PARAM);
            }

            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
            Map<String, byte[]> files = new HashMap<String, byte[]>();
            Map<String, byte[]> configs = new HashMap<String, byte[]>();
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] bytes = ByteStreams.toByteArray(zin);
                if (entry.getName().endsWith(".conf")) {
                    configs.put(entry.getName().substring(DataFileBuilder.PATH_TO_BOTTASK.length(), entry.getName().length()), bytes);
                } else {
                    files.put(entry.getName(), bytes);
                }
            }
            byte[] scriptXml = files.remove(DataFileBuilder.PATH_TO_XML);

            InputStream scriptInputStream = new ByteArrayInputStream(scriptXml);
            Document allDocument = XmlUtils.parseWithXSDValidation(scriptInputStream, "workflowScript.xsd");
            Element root = allDocument.getRootElement();
            byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(root, files);

            if (clearBeforeUpload) {
                List<WfProcess> wfProcesses = Delegates.getExecutionService().getProcesses(getLoggedUser(request),
                        BatchPresentationFactory.PROCESSES.createDefault());
                ProcessFilter processFilter = new ProcessFilter();
                for (WfProcess wfProcess : wfProcesses) {
                    processFilter.setId(wfProcess.getId());
                    Delegates.getExecutionService().removeProcesses(getLoggedUser(request), processFilter);
                }

                List<WfDefinition> definitions = Delegates.getDefinitionService().getProcessDefinitions(getLoggedUser(request),
                        BatchPresentationFactory.DEFINITIONS.createDefault(), false);
                for (WfDefinition definition : definitions) {
                    Delegates.getDefinitionService().undeployProcessDefinition(getLoggedUser(request), definition.getName(), null);
                }

                List<BotStation> botStations = Delegates.getBotService().getBotStations();
                for (BotStation botStation : botStations) {
                    List<Bot> bots = Delegates.getBotService().getBots(getLoggedUser(request), botStation.getId());
                    for (Bot bot : bots) {
                        List<BotTask> botTasks = Delegates.getBotService().getBotTasks(getLoggedUser(request), bot.getId());
                        for (BotTask botTask : botTasks) {
                            Delegates.getBotService().removeBotTask(getLoggedUser(request), botTask.getId());
                        }
                        Delegates.getBotService().removeBot(getLoggedUser(request), bot.getId());
                    }
                    Delegates.getBotService().removeBotStation(getLoggedUser(request), botStation.getId());
                }

                List<Relation> relations = Delegates.getRelationService().getRelations(getLoggedUser(request),
                        BatchPresentationFactory.RELATIONS.createDefault());
                for (Relation relation : relations) {
                    List<RelationPair> relationPairs = Delegates.getRelationService().getRelationPairs(getLoggedUser(request), relation.getName(),
                            BatchPresentationFactory.RELATION_PAIRS.createDefault());
                    for (RelationPair relationPair : relationPairs) {
                        Delegates.getRelationService().removeRelationPair(getLoggedUser(request), relationPair.getId());
                    }
                    Delegates.getRelationService().removeRelation(getLoggedUser(request), relation.getId());
                }

                List<? extends Executor> executors = Delegates.getExecutorService().getExecutors(getLoggedUser(request),
                        BatchPresentationFactory.EXECUTORS.createDefault());
                List<Long> ids = new ArrayList<Long>();
                for (Executor executor : executors) {
                    if (!ApplicationContextFactory.getPermissionDAO().isPrivilegedExecutor(executor)) {
                        ids.add(executor.getId());
                    }
                }
                Delegates.getExecutorService().remove(getLoggedUser(request), ids);
            }

            List<String> errors = Delegates.getScriptingService().executeAdminScriptSkipError(getLoggedUser(request), scriptXml,
                    processDefinitionsBytes, configs, defaultPasswordValue);
            if (errors != null && errors.size() > 0) {
                for (String error : errors) {
                    addError(request, new Exception(error));
                }
                addMessage(request, new ActionMessage(Messages.EXECUTOR_STATE_DONT_UPDATE));
                return mapping.findForward(Resources.FORWARD_FAILURE);
            }
            addMessage(request, new ActionMessage(Messages.IMPORT_DATA_SUCCESS));
            addMessage(request, new ActionMessage(Messages.EXECUTOR_STATE_DONT_UPDATE));
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }

    }

    private static byte[][] readProcessDefinitionsToByteArrays(Element element, Map<String, byte[]> files) throws IOException {
        String[] fileNames = readProcessDefinitionFileNames(element);
        byte[][] processDefinitionsBytes = new byte[fileNames.length][];
        for (int i = 0; i < fileNames.length; i++) {
            processDefinitionsBytes[i] = files.get(fileNames[i]);
        }
        return processDefinitionsBytes;
    }

    private static String[] readProcessDefinitionFileNames(Element element) {
        List<Element> elements = element.elements(DEPLOY_PROCESS_DEFINITION_TAG_NAME);
        List<String> fileNames = Lists.newArrayList();
        for (Element e : elements) {
            fileNames.add(e.attributeValue(FILE_ATTRIBUTE_NAME));
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }
}
