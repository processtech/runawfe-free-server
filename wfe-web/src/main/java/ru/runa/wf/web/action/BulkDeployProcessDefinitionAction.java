package ru.runa.wf.web.action;

import com.google.common.io.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.CategoriesSelectUtils;
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
            val existingDefinitionsMap = new HashMap<String, WfDefinition>();
            for (WfDefinition definition : existingDefinitions) {
                existingDefinitionsMap.put(definition.getName(), definition);
            }
            val successKeys = new ArrayList<String>();
            List<String> categories = CategoriesSelectUtils.extract(request);
            for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                UploadedFile uploadedFile = entry.getValue();
                String existingDefinitionName = Files.getNameWithoutExtension(uploadedFile.getName());
                boolean redeploy = existingDefinitionsMap.containsKey(existingDefinitionName);
                if (!redeploy) {
                    try {
                        Delegates.getDefinitionService().deployProcessDefinition(getLoggedUser(request), uploadedFile.getContent(), categories, null);
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
                        Delegates.getDefinitionService().redeployProcessDefinition(
                                getLoggedUser(request),
                                definition.getId(),
                                uploadedFile.getContent(),
                                BulkDeployDefinitionFormTag.TYPE_APPLYIES_TO_ALL_PROCESSES.equals(paramTypeApplying) ? categories : null,
                                null
                        );
                        successKeys.add(entry.getKey());
                    } catch (Exception ex) {
                        addError(request, ex);
                    }
                }
            }
            for (String key : successKeys) {
                uploadedParFiles.remove(key);
            }
            if (uploadedParFiles.isEmpty()) {
                return getSuccessAction(mapping);
            } else {
                return getErrorForward(mapping);
            }
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
