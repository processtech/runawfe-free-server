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
package ru.runa.report.web.tag;

import java.util.Iterator;
import java.util.List;

import ru.runa.common.web.HierarchyTypesIterator;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.service.ReportService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ReportTypesIterator implements Iterator<String[]> {

    private final HierarchyTypesIterator innerIterator;

    public ReportTypesIterator(User user) {
        innerIterator = new HierarchyTypesIterator(getAllTypes(user));
    }

    private static List<String[]> getAllTypes(User user) {
        ReportService reportService = Delegates.getReportService();
        BatchPresentation batchPresentation = BatchPresentationFactory.REPORTS.createNonPaged();
        List<ReportDto> definitions = reportService.getReportDefinitions(user, batchPresentation, false);
        return Lists.transform(definitions, new Function<ReportDto, String[]>() {

            @Override
            public String[] apply(ReportDto input) {
                return input.getCategories();
            }
        });
    }

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public String[] next() {
        return innerIterator.next();
    }

    // TODO remove this method. Action must not use this iterator - iterator may be changed before action execution (wrong type will be set).
    public String[] getItem(int idx) {
        return innerIterator.getItem(idx);
    }

    @Override
    public void remove() {
        innerIterator.remove();
    }
}
