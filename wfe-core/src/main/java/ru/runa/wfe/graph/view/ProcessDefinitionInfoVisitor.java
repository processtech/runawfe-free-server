package ru.runa.wfe.graph.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.User;

/**
 * Operation to set starting process readable flag.
 */
public class ProcessDefinitionInfoVisitor extends NodeGraphElementVisitor {
    private static final Log log = LogFactory.getLog(ProcessDefinitionInfoVisitor.class);

    /**
     * Current subject.
     */
    private final User user;

    /**
     * Process definition cache.
     */
    private final ProcessDefinitionLoader processDefinitionLoader;
    private final ProcessDefinition definition;

    /**
     * Create instance of operation to set subprocess definition readable flag.
     * 
     * @param subject
     *            Current subject.
     * @param jbpmContext
     *            {@link JbpmContext} to get jbpm data.
     * @param loader
     *            Process definition loader.
     */
    public ProcessDefinitionInfoVisitor(User user, ProcessDefinition definition, ProcessDefinitionLoader loader) {
        this.user = user;
        this.definition = definition;
        this.processDefinitionLoader = loader;
    }

    @Override
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.getLatestDefinition(element.getSubprocessName());
            element.setSubprocessAccessible(hasReadPermission(processDefinition));
            element.setSubprocessId(processDefinition.getId());
        } catch (DefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
        }
    }

    @Override
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {
        if (element.isEmbedded()) {
            element.setSubprocessAccessible(true);
            element.setSubprocessId(definition.getId());
            SubprocessDefinition subprocessDefinition = definition.getEmbeddedSubprocessByNameNotNull(element.getSubprocessName());
            element.setEmbeddedSubprocessId(subprocessDefinition.getNodeId());
            element.setEmbeddedSubprocessGraphWidth(subprocessDefinition.getGraphConstraints()[2]);
            element.setEmbeddedSubprocessGraphHeight(subprocessDefinition.getGraphConstraints()[3]);
        } else {
            try {
                ProcessDefinition processDefinition = processDefinitionLoader.getLatestDefinition(element.getSubprocessName());
                element.setSubprocessAccessible(hasReadPermission(processDefinition));
                element.setSubprocessId(processDefinition.getId());
            } catch (DefinitionDoesNotExistException e) {
                log.warn("ProcessDefinitionDoesNotExistException", e);
            }
        }
    }

    /**
     * Check READ permission on definition for current subject.
     * 
     * @param processDefinition
     *            Process definition to check READ permission.
     * @return true, if current actor can read process definition and false otherwise.
     */
    private boolean hasReadPermission(ProcessDefinition processDefinition) {
        PermissionDao permissionDao = ApplicationContextFactory.getPermissionDao();
        return permissionDao.isAllowed(user, Permission.LIST, processDefinition.getDeployment());
    }
}
