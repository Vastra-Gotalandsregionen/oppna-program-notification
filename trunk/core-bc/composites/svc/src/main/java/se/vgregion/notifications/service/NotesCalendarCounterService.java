package se.vgregion.notifications.service;

import org.apache.camel.*;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

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
public class NotesCalendarCounterService {
    private int period = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesCalendarCounterService.class);

    public String getCount(final String userId, CamelContext context) throws URISyntaxException, IOException {
        if (userId == null || "".equals(userId)) return "";

        Calendar now = Calendar.getInstance();

        URI uri = new URI("http", "aida.vgregion.se", "/calendar.nsf/getinfo", "openagent&userid=" + userId + "&year="
                + getYear(now) + "&month=" + getMonth(now) + "&day=" + getDay(now) + "&period=" + getPeriod(), "");

        HttpResponse httpResponse = callService(uri);

        return handleResponse(context, httpResponse);
    }

    private HttpResponse callService(URI uri) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);

        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 10000);

        return httpClient.execute(httpGet);
    }

    private String handleResponse(CamelContext context, HttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String reply = IOUtils.toString(httpResponse.getEntity().getContent());

            if (reply == null) {
                LOGGER.error("Http request failed. Service did not respond.");
                return "-";
            }

            try {
                String status = XPathBuilder.xpath("/calendarItems/status/text()").evaluate(context, reply,
                        String.class);

                if ("PROCESSED".equals(status)) {
                    String res = XPathBuilder.xpath("/calendarItems/total/text()").evaluate(context, reply,
                            String.class);
                    return res;
                } else {
                    return ""; //The user does not have any notes calendar and should receive nothing.
                }

            } catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
            }

            return "-";
        } else {
            LOGGER.error("Http request failed. Response code=" + httpResponse.getStatusLine().getStatusCode() + ". " +
                    httpResponse.getStatusLine().getReasonPhrase());
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
