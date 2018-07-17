package ru.runa.wfe.office.storage;

import java.util.Properties;

import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.dto.WfVariable;

public interface StoreService {

    public static final String PROP_CONSTRAINTS = "constraints";
    public static final String PROP_PATH = "path";
    public static final String PROP_FORMAT = "format";

    void createFileIfNotExist(String path) throws Exception;

    ExecutionResult findByFilter(Properties properties, WfVariable variable, String condition) throws Exception;

    void update(Properties properties, WfVariable variable, String condition) throws Exception;

    void delete(Properties properties, WfVariable variable, String condition) throws Exception;

    void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception;

}
