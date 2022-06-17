package ru.runa.wfe.rest.impl;

import java.util.List;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.WfeProcessLogFilterMapper;
import ru.runa.wfe.rest.converter.WfeProcessLogMapper;
import ru.runa.wfe.rest.converter.WfeSystemLogMapper;
import ru.runa.wfe.rest.converter.WfeVariablesSnapshotMapper;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeProcessLog;
import ru.runa.wfe.rest.dto.WfeProcessLogFilter;
import ru.runa.wfe.rest.dto.WfeSystemLog;
import ru.runa.wfe.rest.dto.WfeVariablesSnapshot;
import ru.runa.wfe.var.logic.VariableLogic;

@RestController
@RequestMapping("/audit/")
@Transactional
public class AuditController {

    @Autowired
    private AuditLogic auditLogic;
    @Autowired
    private VariableLogic variableLogic;

    @PostMapping("system")
    public WfePagedList<WfeSystemLog> getSystemLogs(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        List<SystemLog> logs = auditLogic.getSystemLogs(authUser.getUser(), filter.toBatchPresentation(ClassPresentationType.SYSTEM_LOG));
        return new WfePagedList<>(logs.size(), Mappers.getMapper(WfeSystemLogMapper.class).map(logs));
    }

    @PostMapping("process/{id}")
    public List<WfeProcessLog> getProcessLogs(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody WfeProcessLogFilter request) {
        ProcessLogFilter filter = Mappers.getMapper(WfeProcessLogFilterMapper.class).map(request);
        filter.setProcessId(id);
        List<BaseProcessLog> logs = auditLogic.getProcessLogs(authUser.getUser(), filter).getLogs();
        return Mappers.getMapper(WfeProcessLogMapper.class).map(logs);
    }

    @PostMapping("process/{id}/variables/snapshot")
    public WfeVariablesSnapshot getProcessVariablesSnapshot(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody WfeProcessLogFilter request) {
        ProcessLogFilter filter = Mappers.getMapper(WfeProcessLogFilterMapper.class).map(request);
        filter.setProcessId(id);
        return Mappers.getMapper(WfeVariablesSnapshotMapper.class).map(variableLogic.getHistoricalVariables(authUser.getUser(), filter));
    }

    @GetMapping("process/{id}/variables/snapshot")
    public WfeVariablesSnapshot getProcessVariablesSnapshot(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) Long taskId) {
        return Mappers.getMapper(WfeVariablesSnapshotMapper.class).map(variableLogic.getHistoricalVariables(authUser.getUser(), id, taskId));
    }

}
