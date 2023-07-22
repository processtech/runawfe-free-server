package ru.runa.wfe.commons.xml;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import ru.runa.wfe.commons.ClassLoaderUtil;

/**
 * Unifies XML operations (Dom4j).
 * 
 * @author Dofs
 */
public class XmlUtils {
    public static final String RUNA_NAMESPACE = "http://runa.ru/xml";

    public static Document createDocument(String rootElementName) {
        Document document = DocumentHelper.createDocument();
        document.addElement(rootElementName);
        return document;
    }

    public static Document createDocument(String rootElementName, String defaultNamespaceUri) {
        Document document = createDocument(rootElementName);
        document.getRootElement().addNamespace("", defaultNamespaceUri);
        return document;
    }

    public static Document createDocument(String rootElementName, String defaultNamespaceUri, String xsdLocation) {
        Document document = createDocument(rootElementName, defaultNamespaceUri);
        document.getRootElement().addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        document.getRootElement().addAttribute("xsi:schemaLocation", defaultNamespaceUri + " " + xsdLocation);
        return document;
    }

    public static Document parseWithoutValidation(String data) {
        return parseWithoutValidation(data.getBytes(Charsets.UTF_8));
    }

    public static Document parseWithoutValidation(byte[] data) {
        return parse(new ByteArrayInputStream(data), false, false, null);
    }

    public static Document parseWithoutValidation(InputStream in) {
        return parse(in, false, false, null);
    }

    public static Document parseWithXSDValidation(InputStream in, String xsdResourceName) {
        return parse(in, false, true, xsdResourceName);
    }

    public static Document parseWithXSDValidation(byte[] data, String xsdResourceName) {
        return parseWithXSDValidation(new ByteArrayInputStream(data), xsdResourceName);
    }

    public static Document parseWithXSDValidation(String data, String xsdResourceName) {
        return parseWithXSDValidation(data.getBytes(Charsets.UTF_8), xsdResourceName);
    }

    private static Document parse(InputStream in, boolean dtdValidation, boolean xsdValidation, String xsdResourceName) {
        try {
            SAXReader reader;
            if (xsdValidation) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                if (xsdResourceName != null) {
                    InputStream xsdInputStream = ClassLoaderUtil.getAsStreamNotNull(xsdResourceName, XmlUtils.class);
                    SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                    factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(xsdInputStream) }));
                } else {
                    factory.setValidating(true);
                }
                SAXParser parser = factory.newSAXParser();
                if (xsdResourceName == null) {
                    parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                }
                reader = new SAXReader(parser.getXMLReader());
            } else {
                reader = new SAXReader();
            }
            reader.setValidation(dtdValidation || (xsdValidation && xsdResourceName == null));
            reader.setErrorHandler(SimpleErrorHandler.getInstance());
            return reader.read(new BomSkippingReader(new InputStreamReader(in, Charsets.UTF_8)));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] save(Node node) {
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setTrimText(false);
        return save(node, outputFormat);
    }

    public static byte[] save(Node node, OutputFormat outputFormat) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(baos, outputFormat);
            writer.write(node);
            return baos.toByteArray();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String toString(Node node) {
        return new String(save(node), Charsets.UTF_8);
    }

    public static String toString(Node node, OutputFormat outputFormat) {
        return new String(save(node, outputFormat), Charsets.UTF_8);
    }

    public static String serialize(Map<String, String> map) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("r");
        for (Entry<String, String> entry : map.entrySet()) {
            Element e = root.addElement(entry.getKey());
            e.addText(String.valueOf(entry.getValue()));
        }
        OutputFormat outputFormat = OutputFormat.createCompactFormat();
        outputFormat.setTrimText(false);
        outputFormat.setSuppressDeclaration(true);
        return toString(document, outputFormat);
    }

    public static HashMap<String, String> deserialize(String xml) {
        HashMap<String, String> result = Maps.newHashMap();
        Document document = parseWithoutValidation(xml);
        List<Element> elements = document.getRootElement().elements();
        for (Element element : elements) {
            result.put(element.getName(), element.getText());
        }
        return result;
    }

    public static String unwrapCdata(String xml) {
        if (StringUtils.isNotBlank(xml)) {
            while (xml.startsWith("&")) {
                xml = StringEscapeUtils.unescapeXml(xml);
            }
            return xml.replace("<![CDATA[", "").replace("]]>", "");
        }
        return "";
    }
}
