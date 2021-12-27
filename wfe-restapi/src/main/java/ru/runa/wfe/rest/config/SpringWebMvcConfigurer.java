package ru.runa.wfe.rest.config;

import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.runa.wfe.rest.converter.StringToSortOrderConverter;

@Component
public class SpringWebMvcConfigurer implements WebMvcConfigurer {
    public static final String DEV_URL = "http://127.0.0.1:3000";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry
            .addViewController("/swagger-ui/")
            .setViewName("forward:" + "/swagger-ui/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
          registry
              .addMapping("/**")
              .allowedOrigins(DEV_URL)
              .allowCredentials(true)
              .allowedMethods("HEAD", "GET", "PUT", "POST", "OPTIONS", "DELETE", "PATCH")
              .allowedHeaders("*")
              .maxAge(3600);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToSortOrderConverter());
    }
}
