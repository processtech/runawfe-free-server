package ru.runa.common.web.action;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.AdminScriptForm;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.service.client.AdminScriptClient;
import ru.runa.wfe.service.client.AdminScriptClient.Handler;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

public class AdminkitScriptsAction extends ActionBase {
    public static final String PATH = "/admin_scripts";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        AdminScriptForm form = (AdminScriptForm) actionForm;
        boolean ajaxRequest = form.isAjax();
        ActionMessages errors = new ActionMessages();
        try {
            String action = form.getAction();
            String fileName = form.getFileName();
            if ("get".equals(action)) {
                log.info("Getting script " + fileName);
                if (!Strings.isNullOrEmpty(fileName)) {
                    final ScriptingService scriptingService = Delegates.getScriptingService();
                    byte[] script = scriptingService.getScriptSource(fileName);
                    writeResponse(response, script);
                }
            } else if ("execute".equals(action)) {
                executeRun(request, response, getScript(form), ajaxRequest, errors);
            } else if ("executeUploadedScript".equals(action)) {
                File file = new File(IOCommons.getAdminkitScriptsDirPath() + fileName);
                byte[] script = FileUtils.readFileToByteArray(file);
                executeRun(request, response, script, ajaxRequest, errors);
            } else if ("save".equals(action)) {
                if (Strings.isNullOrEmpty(fileName)) {
                    throw new Exception("File name is required");
                }
                log.debug("Saving script " + fileName);
                final ScriptingService scriptingService = Delegates.getScriptingService();
                scriptingService.saveScript(fileName, getScript(form));
                log.info("Saved script " + fileName);
                if (!ajaxRequest) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("adminkit.script.save.success"));
                }
            } else if ("delete".equals(action)) {
                log.debug("Deleting script " + fileName);
                final ScriptingService scriptingService = Delegates.getScriptingService();
                scriptingService.deleteScript(fileName);
                if (!ajaxRequest) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("adminkit.script.delete.success"));
                }
            } else {
                log.error("Unknown action: " + action);
            }
        } catch (Throwable th) {
            log.error("admin scripts action", th);
            setErrors(ajaxRequest, errors, request, response, th.toString());
        }
        if (ajaxRequest) {
            return null;
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    private void executeRun(HttpServletRequest request, HttpServletResponse response, byte[] script, boolean ajaxRequest, ActionMessages errors)
            throws Exception
    {
        log.info("Executing script");
        final List<String> scriptErrors = new ArrayList<>();
        AdminScriptClient.run(getLoggedUser(request), script, new Handler() {

            @Override
            public void onUnknownOperations(String msg) {
                log.warn(msg);
                scriptErrors.add(msg);
            }

            @Override
            public void onTransactionException(Exception e) {
                scriptErrors.add(e.getMessage());
            }
        });
        if (ajaxRequest) {
            writeResponse(response, String.valueOf(scriptErrors.size()).getBytes());
        } else {
            if (scriptErrors.size() == 0) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("adminkit.script.execution.success"));
            } else {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("adminkit.script.execution.failed"));
            }
        }
    }

    private byte[] getScript(AdminScriptForm form) throws IOException {
        if (!Strings.isNullOrEmpty(form.getScript())) {
            return form.getScript().getBytes(Charsets.UTF_8);
        }
        if (form.getUploadFile() != null) {
            return form.getUploadFile().getFileData();
        }
        throw new InternalApplicationException("No script parameter found");
    }

    private void setErrors(boolean ajaxRequest, ActionMessages errors, HttpServletRequest request, HttpServletResponse response, String text) {
        if (ajaxRequest) {
            writeResponse(response, text.getBytes(Charsets.UTF_8));
        } else {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(text, false));
        }
    }

    private void writeResponse(HttpServletResponse response, byte[] data) {
        try {
            response.setContentType("text/xml");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            OutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
