package ru.runa.wf.logic.bot.webservice;

/**
 * Represents one interaction with web service.
 */
public class Interaction {
    /**
     * Request, which must be send to web service as SOAP XML. XSLT with WFE tags will be applied to this XML.  
     */
    public final String requestXML;

    /**
     * XSLT to be applied to web service response. May contains WFE tags to save variables and so on. 
     */
    public final String responseXSLT;

    /**
     * Action to be performed if error response received from web service.
     */
    public final ErrorResponseProcessingResult errorAction;

    /**
     * Maximum allowed response size. If response size is greater then maxResponseLength, {@link IllegalArgumentException} will be thrown.  
     */
    public final int maxResponseLength;

    /**
     * Variable name to store response. 
     */
    public final String responseVariable;

    /**
     * Creates interaction instance.
     * @param requestXML Request, which must be send to web service as SOAP XML.
     * @param responseXSLT XSLT to be applied to web service response.
     * @param errorAction Action to be performed if error response received from web service.
     * @param maxResponseLength Maximum allowed response size.
     * @param responseVariable Variable name to store response.
     */
    public Interaction(String requestXML, String responseXSLT, ErrorResponseProcessingResult errorAction, int maxResponseLength,
            String responseVariable) {
        this.requestXML = removeBlank(requestXML);
        this.responseXSLT = removeBlank(responseXSLT);
        this.errorAction = errorAction;
        this.maxResponseLength = maxResponseLength;
        this.responseVariable = removeBlank(responseVariable);
    }

    /**
     * Removes unnecessary whitespace and converts empty strings to null.
     * @param value String to be trimmed.
     * @return Not empty string or null.
     */
    static String removeBlank(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }
}
