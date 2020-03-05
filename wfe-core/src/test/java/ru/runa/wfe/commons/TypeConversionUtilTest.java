package ru.runa.wfe.commons;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Properties;

public class TypeConversionUtilTest {

    private static final String LOCAL_STORAGE_ENABLED = "file.variable.local.storage.enabled";
    private static final String STORAGE_LIMIT = "file.variable.local.storage.enableforfilesgreaterthan";
    private static final String TEST_FILE = "./pom.xml";

    @Test
    public void testStorageEnabledConvertTo() throws Exception {
        switchStorageLimit(false);
        FileVariable fileVariable = TypeConversionUtil.convertTo(FileVariable.class, getTestFile().getAbsolutePath());
        Assert.assertTrue(FileVariableImpl.class.isAssignableFrom(fileVariable.getClass()));
    }

    private Properties getSystemProperties() throws Exception {
        Field fieldResources = SystemProperties.class.getDeclaredField("RESOURCES");
        fieldResources.setAccessible(true);
        PropertyResources resources = (PropertyResources) fieldResources.get(SystemProperties.class);
        Field fieldProperties = resources.getClass().getDeclaredField("properties");
        fieldProperties.setAccessible(true);
        Properties properties = (Properties) fieldProperties.get(resources);
        return properties;
    }

    private void switchStorageLimit(boolean greater) throws Exception {
        long size = Files.size(getTestFile().toPath());
        int limit = SystemProperties.getLocalFileStorageFileLimit();
        boolean sizeIsGreater = size > limit;
        if (greater) {
            if (sizeIsGreater) {
                setFileSizeLimit(Long.valueOf(size + 5));
            }
            return;
        }
        if (sizeIsGreater) {return;}
        setFileSizeLimit(Long.valueOf(size - 5));
    }

    private void setFileSizeLimit(long limit) throws Exception {
        getSystemProperties().setProperty(STORAGE_LIMIT, String.valueOf(limit));
    }

    private void setStorageEnabled(boolean enabled) throws Exception {
        getSystemProperties().setProperty(LOCAL_STORAGE_ENABLED, String.valueOf(enabled));
    }

    private File getTestFile() {
        return new File(TEST_FILE);
    }
}