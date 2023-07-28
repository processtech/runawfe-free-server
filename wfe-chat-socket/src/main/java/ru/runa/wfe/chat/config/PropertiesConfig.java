package ru.runa.wfe.chat.config;

import java.util.Properties;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;

@Configuration
public class PropertiesConfig {
    @Bean
    public PropertyPlaceholderConfigurer chatProperties() {
        final Properties properties = ClassLoaderUtil.getProperties(SystemProperties.CONFIG_FILE_NAME, true);

        final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setProperties(properties);
        return configurer;
    }
}
