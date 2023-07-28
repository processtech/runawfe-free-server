package ru.runa.wf.logic.bot.webservice;

import java.util.List;

import ru.runa.wf.logic.bot.WebServiceTaskHandler;

import com.google.common.base.Charsets;

/**
 * Settings for {@link WebServiceTaskHandler}.
 */
public class WebServiceTaskHandlerSettings {
    /**
     * Credentials, sends in authorization property of web request for Basic
     * authentication.
     */
    public final String authBase;

    /**
     * HTTP Request method (GET, POST, PUT and so on).
     */
    public final String requestMethod;

    /**
     * Web service URL or variable name to read web service URL.
     */
    public final String url;

    /**
     * Value of SOAPAction HTTP header.
     */
    public final String soapAction;

    /**
     * List of interactions with web service.
     */
    public final List<Interaction> interactions;

    /**
     * Encoding, used in HTTP connection and request XML/response XSLT.
     */
    public final String encoding = Charsets.UTF_8.name();

    /**
     * Log request and response with debug priority if set.
     */
    public final boolean isLoggingEnable;

    /**
     * Action to be performed if error response received from web service. (May
     * be overridden in interactions).
     */
    public final ErrorResponseProcessingResult errorAction;

    /**
     * Creates instance of {@link WebServiceTaskHandler} settings with all
     * parameters.
     */
    public WebServiceTaskHandlerSettings(String url, String soapAction, List<Interaction> interactions, String authBase, String requestMethod,
            boolean isLoggingEnable, ErrorResponseProcessingResult errorAction) {
        this.url = url;
        this.soapAction = soapAction;
        this.interactions = interactions;
        this.authBase = authBase;
        this.requestMethod = requestMethod;
        this.isLoggingEnable = isLoggingEnable;
        this.errorAction = errorAction == null ? ErrorResponseProcessingResult.BREAK : errorAction;
    }
}
