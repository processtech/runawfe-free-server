package ru.runa.wf.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HierarchyTypeSelectUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wf.web.tag.BulkDeployDefinitionFormTag;
import ru.runa.wf.web.tag.RedeployDefinitionFormTag;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Created on 26.05.2014
 * 
 * @struts:action path="/bulkDeployProcessDefinition" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do" redirect = "false"
 */
public class BulkDeployProcessDefinitionAction extends ActionBase {
    public static final String ACTION_PATH = "/bulkDeployProcessDefinition";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String paramTypeApplying = request.getParameter(RedeployDefinitionFormTag.TYPE_UPDATE_CURRENT_VERSION);

        Map<String, UploadedFile> uploadedParFiles = BulkUploadServlet.getUploadedFilesMap(request);
        try {
            BatchPresentation presentation = BatchPresentationFactory.DEFINITIONS.createDefault();
            List<WfDefinition> existingDefinitions = Delegates.getDefinitionService().getProcessDefinitions(getLoggedUser(request), presentation,
                    false);
            Map<String, WfDefinition> existingDefinitionsMap = Maps.newHashMap();
            for (WfDefinition definition : existingDefinitions) {
                existingDefinitionsMap.put(definition.getName(), definition);
            }
            boolean allowEmptyType = !BulkDeployDefinitionFormTag.TYPE_APPLYIES_TO_ALL_PROCESSES.equals(paramTypeApplying);
            List<String> fullType = HierarchyTypeSelectUtils.extractSelectedType(request);
            if (HierarchyTypeSelectUtils.isEmptyType(fullType) && !allowEmptyType) {
                throw new ProcessDefinitionTypeNotPresentException();
            }
            if (HierarchyTypeSelectUtils.isEmptyType(fullType)) {
                for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                    String processDefinitionName = Files.getNameWithoutExtension(entry.getValue().getName());
                    if (!existingDefinitionsMap.containsKey(processDefinitionName)) {
                        throw new ProcessDefinitionTypeNotPresentException();
                    }
                }
            }
            List<String> successKeys = new ArrayList<String>();
            for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                UploadedFile uploadedFile = entry.getValue();
                String existingDefinitionName = Files.getNameWithoutExtension(uploadedFile.getName());
                boolean redeploy = existingDefinitionsMap.containsKey(existingDefinitionName);
                if (!redeploy) {
                    try {
                        Delegates.getDefinitionService().deployProcessDefinition(getLoggedUser(request), uploadedFile.getContent(), fullType);
                        successKeys.add(entry.getKey());
                    } catch (DefinitionAlreadyExistException e) {
                        existingDefinitionName = e.getName();
                        redeploy = true;
                    } catch (Exception e) {
                        addError(request, e);
                    }
                }
                if (redeploy) {
                    try {
                        WfDefinition definition = existingDefinitionsMap.get(existingDefinitionName);
                        Delegates.getDefinitionService().redeployProcessDefinition(getLoggedUser(request), definition.getId(),
                                uploadedFile.getContent(),
                                BulkDeployDefinitionFormTag.TYPE_APPLYIES_TO_ALL_PROCESSES.equals(paramTypeApplying) ? fullType : null);
                        successKeys.add(entry.getKey());
                    } catch (Exception ex) {
                        addError(request, ex);
                    }
                }
            }
            for (String key : successKeys) {
                if (uploadedParFiles.containsKey(key)) {
                    uploadedParFiles.remove(key);
                }
            }
            return getSuccessAction(mapping);
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(mapping);
        }
    }

    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    protected ActionForward getErrorForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }

}
