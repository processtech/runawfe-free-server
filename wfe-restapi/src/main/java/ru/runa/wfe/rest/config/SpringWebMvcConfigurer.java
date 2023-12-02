package ru.runa.wfe.rest.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import java.util.TimeZone;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor()).addPathPatterns("/**");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof AbstractJackson2HttpMessageConverter) {
                AbstractJackson2HttpMessageConverter jacksonConverter = (AbstractJackson2HttpMessageConverter) converter;
                jacksonConverter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
                jacksonConverter.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                jacksonConverter.getObjectMapper().setTimeZone(TimeZone.getDefault());
            }
        }
    }

}
