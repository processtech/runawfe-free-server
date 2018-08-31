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
package ru.runa.common.web.action;

import com.google.common.net.MediaType;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellUtil;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

/**
 * @since 4.3.0
 */
public abstract class AbstractExportExcelAction<T> extends ActionBase {

    protected abstract List<T> getData(User user, BatchPresentation batchPresentation);

    protected abstract String getFileNamePrefix();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = getLoggedUser(request);
            String batchPresentationId = ((BatchPresentationForm) form).getBatchPresentationId();
            Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
            BatchPresentation batchPresentation = profile.getActiveBatchPresentation(batchPresentationId);
            List<T> data = getData(user, batchPresentation);
            HSSFWorkbook workbook = new HSSFWorkbook();
            Sheet dataSheet = workbook.createSheet("data");
            buildHeader(request, workbook, batchPresentation);
            buildData(dataSheet, user, batchPresentation, data);
            response.setContentType(MediaType.MICROSOFT_EXCEL.toString());
            String encodedFileName = HTMLUtils.encodeFileName(request,
                    getFileNamePrefix() + "-" + CalendarUtil.formatDateTime(Calendar.getInstance()) + ".xls");
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("Content-Transfer-Encoding", "binary");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Unable to export excel", e);
        }
        return null;
    }

    private void buildHeader(HttpServletRequest request, HSSFWorkbook workbook, BatchPresentation batchPresentation) {
        CellStyle boldCellStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldCellStyle.setFont(boldFont);
        Row header = workbook.getSheetAt(0).createRow(0);
        int i = 0;
        for (FieldDescriptor fieldDescriptor : batchPresentation.getDisplayFields()) {
            if (fieldDescriptor.displayName.startsWith(ClassPresentation.editable_prefix)
                    || fieldDescriptor.displayName.startsWith(ClassPresentation.filterable_prefix)
                    || fieldDescriptor.fieldState != FieldState.ENABLED) {
                continue;
            }
            Cell cell = header.createCell(i++);
            cell.setCellStyle(boldCellStyle);
            cell.setCellValue(getDisplayString(request, fieldDescriptor));
        }
    }

    private String getDisplayString(HttpServletRequest request, FieldDescriptor fieldDescriptor) {
        if (fieldDescriptor.displayName.startsWith(ClassPresentation.removable_prefix)) {
            return fieldDescriptor.displayName.substring(fieldDescriptor.displayName.lastIndexOf(':') + 1);
        }
        String messageKey = fieldDescriptor.displayName;
        if (fieldDescriptor.displayName.startsWith(ClassPresentation.filterable_prefix)) {
            messageKey = fieldDescriptor.displayName.substring(fieldDescriptor.displayName.lastIndexOf(':') + 1);
        }
        return getResources(request).getMessage(messageKey);
    }

    private void buildData(Sheet dataSheet, User user, BatchPresentation batchPresentation, List<T> data) {
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, null);
        int rowNum = 1;
        EnvImpl env = new EnvImpl(user, batchPresentation);
        for (T object : data) {
            Row row = dataSheet.createRow(rowNum++);
            int i = 0;
            for (TdBuilder builder : builders) {
                String string = builder.getValue(object, env);
                // TODO strings instead of native types
                CellUtil.createCell(row, i++, string);
            }
        }

    }

    private static class EnvImpl extends EnvBaseImpl {
        private final User user;
        private final BatchPresentation batchPresentation;

        public EnvImpl(User user, BatchPresentation batchPresentation) {
            this.user = user;
            this.batchPresentation = batchPresentation;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public PageContext getPageContext() {
            // TODO no localization
            return null;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            return batchPresentation;
        }

        @Override
        public String getURL(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAllowed(Permission permission, SecuredObjectExtractor extractor) {
            return false;
        }
    }
}
