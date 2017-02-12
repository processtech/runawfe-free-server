package ru.runa.wfe.script.common;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.CustomAdminScriptJob;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@XmlType(name = CustomOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CustomOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "custom";

    @XmlAttribute(name = "job")
    public String jobClass;

    @XmlMixed
    @XmlAnyElement
    public List elementContent = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        try {
            ClassLoaderUtil.instantiate(jobClass);
        } catch (Exception e) {
            throw new ScriptValidationException(this, e);
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        try {
            CustomAdminScriptJob job = ClassLoaderUtil.instantiate(jobClass);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JAXBContext.newInstance(this.getClass()).createMarshaller().marshal(this, outputStream);
            job.execute(context.getUser(), XmlUtils.parseWithoutValidation(outputStream.toByteArray()).getRootElement());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

}
