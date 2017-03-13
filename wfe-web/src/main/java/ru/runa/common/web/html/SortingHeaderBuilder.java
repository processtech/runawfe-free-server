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
package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import com.google.common.collect.Maps;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.SetSortingAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.wfe.commons.ArraysCommons;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;

/**
 *
 * Created on 17.11.2005
 *
 */
public class SortingHeaderBuilder implements HeaderBuilder {
    private final BatchPresentation batchPresentation;
    private final PageContext pageContext;
    private final String[] prefixNames;
    private final String[] suffixNames;
    private final String returnActionName;

    private static String[] createEmptyStrings(int size) {
        return (String[]) ArraysCommons.fillArray(new String[size], Entities.NBSP);
    }

    public SortingHeaderBuilder(BatchPresentation batchPresentation, String[] prefixNames, String[] suffixNames, String returnActionName,
            PageContext pageContext) {
        this.batchPresentation = batchPresentation;
        this.prefixNames = prefixNames;
        this.suffixNames = suffixNames;
        this.returnActionName = returnActionName;
        this.pageContext = pageContext;
    }

    public SortingHeaderBuilder(BatchPresentation batchPresentation, int numberOfPrefixCells, int numberOfSuffixCells, String returnActionName,
            PageContext pageContext) {
        this(batchPresentation, createEmptyStrings(numberOfPrefixCells), createEmptyStrings(numberOfSuffixCells), returnActionName, pageContext);
    }

    @Override
    public TR build() {
        FieldDescriptor[] sortingFields = batchPresentation.getSortedFields();
        boolean[] sortingModes = batchPresentation.getFieldsToSortModes();
        Map<Integer, Boolean> sortedFieldsIdModeMap = new HashMap<Integer, Boolean>();
        for (int i = 0; i < sortingFields.length; i++) {
            sortedFieldsIdModeMap.put(Integer.valueOf(sortingFields[i].fieldIdx), Boolean.valueOf(sortingModes[i]));
        }
        TR tr = new TR();
        createCells(tr, createEmptyStrings(getAditionalNumberOfPrefixEmptyCells()));
        createCells(tr, prefixNames);
        fillHeaderTR(batchPresentation.getDisplayFields(), sortedFieldsIdModeMap, tr);
        createCells(tr, suffixNames);
        return tr;
    }

    private void fillHeaderTR(FieldDescriptor[] fields, Map<Integer, Boolean> sortedFieldsIdModeMap, TR tr) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].displayName.startsWith(ClassPresentation.editable_prefix)
                    || fields[i].displayName.startsWith(ClassPresentation.filterable_prefix) || fields[i].fieldState != FieldState.ENABLED) {
                continue;
            }

            TH header = new TH();
            tr.addElement(header);
            header.setClass(Resources.CLASS_LIST_TABLE_TH);
            IMG sortingImage = null;
            Boolean sortingMode = sortedFieldsIdModeMap.get(Integer.valueOf(fields[i].fieldIdx));
            if (sortingMode != null) {// i.e. there is at least on sorting field
                if (sortingMode.booleanValue() == BatchPresentationConsts.ASC) {
                    sortingImage = new IMG(Commons.getUrl(Resources.SORT_ASC_IMAGE, pageContext, PortletUrlType.Resource));
                    sortingImage.setAlt(Resources.SORT_ASC_ALT);
                } else {
                    sortingImage = new IMG(Commons.getUrl(Resources.SORT_DESC_IMAGE, pageContext, PortletUrlType.Resource));
                    sortingImage.setAlt(Resources.SORT_DESC_ALT);
                }
            }

            Map<String, Object> params = Maps.newHashMap();
            params.put(SetSortingForm.BATCH_PRESENTATION_ID, batchPresentation.getCategory());
            params.put(IdForm.ID_INPUT_NAME, fields[i].fieldIdx);
            params.put(ReturnActionForm.RETURN_ACTION, returnActionName);
            if (fields[i].sortable) {
                String url = Commons.getActionUrl(SetSortingAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
                A link = new A(url, getDisplayString(fields[i]));
                header.addElement(link);
                if (sortingImage != null) {
                    header.addElement(Entities.NBSP);
                    header.addElement(sortingImage);
                }
            } else {
                header.addElement(getDisplayString(fields[i]));
            }
        }
    }

    private String getDisplayString(FieldDescriptor field) {
        if (field.displayName.startsWith(ClassPresentation.removable_prefix)) {
            return field.displayName.substring(field.displayName.lastIndexOf(':') + 1);
        } else if (field.displayName.startsWith(ClassPresentation.filterable_prefix)) {
            return Messages.getMessage(field.displayName.substring(field.displayName.lastIndexOf(':') + 1), pageContext);
        } else {
            return Messages.getMessage(field.displayName, pageContext);
        }
    }

    private int getAditionalNumberOfPrefixEmptyCells() {
        return batchPresentation.getGrouppedFields().length;
    }

    private void createCells(TR tr, String[] names) {
        for (int i = 0; i < names.length; i++) {
            TH header = new TH();
            header.addElement(names[i]);
            tr.addElement(header);
            if (names[i] == Entities.NBSP) {
                header.setClass(Resources.CLASS_EMPTY20_TABLE_TD);
            } else {
                header.setClass(Resources.CLASS_LIST_TABLE_TH);
            }
        }
    }
}
