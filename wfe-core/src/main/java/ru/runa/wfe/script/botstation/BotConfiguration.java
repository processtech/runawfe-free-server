package ru.runa.wfe.script.botstation;

import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@XmlType(name = "botConfigurationType", namespace = AdminScriptConstants.NAMESPACE)
public class BotConfiguration {

    public static final Log LOG = LogFactory.getLog(BotConfiguration.class);

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.HANDLER_ATTRIBUTE_NAME)
    public String handler = "";

    @XmlAttribute(name = AdminScriptConstants.EMBEDDED_FILE_ATTRIBUTE_NAME)
    public String embeddedFile = "";

    @XmlAttribute(name = AdminScriptConstants.SEQUENTIAL_EXECUTION_ATTRIBUTE_NAME)
    public Boolean sequentialExecution;

    @XmlAttribute(name = AdminScriptConstants.CONFIGURATION_STRING_ATTRIBUTE_NAME)
    public String configuration;

    @XmlAttribute(name = AdminScriptConstants.CONFIGURATION_CONTENT_ATTRIBUTE_NAME)
    public String configurationContent;

    @XmlMixed
    @XmlAnyElement
    public List elementContent = Lists.newArrayList();

    public void validate(ScriptOperation scriptOperation, ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(scriptOperation, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
    }

    public BotTask createBotTask(Bot bot, ScriptExecutionContext context) {
        BotTask task = new BotTask(bot, name);
        task.setTaskHandlerClassName(handler);
        // Add BotTask embedded file if exists
        if (!Strings.isNullOrEmpty(embeddedFile)) {
            task.setEmbeddedFile(context.getExternalResource(embeddedFile));
            Path path = FileSystems.getDefault().getPath(embeddedFile);
            task.setEmbeddedFileName(path.getFileName().toString());
        }
        task.setSequentialExecution(sequentialExecution);
        byte[] conf = getConfiguration(context);
        LOG.info("adding bot configuration element: " + name + " with conf: " + new String(conf, Charsets.UTF_8));
        task.setConfiguration(conf);
        return task;
    }

    private byte[] getConfiguration(ScriptExecutionContext context) {
        if (!Strings.isNullOrEmpty(configuration)) {
            return context.getExternalResource(configuration);
        }
        if (!Strings.isNullOrEmpty(configurationContent)) {
            return configurationContent.trim().getBytes(Charsets.UTF_8);
        }
        for (Object content : elementContent) {
            if (content instanceof String) {
                if (Strings.isNullOrEmpty(((String) content).trim())) {
                    continue;
                }
                return ((String) content).trim().getBytes(Charsets.UTF_8);
            }
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource((Node) content), new StreamResult(byteArrayOutputStream));
                return byteArrayOutputStream.toByteArray();
            } catch (TransformerException e) {
                Throwables.propagate(e);
            }
        }
        return new byte[0];
    }

    public Collection<? extends String> getExternalResources() {
        ArrayList<String> result = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(configuration)) {
            result.add(configuration);
        }
        if (!Strings.isNullOrEmpty(embeddedFile)) {
            result.add(embeddedFile);
        }
        return result;
    }
}
