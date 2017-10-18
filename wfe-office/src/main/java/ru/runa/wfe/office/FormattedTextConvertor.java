package ru.runa.wfe.office;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.rtf.RtfWriter2;

/**
 * 
 * @author dkononov
 * @since 4.3.0
 *
 */
public class FormattedTextConvertor {

    /**
     * преобразование HTML в PDF
     * 
     * @param html исходный текст
     * @return byte[]
     * @throws IOException
     * @throws DocumentException
     */
    public static byte[] htmlToPdf(String html) throws IOException, DocumentException {
        // преобразование текста в валидный HTML
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        TagNode node = cleaner.clean(html);
        ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
        new PrettyXmlSerializer(props).writeToStream(node, htmlOutputStream);

        // запись данных в поток с возможностью преобразовать в PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(new String(htmlOutputStream.toByteArray(), StandardCharsets.UTF_8.name()));
        htmlOutputStream.close();
        renderer.layout();
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        renderer.createPDF(pdfOutputStream);
        renderer.finishPDF();

        // формирование массива байт
        byte[] result = pdfOutputStream.toByteArray();
        pdfOutputStream.close();
        return result;
    }

    /**
     * преобразование HTML в RTF
     * 
     * @param html исходный текст
     * @return byte[]
     * @throws IOException
     */
    public static byte[] htmlToRtf(String html) throws IOException {
        // преобразование текста в валидный HTML
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        TagNode node = cleaner.clean(html);
        ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
        new PrettyXmlSerializer(props).writeToStream(node, htmlOutputStream);
        html = new String (htmlOutputStream.toByteArray());
        htmlOutputStream.close();
        
        // запись данных в поток с возможностью преобразовать в PDF
        ByteArrayOutputStream rtfOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        RtfWriter2.getInstance(document, rtfOutputStream);
        document.open();
        HTMLWorker htmlWorker = new HTMLWorker(document);
        htmlWorker.parse(new StringReader(html));
        document.close();
        
        // формирование массива байт
        byte[] result = rtfOutputStream.toByteArray();
        rtfOutputStream.close();
        return result;
    }

}
