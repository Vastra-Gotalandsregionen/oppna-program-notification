package se.vgregion.notifications.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 4/8-11
 * Time: 12:11
 */
@org.springframework.stereotype.Service
class NotesCalendarCounterService {
    private int period = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesCalendarCounterService.class);

    /**
     * Get the number of events for the current day for a user.
     *
     * @param userId the user id
     * @return the number of events as a string
     * @throws URISyntaxException URISyntaxException
     * @throws IOException IOException
     */
    public String getCount(final String userId) throws URISyntaxException, IOException {
        if (userId == null || "".equals(userId)) {
            return "";
        }

        Calendar now = Calendar.getInstance();

        URI uri = new URI("http", "aida.vgregion.se", "/calendar.nsf/getinfo", "openagent&userid=" + userId + "&year="
                + getYear(now) + "&month=" + getMonth(now) + "&day=" + getDay(now) + "&period=" + getPeriod(), "");

        HttpResponse httpResponse = callService(uri);

        return handleResponse(httpResponse);
    }

    private HttpResponse callService(URI uri) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);

        HttpParams params = httpClient.getParams();
        final int timeout = 10000;
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);

        return httpClient.execute(httpGet);
    }

    private String handleResponse(HttpResponse httpResponse) throws IOException {
        final int ok = 200;
        if (httpResponse.getStatusLine().getStatusCode() == ok) {
            String reply = IOUtils.toString(httpResponse.getEntity().getContent());

            if (reply == null) {
                LOGGER.error("Http request failed. Service did not respond.");
                return "-";
            }

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(reply.getBytes()));
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                XPathExpression expr = xpath.compile("/calendarItems/status/text()");

                String status = expr.evaluate(doc);

                if ("PROCESSED".equals(status)) {
                    String res = xpath.compile("/calendarItems/total/text()").evaluate(doc);
                    return res;
                } else {
                    return ""; //The user does not have any notes calendar and should receive nothing.
                }

            } catch (RuntimeException ex) {
                LOGGER.warn(ex.getMessage());
            } catch (ParserConfigurationException e) {
                LOGGER.warn(e.getMessage());
            } catch (SAXException e) {
                LOGGER.warn(e.getMessage());
            } catch (XPathExpressionException e) {
                LOGGER.warn(e.getMessage());
            }

            return "-";
        } else {
            LOGGER.error("Http request failed. Response code=" + httpResponse.getStatusLine().getStatusCode() + ". "
                    + httpResponse.getStatusLine().getReasonPhrase());
            return "-";
        }
    }

    private int getYear(Calendar date) {
        return date.get(Calendar.YEAR);
    }

    private int getMonth(Calendar date) {
        return date.get(Calendar.MONTH) + 1;
    }

    private int getDay(Calendar date) {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
