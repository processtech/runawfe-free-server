package ru.runa.wfe.office.doc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import ru.runa.wfe.var.format.FormattedTextFormat;

/**
 * 
 * @author dkononov
 * @since 4.3.0
 *
 */
public class DocxConvertor {

    /**
     * перобразование HTML в PDF
     * 
     * @param textFormat переменная типа {@link FormattedTextFormat}
     * @param htmlText текст, помещённый в поле ввода
     * @return byte[]
     * @throws IOException
     * @throws DocumentException
     */
    public byte[] formattedTextFormatToDocx(FormattedTextFormat textFormat, Object htmlText) throws IOException, DocumentException {
        String html = textFormat.formatHtml(null, null, null, null, htmlText);

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        TagNode node = cleaner.clean(html);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new PrettyXmlSerializer(props).writeToStream(node, outputStream);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(new String(outputStream.toByteArray(), StandardCharsets.UTF_8.name()));
        renderer.layout();
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        renderer.createPDF(pdfOutputStream);

        renderer.finishPDF();
        pdfOutputStream.flush();
        pdfOutputStream.close();

        byte[] result = outputStream.toByteArray();
        outputStream.close();
        return result;
    }

}
