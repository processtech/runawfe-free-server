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

import ru.runa.wfe.presentation.*;


public class DefinitionChangesClassPresentation extends ClassPresentation {
    public static final String VERSION = "batch_presentation.process_definition_changes.version";
    public static final String DATE = "batch_presentation.process_definition_changes.date";
    public static final String AUTHOR = "batch_presentation.process_definition_changes.author";
    public static final String COMMENT = "batch_presentation.process_definition_changes.comment";

    private static final ClassPresentation INSTANCE = new DefinitionChangesClassPresentation();

    private DefinitionChangesClassPresentation() {
        super(Deployment.class, "", false, new FieldDescriptor[] {
                new FieldDescriptor(VERSION, Long.class.getName(), new DefaultDBSource(Deployment.class, "version"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionChangeVersionTDBuilder", new Object[] { }),
                new FieldDescriptor(DATE, Date.class.getName(), new DefaultDBSource(Deployment.class, "versionDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionChangeDateTDBuilder", new Object[] {}),
                new FieldDescriptor(AUTHOR, String.class.getName(), new SubstringDBSource(Deployment.class, "versionAuthor"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionChangeAuthorTDBuilder", new Object[] {}),
                new FieldDescriptor(COMMENT, String.class.getName(), new SubstringDBSource(Deployment.class, "versionComment"), false, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionChangeCommentTDBuilder", new Object[] {})
           });
  }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}