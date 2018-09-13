package ru.runa.wfe.graph.view;

import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.User;

/**
 * Operation to set starting process readable flag.
 */
@CommonsLog
public class ProcessDefinitionInfoVisitor extends NodeGraphElementVisitor {

    /**
     * Current subject.
     */
    private final User user;

    /**
     * Process definition cache.
     */
    private final IProcessDefinitionLoader loader;
    private final ParsedProcessDefinition definition;

    /**
     * Create instance of operation to set subprocess definition readable flag.
     */
    public ProcessDefinitionInfoVisitor(User user, ParsedProcessDefinition definition, IProcessDefinitionLoader loader) {
        this.user = user;
        this.definition = definition;
        this.loader = loader;
    }

    @Override
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {
        try {
            ParsedProcessDefinition parsedProcessDefinition = loader.getLatestDefinition(element.getSubprocessName());
            element.setSubprocessAccessible(hasReadPermission(parsedProcessDefinition));
            element.setSubprocessId(parsedProcessDefinition.getId());
        } catch (DefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
        }
    }

    @Override
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {
        if (element.isEmbedded()) {
            element.setSubprocessAccessible(true);
            element.setSubprocessId(definition.getId());
            ParsedSubprocessDefinition subprocessDefinition = definition.getEmbeddedSubprocessByNameNotNull(element.getSubprocessName());
            element.setEmbeddedSubprocessId(subprocessDefinition.getNodeId());
            element.setEmbeddedSubprocessGraphWidth(subprocessDefinition.getGraphConstraints()[2]);
            element.setEmbeddedSubprocessGraphHeight(subprocessDefinition.getGraphConstraints()[3]);
        } else {
            try {
                ParsedProcessDefinition parsedProcessDefinition = loader.getLatestDefinition(element.getSubprocessName());
                element.setSubprocessAccessible(hasReadPermission(parsedProcessDefinition));
                element.setSubprocessId(parsedProcessDefinition.getId());
            } catch (DefinitionDoesNotExistException e) {
                log.warn("ProcessDefinitionDoesNotExistException", e);
            }
        }
    }

    /**
     * Check READ permission on definition for current subject.
     * 
     * @param parsedProcessDefinition
     *            Process definition to check READ permission.
     * @return true, if current actor can read process definition and false otherwise.
     */
    private boolean hasReadPermission(ParsedProcessDefinition parsedProcessDefinition) {
        PermissionDAO permissionDAO = ApplicationContextFactory.getPermissionDAO();
        return permissionDAO.isAllowed(user, Permission.LIST, parsedProcessDefinition.getDeployment());
    }
}
