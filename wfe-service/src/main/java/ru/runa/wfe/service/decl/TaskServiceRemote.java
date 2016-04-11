package ru.runa.wfe.service.decl;

import javax.ejb.Remote;

import ru.runa.wfe.service.TaskService;

@Remote
public interface TaskServiceRemote extends TaskService {
}