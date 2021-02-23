package ru.runa.wfe.commons.monitoring;

import net.bull.javamelody.MonitoredWithAnnotationPointcut;
import net.bull.javamelody.MonitoringSpringAdvisor;
import net.bull.javamelody.SpringContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author Alekseev Mikhail
 * @since #1461
 */
@Configuration
public class JavaMelodySpringBeansMonitoringConfig {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MonitoringSpringAdvisor monitoringSpringAdvisor() {
        return new MonitoringSpringAdvisor(new MonitoredWithAnnotationPointcut());
    }

    @Bean
    public SpringContext springContext() {
        return new SpringContext();
    }
}
