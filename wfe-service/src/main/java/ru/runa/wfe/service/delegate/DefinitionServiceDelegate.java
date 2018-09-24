/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service.delegate;

import java.util.Date;
import java.util.List;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Provides simplified access to local ParsedProcessDefinition. Created on 28.09.2004
 */
public class DefinitionServiceDelegate extends Ejb3Delegate implements DefinitionService {

    public DefinitionServiceDelegate() {
        super(DefinitionService.class);
    }

    private DefinitionService getDefinitionService() {
        return getService();
    }

    @Override
    public WfDefinition deployProcessDefinition(User user, byte[] archive, List<String> categories, Integer secondsBeforeArchiving) {
        try {
            return getDefinitionService().deployProcessDefinition(user, archive, categories, secondsBeforeArchiving);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfDefinition redeployProcessDefinition(User user, Long processDefinitionVersionId, byte[] processArchive, List<String> categories,
            Integer secondsBeforeArchiving) {
        try {
            return getDefinitionService().redeployProcessDefinition(user, processDefinitionVersionId, processArchive, categories,
                    secondsBeforeArchiving);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfDefinition updateProcessDefinition(User user, Long processDefinitionVersionId, byte[] processArchive) {
        try {
            return getDefinitionService().updateProcessDefinition(user, processDefinitionVersionId, processArchive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setProcessDefinitionSubprocessBindingDate(User user, Long processDefinitionVersionId, Date date) throws DefinitionDoesNotExistException {
        try {
            getDefinitionService().setProcessDefinitionSubprocessBindingDate(user, processDefinitionVersionId, date);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        try {
            return getDefinitionService().getProcessDefinitions(user, batchPresentation, enablePaging);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getDefinitionService().getProcessDefinitionsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        try {
            return getDefinitionService().getDeployments(user, batchPresentation, enablePaging);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        try {
            return getDefinitionService().getLatestProcessDefinition(user, definitionName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfDefinition getProcessDefinitionVersion(User user, String definitionName, Long definitionVersion) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getProcessDefinitionVersion(user, definitionName, definitionVersion);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfDefinition getProcessDefinition(User user, Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getProcessDefinition(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getParsedProcessDefinition(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfNode getNode(User user, Long processDefinitionVersionId, String nodeId) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getNode(user, processDefinitionVersionId, nodeId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void undeployProcessDefinition(User user, String definitionName, Long version) {
        try {
            getDefinitionService().undeployProcessDefinition(user, definitionName, version);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Interaction getStartInteraction(User user, Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getStartInteraction(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Interaction getTaskNodeInteraction(User user, Long processDefinitionVersionId, String nodeId) {
        try {
            return getDefinitionService().getTaskNodeInteraction(user, processDefinitionVersionId, nodeId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] getProcessDefinitionFile(User user, Long processDefinitionVersionId, String fileName) {
        try {
            return getDefinitionService().getProcessDefinitionFile(user, processDefinitionVersionId, fileName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] getProcessDefinitionGraph(User user, Long processDefinitionVersionId, String subprocessId) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getProcessDefinitionGraph(user, processDefinitionVersionId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<SwimlaneDefinition> getSwimlaneDefinitions(User user, Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getSwimlaneDefinitions(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<UserType> getUserTypes(User user, Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getUserTypes(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public UserType getUserType(User user, Long processDefinitionVersionId, String name) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getUserType(user, processDefinitionVersionId, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<VariableDefinition> getVariableDefinitions(User user, Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getVariableDefinitions(user, processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public VariableDefinition getVariableDefinition(User user, Long processDefinitionVersionId, String variableName) throws DefinitionDoesNotExistException {
        try {
            return getDefinitionService().getVariableDefinition(user, processDefinitionVersionId, variableName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long processDefinitionVersionId, String subprocessId) {
        try {
            return getDefinitionService().getProcessDefinitionGraphElements(user, processDefinitionVersionId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        try {
            return getDefinitionService().getProcessDefinitionHistory(user, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<ProcessDefinitionChange> getChanges(Long processDefinitionVersionId) {
        try {
            return getDefinitionService().getChanges(processDefinitionVersionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<ProcessDefinitionChange> getLastChanges(Long processDefinitionVersionId, Long n) {
        try {
            return getDefinitionService().getLastChanges(processDefinitionVersionId, n);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        try {
            return getDefinitionService().findChanges(definitionName, version1, version2);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
