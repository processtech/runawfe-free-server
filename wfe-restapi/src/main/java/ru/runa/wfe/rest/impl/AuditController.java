package ru.runa.wfe.rest.impl;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.ProcessLogDto;
import ru.runa.wfe.rest.dto.ProcessLogMapper;
import ru.runa.wfe.rest.dto.SystemLogDto;
import ru.runa.wfe.rest.dto.SystemLogMapper;
import java.util.List;

@RestController
@RequestMapping("/audit/")
@Transactional
public class AuditController {

    @Autowired
    private AuditLogic auditLogic;

    @GetMapping("{id}")
    public PagedList<ProcessLogDto> getProcessLogs(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<BaseProcessLog> logs = auditLogic.getProcessLogs(authUser.getUser(), new ProcessLogFilter(id)).getLogs();
        return new PagedList<>(logs.size(), Mappers.getMapper(ProcessLogMapper.class).map(logs));
    }

    @PostMapping("system")
    public PagedList<SystemLogDto> getSystemLogs(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        List<SystemLog> logs = auditLogic.getSystemLogs(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.SYSTEM_LOG));
        return new PagedList<>(logs.size(), Mappers.getMapper(SystemLogMapper.class).map(logs));
    }
}
