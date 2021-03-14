package ru.runa.wfe.rest.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class SpringWebMvcConfigurer implements WebMvcConfigurer {

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
              //TODO Вынести настройку в properties с белым списком для кроссдоменных запросов
              .allowedOrigins("http://127.0.0.1:3000")
              .allowCredentials(true)
              .allowedMethods("HEAD", "GET", "PUT", "POST", "OPTIONS", "DELETE", "PATCH")
              .allowedHeaders("*")
              .maxAge(3600);
    }

}
