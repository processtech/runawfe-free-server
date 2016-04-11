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
package ru.runa.common.web;

import javax.security.auth.login.LoginException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.wf.web.VariablesFormatException;
import ru.runa.wf.web.action.DataFileNotPresentException;
import ru.runa.wf.web.action.ProcessDefinitionTypeNotPresentException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.LocalizableException;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.presentation.filter.FilterFormatException;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.validation.ValidationException;

import com.google.common.base.Throwables;

/**
 * Created 27.05.2005
 * 
 */
public class ActionExceptionHelper {
    private static final Log log = LogFactory.getLog(ActionExceptionHelper.class);

    public static void addProcessError(ActionMessages errors, Throwable e) {
        addException(errors, "processErrors", e);
    }

    public static void addException(ActionMessages errors, Throwable e) {
        addException(errors, ActionMessages.GLOBAL_MESSAGE, e);
    }

    private static void addException(ActionMessages errors, String key, Throwable e) {
        e = Throwables.getRootCause(e);
        errors.add(key, getActionMessage(e));
        // category set to DEBUG due to logging in EJB layer
        // it's logged anyway due to cause in web layer
        log.debug("action exception", e);
    }

    public static String getErrorMessage(Throwable e, PageContext pageContext) {
        ActionMessage actionMessage = getActionMessage(e);
        return Commons.getMessage(actionMessage.getKey(), pageContext, actionMessage.getValues());
    }

    public static ActionMessage getActionMessage(Throwable e) {
        ActionMessage actionMessage;
        if (e instanceof AuthenticationException || e instanceof LoginException || e instanceof AuthenticationExpiredException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_AUTHENTICATION);
        } else if (e instanceof AuthorizationException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_AUTHORIZATION);
        } else if (e instanceof WeakPasswordException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_PASSWORD_IS_WEAK);
        } else if (e instanceof ExecutorDoesNotExistException) {
            ExecutorDoesNotExistException exception = (ExecutorDoesNotExistException) e;
            if (exception.getExecutorClass().equals(Actor.class)) {
                actionMessage = new ActionMessage(Messages.EXCEPTION_ACTOR_DOES_NOT_EXISTS, exception.getExecutorName());
            } else if (exception.getExecutorClass().equals(Group.class)) {
                actionMessage = new ActionMessage(Messages.EXCEPTION_GROUP_DOES_NOT_EXISTS, exception.getExecutorName());
            } else {
                actionMessage = new ActionMessage(Messages.EXCEPTION_EXECUTOR_DOES_NOT_EXISTS, exception.getExecutorName());
            }
        } else if (e instanceof ExecutorAlreadyExistsException) {
            ExecutorAlreadyExistsException exception = (ExecutorAlreadyExistsException) e;
            actionMessage = new ActionMessage(Messages.EXCEPTION_EXECUTOR_ALREADY_EXISTS, exception.getExecutorName());
        } else if (e instanceof ExecutorParticipatesInProcessesException) {
            ExecutorParticipatesInProcessesException exception = (ExecutorParticipatesInProcessesException) e;
            actionMessage = new ActionMessage(Messages.EXCEPTION_EXECUTOR_PARTICIPATES_IN_PROCESSES, exception.getExecutorName(),
                    exception.getIdsInfo());
        } else if (e instanceof ProcessDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.ERROR_PROCESS_DOES_NOT_EXIST, e.getMessage());
        } else if (e instanceof DefinitionAlreadyExistException) {
            DefinitionAlreadyExistException exception = (DefinitionAlreadyExistException) e;
            actionMessage = new ActionMessage(Messages.ERROR_DEFINITION_ALREADY_EXISTS, exception.getName());
        } else if (e instanceof DefinitionDoesNotExistException) {
            DefinitionDoesNotExistException exception = (DefinitionDoesNotExistException) e;
            actionMessage = new ActionMessage(Messages.ERROR_DEFINITION_DOES_NOT_EXIST, exception.getName());
        } else if (e instanceof DefinitionFileDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_FILE_DOES_NOT_EXIST_ERROR, e.getMessage());
        } else if (e instanceof DefinitionArchiveFormatException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        } else if (e instanceof InvalidDefinitionException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_FILE_FORMAT_ERROR, ((InvalidDefinitionException) e).getDefinitionName(),
                    e.getMessage());
        } else if (e instanceof DefinitionNameMismatchException) {
            DefinitionNameMismatchException exception = (DefinitionNameMismatchException) e;
            actionMessage = new ActionMessage(Messages.ERROR_DEFINITION_NAME_MISMATCH, exception.getDeployedProcessDefinitionName(),
                    exception.getGivenProcessDefinitionName());
        } else if (e instanceof TaskDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.ERROR_TASK_DOES_NOT_EXIST);
        } else if (e instanceof SubstitutionDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.SUBSTITUTION_OUT_OF_DATE);
        } else if (e instanceof InvalidSessionException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_SESSION_INVALID);
        } else if (e instanceof FilterFormatException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_TABLE_VIEW_SETUP_FORMAT_INCORRECT);
        } else if (e instanceof ProcessDefinitionTypeNotPresentException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_DEFINITION_TYPE_NOT_PRESENT);
        } else if (e instanceof TaskAlreadyAcceptedException) {
            actionMessage = new ActionMessage(Messages.TASK_WAS_ALREADY_ACCEPTED, e.getMessage());
        } else if (e instanceof ParentProcessExistsException) {
            ParentProcessExistsException exc = (ParentProcessExistsException) e;
            actionMessage = new ActionMessage(Messages.PROCESS_HAS_SUPER_PROCESS, exc.getDefinitionName(), exc.getParentDefinitionName());
        } else if (e instanceof RelationDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_RELATION_GROUP_DOESNOT_EXISTS, e.getMessage());
        } else if (e instanceof RelationAlreadyExistException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_RELATION_GROUP_EXISTS, e.getMessage());
        } else if (e instanceof VariablesFormatException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_VARIABLE_FORMAT_ERROR, ((VariablesFormatException) e).getErrorFields());
        } else if (e instanceof DataFileNotPresentException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_DATAFILE_NOT_PRESENT);
        } else if (e instanceof ValidationException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_VALIDATION_ERROR);
        } else if (e instanceof LocalizableException) {
            actionMessage = new ActionMessage(e.getLocalizedMessage(), false);
        } else if (e instanceof InternalApplicationException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_UNKNOWN, e.getMessage());
        } else {
            String message = e.getMessage();
            if (message == null) {
                message = e.getClass().getName();
            }
            actionMessage = new ActionMessage(Messages.EXCEPTION_UNKNOWN, message);
        }
        return actionMessage;
    }
}
