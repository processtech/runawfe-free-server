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
package ru.runa.wfe.definition;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;

/**
 * Class presentation for process definition. Shows only last deployed process definition.
 *
 * @author Dofs
 * @since 4.0
 */
public class DefinitionClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.process_definition.name";
    public static final String DESCRIPTION = "batch_presentation.process_definition.description";
    public static final String VERSION = "batch_presentation.process_definition.version";
    public static final String TYPE = "batch_presentation.process_definition.process_type";
    public static final String CREATE_DATE = "batch_presentation.process_definition.create_date";
    public static final String CREATE_ACTOR = "batch_presentation.process_definition.create_actor";
    public static final String UPDATE_DATE = "batch_presentation.process_definition.update_date";
    public static final String UPDATE_ACTOR = "batch_presentation.process_definition.update_actor";
    public static final String SUBPROCESS_BINDING_DATE = "batch_presentation.process_definition.subprocess_binding_date";

    private static final ClassPresentation INSTANCE = new DefinitionClassPresentation();

    private DefinitionClassPresentation() {
        super(ProcessDefinition.class, classNameSQL + ".version=(select max(temp.version) from " + ProcessDefinition.class.getName() + " as temp where "
                + classNameSQL + ".name=temp.name)", false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode
                // get value/show in web getter parameters
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(ProcessDefinition.class, "name"), true, 1,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                        Permission.START, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDBSource(ProcessDefinition.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DescriptionProcessTDBuilder", new Object[] {}),
                new FieldDescriptor(TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(ProcessDefinition.class, "category"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTDBuilder", new Object[] {}),
                new FieldDescriptor(CREATE_DATE, Date.class.getName(), new DefaultDBSource(ProcessDefinition.class, "createDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionCreateDateTDBuilder", new Object[] {}),
                new FieldDescriptor(CREATE_ACTOR, Actor.class.getName(), new DefaultDBSource(ProcessDefinition.class, "createActor"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionCreateActorTDBuilder", new Object[] {}),
                new FieldDescriptor(UPDATE_DATE, Date.class.getName(), new DefaultDBSource(ProcessDefinition.class, "updateDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionUpdateDateTDBuilder", new Object[] {}),
                new FieldDescriptor(UPDATE_ACTOR, Actor.class.getName(), new DefaultDBSource(ProcessDefinition.class, "updateActor"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionUpdateActorTDBuilder", new Object[] {}),
                new FieldDescriptor(SUBPROCESS_BINDING_DATE, Date.class.getName(), new DefaultDBSource(ProcessDefinition.class, "subprocessBindingDate"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionSubprocessBindingDateTDBuilder", new Object[] {})
                        .setVisible(false) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
