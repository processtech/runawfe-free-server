package ru.runa.wfe.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.oas.web.WebMvcOpenApiTransformationFilter;
import springfox.documentation.spi.DocumentationType;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SwaggerConfigServerUrlConfigurer implements WebMvcOpenApiTransformationFilter {

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
        OpenAPI openApi = context.getSpecification();
        context.request().ifPresent(request -> {
            String referer = request.getHeader(HttpHeaders.REFERER);
            if (referer == null || referer.startsWith(SpringWebMvcConfigurer.DEV_URL)) {
                return;
            }
            String serverUrl = UriComponentsBuilder.fromHttpUrl(referer).replacePath(null).replaceQuery(null).toUriString();
            openApi.servers(Collections.singletonList(new Server().url(serverUrl).description("Referer Url")));
        });
        return openApi;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return delimiter == DocumentationType.OAS_30;
    }
}
