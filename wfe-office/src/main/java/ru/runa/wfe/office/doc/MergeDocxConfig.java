package ru.runa.wfe.office.doc;

import java.io.InputStream;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MergeDocxConfig extends DocxConfig {
    private final List<DocxInfo> inputFileInfos = Lists.newArrayList();

    public List<DocxInfo> getInputFileInfos() {
        return inputFileInfos;
    }

    public InputStream getFileInputStream(VariableProvider variableProvider, FileDataProvider fileDataProvider, DocxInfo info) {
        if (!Strings.isNullOrEmpty(info.path)) {
            return getInputStreamByPath(variableProvider, fileDataProvider, info.path);
        }
        if (!Strings.isNullOrEmpty(info.variableName)) {
            return getInputStreamByVariableName(variableProvider, info.variableName);
        }
        throw new InternalApplicationException("No input docx info defined in configuration");
    }

    public static class DocxInfo {
        public String path;
        public String variableName;
        public boolean addBreak;

    }
}
