package ru.runa.wfe.office.doc;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.google.common.collect.Maps;

import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.format.FormattedTextFormat;

public class DocxConvertor {

    public void formattedTextFormatToDocx(FormattedTextFormat textFormat, Object htmlText) throws Docx4JException {
        WordprocessingMLPackage wordMlPackage = WordprocessingMLPackage.createPackage();
        XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMlPackage);
        String html = textFormat.formatHtml(null, null, null, null, htmlText);
        wordMlPackage.getMainDocumentPart().getContent().addAll(xhtmlImporter.convert(html, null));

    }

    public static void execute(IVariableProvider variableProvider, FormattedTextFormat textFormat, Object htmlText) throws Docx4JException {
        DocxConfig config = new DocxConfig();
        WordprocessingMLPackage wordMlPackage = WordprocessingMLPackage.createPackage();
        XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMlPackage);
        String html = textFormat.formatHtml(null, null, null, null, htmlText);
        wordMlPackage.getMainDocumentPart().getContent().addAll(xhtmlImporter.convert(html, null));
        wordMlPackage.save(new File(config.getDefaultOutputFileName()));
        Map<String, Object> result = Maps.newHashMap();
        OutputStream outputStream = config.getFileOutputStream(result, variableProvider, true);
    }

}
