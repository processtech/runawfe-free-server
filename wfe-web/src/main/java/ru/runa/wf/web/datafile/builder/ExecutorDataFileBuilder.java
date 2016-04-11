package ru.runa.wf.web.datafile.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Add action 'createActor' or 'createGroup' to xml file.
 * 
 * @author riven
 * 
 */
public class ExecutorDataFileBuilder implements DataFileBuilder {
    private final User user;

    public ExecutorDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        List<Group> groupsForCreating = new ArrayList<Group>();
        List<Actor> actorOnPermissions = new ArrayList<Actor>();
        List<Group> groupOnPermissions = new ArrayList<Group>();
        List<? extends Executor> executors = Delegates.getExecutorService().getExecutors(user, BatchPresentationFactory.EXECUTORS.createDefault());
        for (Executor executor : executors) {
            if (executor instanceof Actor) {
                actorOnPermissions.add((Actor) executor);
                populateActorElement(script, (Actor) executor);
                continue;
            }
            if (executor instanceof Group) {
                groupOnPermissions.add((Group) executor);
                populateGroupElement(script, (Group) executor);
                groupsForCreating.add((Group) executor);
                continue;
            }
        }

        for (Group group : groupsForCreating) {
            List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, group);
            populateExecutorsToGroup(script, group, actors);
        }

        new PermissionsDataFileBuilder(user, actorOnPermissions, "addPermissionsOnActor", true).build(zos, script);
        new PermissionsDataFileBuilder(user, groupOnPermissions, "addPermissionsOnGroup", true).build(zos, script);
    }

    private void populateActorElement(Document script, Actor actor) {
        Element element = script.getRootElement().addElement("createActor", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(actor.getName())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, actor.getName());
        }
        if (StringUtils.isNotEmpty(actor.getFullName())) {
            element.addAttribute(AdminScriptConstants.FULL_NAME_ATTRIBUTE_NAME, actor.getFullName());
        }
        if (StringUtils.isNotEmpty(actor.getDescription())) {
            element.addAttribute(AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME, actor.getDescription());
        }
        if (StringUtils.isNotEmpty(actor.getEmail())) {
            element.addAttribute(AdminScriptConstants.EMAIL_ATTRIBUTE_NAME, actor.getEmail());
        }
        if (StringUtils.isNotEmpty(actor.getPhone())) {
            element.addAttribute(AdminScriptConstants.PHONE_ATTRIBUTE_NAME, actor.getPhone());
        }
        if (actor.getCode() != null) {
            element.addAttribute(AdminScriptConstants.CODE_ATTRIBUTE_NAME, actor.getCode().toString());
        }
    }

    private void populateGroupElement(Document script, Group group) {
        Element element = script.getRootElement().addElement("createGroup", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(group.getName())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, group.getName());
        }
        if (StringUtils.isNotEmpty(group.getDescription())) {
            element.addAttribute(AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME, group.getDescription());
        }
    }

    private void populateExecutorsToGroup(Document script, Group group, List<Actor> actors) {
        if (actors != null && actors.size() > 0) {
            Element element = script.getRootElement().addElement("addExecutorsToGroup", XmlUtils.RUNA_NAMESPACE);
            if (StringUtils.isNotEmpty(group.getName())) {
                element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, group.getName());
            }
            for (Actor actor : actors) {
                Element subElement = element.addElement(AdminScriptConstants.EXECUTOR_ELEMENT_NAME, XmlUtils.RUNA_NAMESPACE);
                if (StringUtils.isNotEmpty(actor.getName())) {
                    subElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, actor.getName());
                }
            }
        }
    }
}
