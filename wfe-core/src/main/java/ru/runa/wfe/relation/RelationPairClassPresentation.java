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
package ru.runa.wfe.relation;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;

public class RelationPairClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.relation.name";
    public static final String EXECUTOR_FROM = "batch_presentation.relation.executor_from";
    public static final String EXECUTOR_TO = "batch_presentation.relation.executor_to";

    private static final ClassPresentation INSTANCE = new RelationPairClassPresentation();

    private RelationPairClassPresentation() {
        super(RelationPair.class, "", false, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(RelationPair.class, "relation.name"), true,
                        FieldFilterMode.DATABASE, FieldState.HIDDEN),
                new FieldDescriptor(EXECUTOR_FROM, String.class.getName(), new DefaultDBSource(RelationPair.class, "left.name"), true, 1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.af.web.html.RelationFromTDBuilder", null),
                new FieldDescriptor(EXECUTOR_TO, String.class.getName(), new DefaultDBSource(RelationPair.class, "right.name"), true, 2, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.af.web.html.RelationToTDBuilder", null) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
