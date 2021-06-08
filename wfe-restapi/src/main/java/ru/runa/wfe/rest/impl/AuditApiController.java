package ru.runa.wfe.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import java.util.List;

@RestController
@RequestMapping("/audit/")
@Transactional
public class AuditApiController {

    @Autowired
    private AuditLogic auditLogic;

    @GetMapping("process/{processId}")
    public ProcessLogs getProcessLogs(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long processId) {
        return auditLogic.getProcessLogs(authUser.getUser(), new ProcessLogFilter(processId));
    }

    @PostMapping("system")
    public List<SystemLog> getSystemLogs(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        return auditLogic.getSystemLogs(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.SYSTEM_LOG));
    }
}
