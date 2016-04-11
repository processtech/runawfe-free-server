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
package ru.runa.wf.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ProcessTypesIterator implements Iterator<String[]> {

    private final List<String[]> processTypes = new ArrayList<String[]>();
    private int curIdx = 0;

    public ProcessTypesIterator(User user) {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        List<WfDefinition> definitions = definitionService.getProcessDefinitions(user, batchPresentation, false);
        SortedSet<String[]> processTypesSet = new TreeSet<String[]>(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int length = (o1.length > o2.length ? o2.length : o1.length);
                for (int i = 0; i < length; ++i) {
                    int compareResult = o1[i].compareTo(o2[i]);
                    if (compareResult == 0) {
                        continue;
                    }
                    return compareResult;
                }
                if (o1.length > length) {
                    return 1;
                }
                if (o2.length > length) {
                    return -1;
                }
                return 0;
            }

            @Override
            public boolean equals(Object another) {
                if (another == null) {
                    return false;
                }
                if (another.getClass().equals(this.getClass())) {
                    return true;
                }
                return false;
            }
        });
        for (WfDefinition def : definitions) {
            String[] type = def.getCategories();
            for (int i = 0; i < type.length; ++i) {
                String[] subType = new String[i + 1];
                for (int st = 0; st <= i; ++st) {
                    subType[st] = type[st];
                }
                processTypesSet.add(subType);
            }
        }
        processTypes.addAll(processTypesSet);
    }

    @Override
    public boolean hasNext() {
        return curIdx < processTypes.size();
    }

    @Override
    public String[] next() {
        return processTypes.get(curIdx++);
    }

    public String[] getItem(int idx) {
        return processTypes.get(idx);
    }

    @Override
    public void remove() {
    }
}
