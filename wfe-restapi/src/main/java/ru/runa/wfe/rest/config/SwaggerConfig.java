package ru.runa.wfe.rest.config;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.HttpAuthenticationScheme;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .directModelSubstitute(Date.class, Long.class)
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.JWT_BEARER_BUILDER.name("token").build()))
                .securityContexts(Collections.singletonList(securityContext()))
        ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("runawfe api").version("1.0").build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(securityReferences()).build();
    }

    private List<SecurityReference> securityReferences() {
        return Collections.singletonList(new SecurityReference("Authorization",
                new AuthorizationScope[] { new AuthorizationScope("global", "global") }));
    }

    // @Bean
    // public SecurityConfiguration security() {
        // return SecurityConfigurationBuilder.builder().scopeSeparator(",").additionalQueryStringParams(null)
        // .useBasicAuthenticationWithAccessCodeGrant(false).build();
    // }

}
