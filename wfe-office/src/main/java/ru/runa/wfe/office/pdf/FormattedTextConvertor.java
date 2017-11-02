package ru.runa.wfe.office.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xml.sax.SAXException;

/**
 * 
 * @author dkononov
 * @since 4.3.0
 *
 */
public class FormattedTextConvertor {
    
    private static final String FOP_CONFIG_PATH = "fop/fopcfg.xml";
    private static final String XSL_PATH = "fop/xsl/xhtml-to-xslfo.xsl";
    
    /**
     * 
     * @param html строка в формате HTML, не содержащая тэгов &lt;html&gt;, &lt;head&gt;, &lt;body&gt;
     * @return byte[] поток байт, который можно преобразовать в файл формата PDF 
     * @throws TransformerException при выполнении преобразования FO -> PDF, если FO некорректен,
     * либо при выполнении преобразования HTML -> FO
     * @throws ConfigurationException при чтении конфигурационного файла Apache FOP, если он некорректен
     * @throws IOException при работе с потоками I/O
     * @throws SAXException при чтении конфигурационного файла Apache FOP, если он некорректен или неправильном создании экземпляра класса Fop
     */
    public byte[] getPdfFromHtml(String html) throws TransformerException, ConfigurationException, IOException, SAXException {
        html = createPrettyHtml(html);
        System.out.println(html);
        String fo = getFoFromHtml(html, XSL_PATH);
        System.out.println(fo);
        byte[] result = getPdfFromFo(fo, FOP_CONFIG_PATH);
        return result;
    }
    
    /**
     * <p>формирование валидной строки в формате HTML</p>
     * @param html строка в формате HTML, не содержащая тэгов &lt;html&gt;, &lt;head&gt;, &lt;body&gt;
     * @return String строка в формате HTML
     * @throws IOException при работе с потоками I/O
     */
    private String createPrettyHtml(String html) throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        TagNode node = cleaner.clean(html);
        ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
        new PrettyXmlSerializer(props).writeToStream(node, htmlOutputStream);
        html = new String(htmlOutputStream.toByteArray());
        htmlOutputStream.close();
        return html;
    }
    
    /**
     * <p>преобразование FO -> PDF</p>
     * @param fo строка в формате FO
     * @param fopConfigPath путь к конфигурационному файлу Apache FOP
     * @return byte[] поток байт, который можно преобразовать в файл формата PDF 
     * @throws TransformerException при выполнении преобразования FO -> PDF, если FO некорректен
     * @throws IOException при работе с потоками I/O
     * @throws ConfigurationException при чтении конфигурационного файла Apache FOP, если он некорректен
     * @throws SAXException при чтении конфигурационного файла Apache FOP, если он некорректен или неправильном создании экземпляра класса Fop
     */
    private byte[] getPdfFromFo(String fo, String fopConfigPath) throws TransformerException, IOException, ConfigurationException, SAXException {
        //подгрузка конфига для FopFactory
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fopConfigPath).getFile());
        //подготовка преобразования
        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        Configuration configuration = configurationBuilder.buildFromFile(file);
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(new File(".").toURI()).setConfiguration(configuration);
        FopFactory fopFactory = fopFactoryBuilder.build();
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdfOutputStream);
        //преобразование FO -> PDF
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        InputStream foStream = new ByteArrayInputStream(fo.getBytes(StandardCharsets.UTF_8.name()));
        Source foSource = new StreamSource(foStream);
        Result pdfResult = new SAXResult(fop.getDefaultHandler());
        transformer.transform(foSource, pdfResult);
        byte[] result = pdfOutputStream.toByteArray();
        pdfOutputStream.close();
        return result;
    }
    
    /**
     * <p>преобразование HTML -> FO</p>
     * @param html строка в формате HTML
     * @param xsltPath относительный путь к XSL
     * @return String строка в формате FO
     * @throws TransformerException при выполнении преобразования HTML -> FO
     * @throws UnsupportedEncodingException при преобразование строки в поток байт
     */
    private String getFoFromHtml(String html, String xsltPath) throws TransformerException, UnsupportedEncodingException {
        //подгрузка XSL
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(xsltPath).getFile());
        //преобразование HTML -> FO
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslSource = new StreamSource(file);
        Transformer transformer = factory.newTransformer(xslSource);
        InputStream htmlStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8.name()));
        Source htmlSource = new StreamSource(htmlStream);
        StringWriter writer = new StringWriter();
        Result foResult = new StreamResult(writer);
        transformer.transform(htmlSource, foResult);
        return writer.toString();
    }
}
