package ru.runa.wf.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wf.web.tag.BulkDeployDefinitionFormTag;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Created on 26.05.2014
 *
 * @struts:action path="/bulkDeployProcessDefinition" name="fileForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do"
 *                        redirect = "false"
 */
public class BulkDeployProcessDefinitionAction extends ActionBase {
    public static final String ACTION_PATH = "/bulkDeployProcessDefinition";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String paramType = request.getParameter("type");
        String paramTypeSelected = request.getParameter("typeSel");
        String paramTypeApplying = request.getParameter("typeApplying");

        Map<String, String> typeParamsHolder = new HashMap<String, String>();
        typeParamsHolder.put("type", paramType);
        typeParamsHolder.put("typeSel", paramTypeSelected);
        request.setAttribute("TypeAttributes", typeParamsHolder);

        List<String> processTypes;
        Map<String, UploadedFile> uploadedParFiles = (Map<String, UploadedFile>) request.getSession().getAttribute(BulkUploadServlet.UPLOADED_FILES);
        try {
            BatchPresentation presentation = BatchPresentationFactory.DEFINITIONS.createDefault();
            List<WfDefinition> existingDefinitions = Delegates.getDefinitionService().getProcessDefinitions(getLoggedUser(request), presentation,
                    false);
            Map<String, WfDefinition> existingDefinitionsMap = Maps.newHashMap();
            for (WfDefinition definition : existingDefinitions) {
                existingDefinitionsMap.put(definition.getName(), definition);
            }
            ProcessTypesIterator iter = new ProcessTypesIterator(getLoggedUser(request));
            if (paramTypeSelected == null || paramTypeSelected.equals("_default_type_")) {
                if (Strings.isNullOrEmpty(paramType)) {
                    if (BulkDeployDefinitionFormTag.TYPE_APPLYIES_TO_ALL_PROCESSES.equals(paramTypeApplying)) {
                        throw new ProcessDefinitionTypeNotPresentException();
                    } else {
                        for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                            String processDefinitionName = Files.getNameWithoutExtension(entry.getValue().getName());
                            if (!existingDefinitionsMap.containsKey(processDefinitionName)) {
                                throw new ProcessDefinitionTypeNotPresentException();
                            }
                        }
                    }
                }
                processTypes = Lists.newArrayList(paramType);
            } else {
                String[] selectedType = iter.getItem(Integer.parseInt(paramTypeSelected));
                processTypes = Lists.newArrayList(selectedType);
                if (!Strings.isNullOrEmpty(paramType)) {
                    processTypes.add(paramType);
                }
            }
            List<String> successKeys = new ArrayList<String>();
            for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                UploadedFile uploadedFile = entry.getValue();
                String existingDefinitionName = Files.getNameWithoutExtension(uploadedFile.getName());
                boolean redeploy = existingDefinitionsMap.containsKey(existingDefinitionName);
                if (!redeploy) {
                    try {
                        Delegates.getDefinitionService().deployProcessDefinition(getLoggedUser(request), uploadedFile.getContent(), processTypes);
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
                                BulkDeployDefinitionFormTag.TYPE_APPLYIES_TO_ALL_PROCESSES.equals(paramTypeApplying) ? processTypes : null);
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
