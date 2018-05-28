package ru.runa.wfe.fop.convertors;

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
public class FormattedTextToPdfConvertor {
	private static final String FOP_CONFIG_PATH = "fop/fopcfg.xml";
    private static final String XSL_PATH = "fop/xsl/xhtml-to-xslfo.xsl";
    
    /**
     * 
     * @param html HTML-string without tags &lt;html&gt;, &lt;head&gt;, &lt;body&gt;
     * @return byte[] PDF byte stream 
     * @throws TransformerException when converting FO -> PDF, if FO has incorrect format,
     * or when converting HTML -> FO
     * @throws ConfigurationException when reading Apache FOP config file, if it has incorrect format
     * @throws IOException I/O streams throw
     * @throws SAXException when reading Apache FOP config file, if it has incorrect format 
     * or when you incorrect create an instance of the class Fop
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
     * <p>creating valid HTML-string</p>
     * @param html HTML-string without tags  &lt;html&gt;, &lt;head&gt;, &lt;body&gt;
     * @return String HTML-string
     * @throws IOException I/O streams throw
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
     * <p>FO -> PDF converting</p>
     * @param fo FO-string
     * @param fopConfigPath Apache FOP config files's path
     * @return byte[] PDF byte stream 
     * @throws TransformerException when converting FO -> PDF, if FO has incorrect format
     * @throws IOException I/O streams throw
     * @throws ConfigurationException when reading Apache FOP config file, if it has incorrect format
     * @throws SAXException when reading Apache FOP config file, if it has incorrect format 
     * or when you incorrect create an instance of the class Fop
     */
    private byte[] getPdfFromFo(String fo, String fopConfigPath) throws TransformerException, IOException, ConfigurationException, SAXException {
        //reading FopFactory's config file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fopConfigPath).getFile());
        //prepare converting
        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        Configuration configuration = configurationBuilder.buildFromFile(file);
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(new File(".").toURI()).setConfiguration(configuration);
        FopFactory fopFactory = fopFactoryBuilder.build();
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdfOutputStream);
        //FO -> PDF converting
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
     * <p>HTML -> FO converting</p>
     * @param html HTML-string
     * @param xsltPath XSL files's path
     * @return String FO-string
     * @throws TransformerException when converting HTML -> FO
     * @throws UnsupportedEncodingException when convertingString->byte[]
     */
    private String getFoFromHtml(String html, String xsltPath) throws TransformerException, UnsupportedEncodingException {
        //reading XSL
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(xsltPath).getFile());
        //converting HTML -> FO
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
