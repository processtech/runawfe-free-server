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
package ru.runa.wfe.report;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.SubstringDBSource;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;

/**
 * Class presentation for reports.
 */
public class ReportClassPresentation extends ClassPresentation {
    private static final String PropertyTdBuilder = "ru.runa.common.web.html.PropertyTDBuilder";

    public static final String NAME = "reports.batch_presentation.name";
    public static final String DESCRIPTION = "reports.batch_presentation.description";
    public static final String TYPE = "reports.batch_presentation.type";

    private static final ClassPresentation INSTANCE = new ReportClassPresentation();

    private ReportClassPresentation() {
        super(ReportDefinition.class, null, false, new FieldDescriptor[] {
                new FieldDescriptor(NAME, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(ReportDefinition.class, "name"), true, 1,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, PropertyTdBuilder, new Object[] { ReportPermission.READ, "name" }),
                new FieldDescriptor(DESCRIPTION, AnywhereStringFilterCriteria.class.getName(), new SubstringDBSource(ReportDefinition.class,
                        "description"), true, FieldFilterMode.DATABASE, PropertyTdBuilder, new Object[] { ReportPermission.READ, "description" }),
                new FieldDescriptor(TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(ReportDefinition.class, "category"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTDBuilder", new Object[] {}, true) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
