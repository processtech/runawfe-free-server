package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/*
 * Created on 10.08.2004
 */
public class ExecutorServiceDelegate extends Ejb3Delegate implements ExecutorService {

    public ExecutorServiceDelegate() {
        super(ExecutorService.class);
    }

    private ExecutorService getExecutorService() {
        return (ExecutorService) getService();
    }

    @Override
    public <T extends Executor> T create(User user, T executor) {
        try {
            return getExecutorService().create(user, executor);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void remove(User user, List<Long> ids) {
        try {
            getExecutorService().remove(user, ids);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void update(User user, Executor executor) {
        try {
            getExecutorService().update(user, executor);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<? extends Executor> getExecutors(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutorService().getExecutors(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<? extends Executor> getNotTemporaryExecutors(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutorService().getNotTemporaryExecutors(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getExecutorsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutorService().getExecutorsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getNotTemporaryExecutorsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutorService().getNotTemporaryExecutorsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Actor getActorCaseInsensitive(String login) {
        try {
            return getExecutorService().getActorCaseInsensitive(login);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public <T extends Executor> T getExecutorByName(User user, String name) {
        try {
            return (T) getExecutorService().getExecutorByName(user, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) {
        try {
            getExecutorService().addExecutorsToGroup(user, executorIds, groupId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) {
        try {
            getExecutorService().addExecutorToGroups(user, executorId, groupIds);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) {
        try {
            getExecutorService().removeExecutorsFromGroup(user, executorIds, groupId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        try {
            getExecutorService().removeExecutorFromGroups(user, executorId, groupIds);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        try {
            return getExecutorService().getGroupChildren(user, group, batchPresentation, isExclude);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        try {
            return getExecutorService().getGroupChildrenCount(user, group, batchPresentation, isExclude);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Actor> getGroupActors(User user, Group group) {
        try {
            return getExecutorService().getGroupActors(user, group);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        try {
            return getExecutorService().getExecutorGroups(user, executor, batchPresentation, isExclude);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        try {
            return getExecutorService().getExecutorGroupsCount(user, executor, batchPresentation, isExclude);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setPassword(User user, Actor actor, String password) {
        try {
            getExecutorService().setPassword(user, actor, password);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setStatus(User user, Actor actor, boolean isActive) {
        try {
            getExecutorService().setStatus(user, actor, isActive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public <T extends Executor> T getExecutor(User user, Long id) {
        try {
            return (T) getExecutorService().getExecutor(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Actor getActorByCode(User user, Long code) {
        try {
            return getExecutorService().getActorByCode(user, code);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isExecutorInGroup(User user, Executor executor, Group group) {
        try {
            return getExecutorService().isExecutorInGroup(user, executor, group);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isExecutorExist(User user, String executorName) {
        try {
            return getExecutorService().isExecutorExist(user, executorName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        try {
            return getExecutorService().getAllExecutorsFromGroup(user, group);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isAdministrator(User user) {
        try {
            return getExecutorService().isAdministrator(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
