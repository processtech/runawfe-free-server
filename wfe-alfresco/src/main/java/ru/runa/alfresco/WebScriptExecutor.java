package ru.runa.alfresco;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

/**
 * Executes web script.
 * 
 * @author Gritsenko_S
 */
public class WebScriptExecutor {
    private static final Log log = LogFactory.getLog(WebScriptExecutor.class);

    private boolean useHttpPost;
    private boolean throwExceptionOnErrorState = true;
    private final String webScriptUri;
    private final Map<String, Object> webScriptParameters;

    public WebScriptExecutor(String webScriptUri, Map<String, Object> webScriptParameters) {
        this.webScriptUri = webScriptUri;
        this.webScriptParameters = webScriptParameters;
    }

    public void setUseHttpPost(boolean useHttpPost) {
        this.useHttpPost = useHttpPost;
    }

    public void setThrowExceptionOnErrorState(boolean throwExceptionOnErrorState) {
        this.throwExceptionOnErrorState = throwExceptionOnErrorState;
    }

    public byte[] doRequest() {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null),
                    new UsernamePasswordCredentials(WSConnectionSettings.getInstance().getLogin(), WSConnectionSettings.getInstance().getPassword()));
            String alfBaseUrl = WSConnectionSettings.getInstance().getAlfBaseUrl();
            HttpUriRequest request;
            if (useHttpPost) {
                request = formHttpPostRequest(alfBaseUrl);
            } else {
                request = formHttpGetRequest(alfBaseUrl);
            }
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("WebScript status code = " + statusCode);
            if (statusCode != 200 && throwExceptionOnErrorState) {
                throw new InternalApplicationException("WebScript " + request.getRequestLine() + " status code is " + statusCode);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private HttpGet formHttpGetRequest(String alfBaseUrl) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(webScriptUri);
        boolean first = true;
        for (Map.Entry<String, Object> entry : webScriptParameters.entrySet()) {
            if (entry.getValue() == null || entry.getValue() instanceof IFileVariable) {
                log.warn("Ignored parameter " + entry.getKey() + "=" + entry.getValue());
                continue;
            }
            String paramValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
            if (first) {
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(entry.getKey()).append("=").append(paramValue);
            first = false;
        }
        log.info("Executing WebScript " + url);
        return new HttpGet(url.toString());
    }

    private HttpPost formHttpPostRequest(String alfBaseUrl) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(webScriptUri);
        HttpPost request = new HttpPost(url.toString());
        MultipartEntity entity = new MultipartEntity();
        for (Map.Entry<String, Object> entry : webScriptParameters.entrySet()) {
            if (entry.getValue() == null) {
                log.warn("Ignored null parameter " + entry.getKey());
                continue;
            }
            if (entry.getValue() instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable) entry.getValue();
                InputStream inputStream = ByteStreams.newInputStreamSupplier(fileVariable.getData()).getInput();
                InputStreamBody body = new InputStreamBody(inputStream, fileVariable.getContentType(), fileVariable.getName());
                entity.addPart(entry.getKey(), body);
            } else {
                entity.addPart(entry.getKey(), new StringBody(entry.getValue().toString(), Charset.forName("UTF-8")));
            }
        }
        long length = entity.getContentLength();
        request.setEntity(entity);
        log.info("Executing WebScript via post " + url + " content-length = " + length);
        return request;
    }

}
