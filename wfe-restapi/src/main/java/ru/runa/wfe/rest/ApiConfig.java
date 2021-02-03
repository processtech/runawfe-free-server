package ru.runa.wfe.rest;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableWebMvc
@EnableSwagger2
@EnableTransactionManagement
@ComponentScan
public class ApiConfig {

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .apis(RequestHandlerSelectors.basePackage("ru.runa.wfe.rest"))
                .paths(PathSelectors.any()).build()
                //.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                //.directModelSubstitute(java.time.OffsetDateTime.class, java.util.Date.class)
                .apiInfo(apiInfo())
                //.securityContexts(Collections.singletonList(securityContext())).securitySchemes(Collections.singletonList(securitySchema())
        ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("runawfe api").version("1").build();
    }

}
