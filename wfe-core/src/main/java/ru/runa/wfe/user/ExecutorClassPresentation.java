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
package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Created on 22.10.2005
 */
public class ExecutorClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.executor.name";
    public static final String FULL_NAME = "batch_presentation.executor.full_name";
    public static final String DESCRIPTION = "batch_presentation.executor.description";
    public static final String TYPE = "batch_presentation.executor.type";

    private static final ClassPresentation INSTANCE = new ExecutorClassPresentation();

    private ExecutorClassPresentation() {
        super(Executor.class, "", true, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode get
                // value/show in web getter param
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(Executor.class, "name"), true, 1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "name" }),
                new FieldDescriptor(FULL_NAME, String.class.getName(), new DefaultDBSource(Executor.class, "fullName"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "fullName" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDBSource(Executor.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "description" }),
                new FieldDescriptor(TYPE, String.class.getName(), new DefaultDBSource(Executor.class, "class"), false, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "class" }).setShowable(false) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
