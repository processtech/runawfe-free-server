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
    public static byte[] formattedTextFormatToDocx(FormattedTextFormat textFormat, Object htmlText) throws IOException, DocumentException {
        String html = textFormat.formatHtml(null, null, null, null, htmlText);

        // обработка текста как HTML, запись в поток
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        TagNode node = cleaner.clean(html);
        ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
        new PrettyXmlSerializer(props).writeToStream(node, htmlOutputStream);

        // запись данных из одного потока в другой с возмодностью преобразовать в PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(new String(htmlOutputStream.toByteArray(), StandardCharsets.UTF_8.name()));
        htmlOutputStream.close();

        // формирование массива байт
        renderer.layout();
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        renderer.createPDF(pdfOutputStream);
        renderer.finishPDF();
        byte[] result = pdfOutputStream.toByteArray();
        pdfOutputStream.close();

        return result;
    }

}
