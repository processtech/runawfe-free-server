package ru.runa.wfe.office.doc;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.MediaType;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.office.shared.FilesSupplierConfig;
import ru.runa.wfe.var.format.VariableFormat;

@CommonsLog
public class DocxConfig extends FilesSupplierConfig {
    public static final String PDF_EXTENSION = "pdf";
    private boolean strictMode;
    private final Map<String, TableConfig> tables = Maps.newHashMap();
    private final Map<String, VariableFormat> typeHints = Maps.newHashMap();

    @Override
    protected MediaType getContentType() {
        if (getOutputFileName().endsWith(PDF_EXTENSION)) {
            return MediaType.PDF;
        }
        return MediaType.OOXML_DOCUMENT;
    }

    @Override
    public String getDefaultOutputFileName() {
        return "document.docx";
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void reportProblem(String message) {
        if (strictMode) {
            throw new InternalApplicationException(message);
        }
        log.warn(message);
    }

    public void reportProblem(Exception e) {
        if (strictMode) {
            Throwables.propagate(e);
        }
        log.warn("", e);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public Map<String, TableConfig> getTables() {
        return tables;
    }

    public Map<String, VariableFormat> getTypeHints() {
        return typeHints;
    }

    /**
     * @deprecated remove before release 4.2.0
     */
    @Deprecated
    public static class TableConfig {
        private boolean addBreak;
        private String styleName;
        private final List<String> columns = Lists.newArrayList();

        public void setAddBreak(boolean addBreak) {
            this.addBreak = addBreak;
        }

        public boolean isAddBreak() {
            return addBreak;
        }

        public void setStyleName(String styleName) {
            this.styleName = styleName;
        }

        public String getStyleName() {
            return styleName;
        }

        public List<String> getColumns() {
            return columns;
        }
    }
}
