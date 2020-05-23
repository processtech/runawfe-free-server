package ru.runa.wfe.script.datasource;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = DeployDataSourceOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeployDataSourceOperation extends ScriptOperation implements DataSourceStuff {

    public static final String SCRIPT_NAME = "deployDataSource";

    @XmlAttribute(name = AdminScriptConstants.FILE_ATTRIBUTE_NAME, required = true)
    public String file;

    @Override
    public List<String> getExternalResources() {
        return Lists.newArrayList(file);
    }

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.FILE_ATTRIBUTE_NAME, file);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        if (DataSourceStorage.save(context.getExternalResource(file), false, false)) {
            if (context.getDataSourceDefaultPassword() != null) {
                String dsFileName = file.substring(file.lastIndexOf("/") + 1);
                DataSourceStorage.changePassword(dsFileName.substring(0, dsFileName.length() - DATA_SOURCE_FILE_SUFFIX.length()),
                        context.getDataSourceDefaultPassword());
            }
        }
    }
}
