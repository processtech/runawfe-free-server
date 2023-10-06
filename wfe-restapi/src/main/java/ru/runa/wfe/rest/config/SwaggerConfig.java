package ru.runa.wfe.rest.config;

import com.fasterxml.classmate.TypeResolver;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.rest.dto.WfeExceptionResponse;
import ru.runa.wfe.rest.dto.WfeValidationExceptionResponse;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.HttpAuthenticationScheme;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Конфиг свагера влияет только на генерирование спецификации и на встроенный интерфейс swagger-ui.
 *
 * @author Zuev Vladimir
 * @since 14.03.2021
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public SwaggerConfigServerUrlConfigurer configurer() {
        return new SwaggerConfigServerUrlConfigurer();
    }

    @Bean
    public Docket customImplementation(TypeResolver typeResolver) {
        return new Docket(DocumentationType.OAS_30)
                .additionalModels(typeResolver.resolve(WfeExceptionResponse.class), typeResolver.resolve(WfeValidationExceptionResponse.class))
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .globalResponses(HttpMethod.POST, Collections.singletonList(internalServerErrorResponse()))
                .globalResponses(HttpMethod.GET, Collections.singletonList(internalServerErrorResponse()))
                .globalResponses(HttpMethod.PUT, Collections.singletonList(internalServerErrorResponse()))
                .globalResponses(HttpMethod.DELETE, Collections.singletonList(internalServerErrorResponse()))
                .globalResponses(HttpMethod.PATCH, Collections.singletonList(internalServerErrorResponse()))
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.JWT_BEARER_BUILDER.name("token").build()))
                .securityContexts(Collections.singletonList(securityContext()))
                .forCodeGeneration(true);
    }

    private Response internalServerErrorResponse() {
        return new ResponseBuilder()
                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .description(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .representation(MediaType.APPLICATION_JSON)
                .apply(r -> r.model(m -> m.referenceModel(ref -> ref.key(k -> k.qualifiedModelName(q ->
                        q.namespace(WfeExceptionResponse.class.getPackage().getName()).name(WfeExceptionResponse.class.getSimpleName()))))))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("runawfe api").version("1.0").build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(securityReferences())
                .operationSelector(o -> (!o.requestMappingPattern().contains("/auth/basic")) && (!o.requestMappingPattern().contains("/auth/kerberos"))).build();
    }

    private List<SecurityReference> securityReferences() {
        return Collections.singletonList(new SecurityReference("token", new AuthorizationScope[] { new AuthorizationScope("global", "global") }));
    }

}
