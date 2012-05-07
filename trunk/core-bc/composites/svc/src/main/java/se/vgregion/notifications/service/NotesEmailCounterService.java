package se.vgregion.notifications.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.portal.cs.domain.UserSiteCredential;
import se.vgregion.portal.cs.service.CredentialService;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Class for getting the number of unread emails.
 *
 * User: david
 * Date: 8/8-11
 * Time: 14:55
 */
@Service
class NotesEmailCounterService {

    @Resource(name = "iNotesSiteKey")
    private String siteKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesEmailCounterService.class);

    @Autowired
    private CredentialService credentialService;

    /**
     * Get the number of unread emails for a user.
     *
     * @param userId user id which is the same as screen name
     * @return the number or unread emails
     * @throws IOException IOException
     */
    public Integer getCount(final String userId) throws IOException {
        if (userId == null) {
            return null;
        }

        final UserSiteCredential userSiteCredential = getSitePassword(userId);

        if (userSiteCredential == null) {
            return null;
        }

        URI uri = null;
        try {
            uri = new URI("http", "aida.vgregion.se", "/calendar.nsf/unreadcount", "openagent&userid="
                    + userSiteCredential.getSiteUser(), "");
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }

        HttpResponse httpResponse = callService(userSiteCredential.getSiteUser(),
                userSiteCredential.getSitePassword(), uri);

        return handleResponse(httpResponse);
    }

    private HttpResponse callService(String userId, String sitePassword, URI uri) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userId, sitePassword));
        httpClient.setCredentialsProvider(credsProvider);

        BasicHttpContext httpContext = new BasicHttpContext();

        // Generate BASIC scheme object and stick it to the local
        // execution context
        BasicScheme basicAuth = new BasicScheme();
        httpContext.setAttribute("preemptive-auth", basicAuth);

        // Add as the first request interceptor
        httpClient.addRequestInterceptor(new PreemptiveAuth(), 0);

        HttpParams params = httpClient.getParams();
        final int timeout = 10000;
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);

        return httpClient.execute(httpGet, httpContext);
    }

    private Integer handleResponse(HttpResponse httpResponse) throws IOException {
        final int ok = 200;
        if (httpResponse.getStatusLine().getStatusCode() == ok) {
            String reply = IOUtils.toString(httpResponse.getEntity().getContent());

            if (reply == null) {
                LOGGER.error("Http request failed. Service did not respond.", new Exception());
                return null;
            }

            if (reply.contains("DOCTYPE")) {
                // Log this way to avoid too much stacktraces in the log files.
                StackTraceElement stackTraceElement = new Exception().getStackTrace()[0];
                String stackTraceElementString = stackTraceElement.getClassName() + "." + stackTraceElement
                        .getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement
                        .getLineNumber() + ")";
                LOGGER.warn("Http request failed. Unexpected response - " + stackTraceElementString);
                return null;
            }

            Scanner scanner = new Scanner(reply);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                return null;
            } 
        } else {
            LOGGER.error("Http request failed. Response code=" + httpResponse.getStatusLine().getStatusCode() + ". "
                    + httpResponse.getStatusLine().getReasonPhrase(), new Exception());
            return null;
        }
    }

    private UserSiteCredential getSitePassword(String userId) {
        try {
            return credentialService.getUserSiteCredential(userId, siteKey);
        } catch (Exception ex) {
            return null;
        }
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {
        public void process(final HttpRequest request, final HttpContext context) throws HttpException,
                IOException {

            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context
                        .getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(),
                            targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
        }
    }

    public static void main(String[] args) {
        Exception exception = new Exception();
        StackTraceElement stackTraceElement = exception.getStackTrace()[0];
        String x = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")";
        System.out.println(x);
//        at se.vgregion.notifications.service.NotesEmailCounterService.handleResponse(NotesEmailCounterService.java:117)
    }
}
