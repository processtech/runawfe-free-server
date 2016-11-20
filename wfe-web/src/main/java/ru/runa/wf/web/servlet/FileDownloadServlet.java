package ru.runa.wf.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wf.web.form.PagingForm;
import ru.runa.wf.web.html.ProcessExecutionStatusTDBuilder;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class FileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String batchPresentationId = request.getParameter(PagingForm.BATCH_PRESENTATION_ID);
        final BatchPresentation batchPresentation = getProfile(request).getActiveBatchPresentation(batchPresentationId);
        final File file = new File(batchPresentationId + "_" + request.getRequestedSessionId() + ".csv");
        PrintStream ps = null;
        try {
            ps = new PrintStream(file);
            final FileBuilder builder = new FileBuilder(batchPresentation, ps, getLocale(request));
            builder.printHeader();
            ps.flush();

            final List<?> rows = getRows(getUser(request), batchPresentation.clone());
            for (final Object row : rows) {
                builder.printRow(row);
                ps.flush();
            }

        } finally {
            if (null != ps) {
                ps.close();
            }
        }

        response.setContentType("text/csv");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", batchPresentationId + ".csv"));

        final OutputStream out = response.getOutputStream();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (null != in) {
                in.close();
            }
        }
        out.flush();
        file.delete();
    }

    protected Profile getProfile(HttpServletRequest request) {
        return ProfileHttpSessionHelper.getProfile(request.getSession());
    }

    protected Locale getLocale(HttpServletRequest request) {
        return request.getLocale();
    }

    protected User getUser(HttpServletRequest request) {
        return Commons.getUser(request.getSession());
    }

    private List<?> getRows(User user, BatchPresentation batchPresentation) {
        batchPresentation.setRangeSize(10000);
        final ExecutionService executionService = Delegates.getExecutionService();
        if ("listProcessesForm".equals(batchPresentation.getCategory())) {
            return executionService.getProcesses(user, batchPresentation);
        } else if ("listExtendedProcessesForm".equals(batchPresentation.getCategory())) {
            return executionService.getExtendedProcesses(user, batchPresentation);
        }
        return Collections.EMPTY_LIST;
    }

    private class FileBuilder {
        private final List<String> fieldNames;
        private final List<TDBuilder> tdBuilders;
        private final PrintStream ps;
        private final ResourceBundle bundle;

        public FileBuilder(BatchPresentation batchPresentation, PrintStream ps, Locale locale) {
            this.ps = ps;
            this.fieldNames = new ArrayList<String>(batchPresentation.getDisplayFields().length);
            this.tdBuilders = new ArrayList<TDBuilder>(batchPresentation.getDisplayFields().length);
            this.bundle = ResourceBundle.getBundle("struts", locale);

            for (final FieldDescriptor field : batchPresentation.getDisplayFields()) {
                String displayName = field.displayName;
                if (displayName.startsWith(ClassPresentation.editable_prefix) || displayName.startsWith(ClassPresentation.filterable_prefix)
                        || field.fieldState != FieldState.ENABLED) {
                    continue;
                }
                this.fieldNames.add(displayName);
                this.tdBuilders.add((TDBuilder) field.getTDBuilder());
            }
        }

        public void printHeader() {
            boolean isFirst = true;
            for (final String fieldName : fieldNames) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    ps.print(";");
                }
                ps.print(bundle.getString(fieldName));
            }
            ps.println();
        }

        public void printRow(Object row) {
            boolean isFirst = true;
            for (final TDBuilder builder : tdBuilders) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    ps.print(";");
                }
                final String value;
                if (builder instanceof ProcessExecutionStatusTDBuilder) {
                    value = bundle.getString(((ProcessExecutionStatusTDBuilder) builder).getKey(row));
                } else {
                    value = builder.getValue(row, null);
                }
                ps.print(value);
            }
            ps.println();
        }
    }
}
